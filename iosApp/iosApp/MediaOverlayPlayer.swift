import Foundation
import AVFoundation
import ComposeApp
import ReadiumShared
import ReadiumNavigator

/// Represents a single text-audio synchronization point from SMIL.
struct MediaOverlayClip {
    let textHref: RelativeURL
    let fragmentId: String?
    let audioHref: RelativeURL
    let startTime: Double
    let endTime: Double
}

/// Playback state for the media overlay player.
struct MediaPlaybackState {
    let isPlaying: Bool
    let currentPositionMs: Int64
    let durationMs: Int64?
}

/// Player for EPUB Media Overlays (SMIL-based text-audio synchronization).
///
/// This player uses lazy loading to optimize initialization:
/// 1. Builds a lightweight SMIL→chapter index on init (fast regex scan)
/// 2. Parses SMIL files on-demand when a chapter is accessed
/// 3. Caches parsed clips for the session
/// 4. Prefetches next chapter in background
@MainActor
class MediaOverlayPlayer {

    private let publication: Publication
    private var player: AVPlayer?
    private var playerItem: AVPlayerItem?
    private var timeObserver: Any?

    // Lazy loading: chapter→clips cache (parsed on demand)
    private var chapterClipsCache: [String: [MediaOverlayClip]] = [:]

    // Lazy loading: chapter→SMIL files index (built on init)
    private var chapterToSmilIndex: [String: [RelativeURL]] = [:]

    // Set of SMIL files that have been scanned for indexing
    private var scannedSmilFiles: Set<String> = []

    // All SMIL resources in the publication
    private var allSmilResources: [Link] = []

    // Reading order for prefetching
    private var readingOrder: [String] = []

    // Shared parsers from Kotlin (accessed via Koin provider)
    private let smilParser: SmilParser
    private let quickScanner: SmilQuickScanner

    // Number of chapters ahead to scan during initial index build
    private let initialScanAhead = 3

    private var currentChapterClips: [MediaOverlayClip] = []
    private var currentAudioHref: RelativeURL?

    private(set) var isPlaying: Bool = false
    private(set) var currentPositionMs: Int64 = 0
    private(set) var durationMs: Int64?

    /// Flag to prevent time observer from overwriting position immediately after seek
    /// The time observer should wait until AVPlayer's position catches up to our seeked position
    private var lastSeekTargetMs: Int64?

    /// Cache of temp file URLs indexed by audio href for reuse
    private var tempFileCache: [String: URL] = [:]

    /// Background prefetch task
    private var prefetchTask: Task<Void, Never>?

    var onPlaybackStateChanged: ((MediaPlaybackState) -> Void)?
    var onLocatorChanged: ((Locator) -> Void)?

    init(publication: Publication) {
        self.publication = publication
        self.smilParser = SmilParserProvider.shared.smilParser
        self.quickScanner = SmilParserProvider.shared.quickScanner
    }

    /// Initializes the player with lazy SMIL loading.
    /// Builds a lightweight index without fully parsing all SMIL files.
    /// - Parameter initialChapterHref: The initial chapter to optimize index building for
    func initialize(initialChapterHref: String? = nil) async {
        // Collect all SMIL resources
        allSmilResources = publication.resources.filter { link in
            let hrefString: String = link.href.description
            return link.mediaType?.matches(.smil) == true || hrefString.hasSuffix(".smil")
        }

        // Build reading order
        readingOrder = publication.readingOrder.map {
            normalizeChapterHref($0.href.description)
        }

        // Build initial index for current chapter and nearby chapters
        let chapterHref = initialChapterHref ?? publication.readingOrder.first?.href.description ?? ""
        await buildInitialIndex(currentChapterHref: chapterHref)
    }

    /// Legacy initialize for backward compatibility
    func initialize() async {
        await initialize(initialChapterHref: nil)
    }

    /// Starts or resumes playback for the current chapter.
    /// - Parameters:
    ///   - chapterHref: The href of the chapter to play
    ///   - initialFragmentId: Optional fragment ID to start from (e.g., "chapter44.xhtml-sentence50")
    ///   - initialProgression: Optional text progression (0.0 to 1.0) to estimate audio position
    ///   - initialPositionMs: Optional initial position in milliseconds to seek to before playing
    ///                        (preferred over fragment/progression when provided, matching Android behavior)
    func play(chapterHref: RelativeURL?, initialFragmentId: String? = nil, initialProgression: Double? = nil, initialPositionMs: Int64? = nil) {
        if let chapterHref = chapterHref {
            prepareChapter(chapterHref: chapterHref, initialFragmentId: initialFragmentId, initialProgression: initialProgression, initialPositionMs: initialPositionMs)
        } else {
            // No chapter change - try to seek to position, fragment, or progression
            // Prefer initialPositionMs first (saved audio position), then fall back to text-based positions
            let positionToSeek = initialPositionMs
                ?? findPositionForFragment(fragmentId: initialFragmentId)
                ?? findPositionForProgression(progression: initialProgression)
            // Use helper to ensure seek completes before playback starts
            setupAndPlay(initialPositionMs: positionToSeek, shouldAutoPlay: true)
        }
    }

    /// Finds the audio position in milliseconds for a given text fragment ID.
    /// - Parameter fragmentId: The fragment ID to find (e.g., "chapter44.xhtml-sentence50")
    /// - Returns: The start time in milliseconds, or nil if not found
    private func findPositionForFragment(fragmentId: String?) -> Int64? {
        guard let fragmentId = fragmentId else {
            return nil
        }

        if let clip = currentChapterClips.first(where: { $0.fragmentId == fragmentId }) {
            let positionMs = Int64(clip.startTime * 1000)
            return positionMs
        }

        return nil
    }

    /// Finds the audio position in milliseconds for a given text progression.
    /// Uses the progression to estimate which clip corresponds to that position in the text.
    /// - Parameter progression: The text progression (0.0 to 1.0) through the chapter
    /// - Returns: The start time in milliseconds, or nil if clips are empty
    private func findPositionForProgression(progression: Double?) -> Int64? {
        guard let progression = progression, progression > 0.0, !currentChapterClips.isEmpty else {
            return nil
        }

        // Estimate which clip corresponds to this progression
        // If we have 440 clips and progression is 0.09, we want clip ~40
        let clipIndex = min(max(Int(progression * Double(currentChapterClips.count)), 0), currentChapterClips.count - 1)

        let clip = currentChapterClips[clipIndex]
        let positionMs = Int64(clip.startTime * 1000)
        return positionMs
    }

    private func startPlayback() {
        player?.play()
        isPlaying = true
        notifyPlaybackStateChanged()
    }

    func pause() {
        player?.pause()
        isPlaying = false
        notifyPlaybackStateChanged()
    }

    /// Resumes playback from the current position without seeking.
    func resume() {
        player?.play()
        isPlaying = true
        notifyPlaybackStateChanged()
    }

    func seekTo(positionMs: Int64, completion: (() -> Void)? = nil) {
        let time = CMTime(value: positionMs, timescale: 1000)
        // Update position immediately to avoid emitting stale position
        currentPositionMs = positionMs
        // Set the seek target so time observer knows to wait for AVPlayer to catch up
        lastSeekTargetMs = positionMs
        player?.seek(to: time) { [weak self] _ in
            self?.updateCurrentLocator()
            completion?()
        }
    }

    func setPlaybackSpeed(speed: Float) {
        player?.rate = speed
    }

    /// Prepares the duration for a specific chapter without starting playback.
    /// This allows the UI to show the chapter duration before the user presses play.
    /// - Parameter chapterHref: The href of the chapter to get duration for
    func prepareChapterDuration(chapterHref: RelativeURL) async {
        let normalizedHref = normalizeChapterHref(chapterHref.description)

        // Get clips using lazy loading
        let chapterClips = await getClipsForChapter(chapterHref: normalizedHref)

        guard !chapterClips.isEmpty else {
            return
        }

        // Calculate total duration from clips (last clip's end time)
        if let maxEndTime = chapterClips.map({ $0.endTime }).max() {
            let chapterDurationMs = Int64(maxEndTime * 1000)
            if chapterDurationMs > 0 {
                self.durationMs = chapterDurationMs
                notifyPlaybackStateChanged()
            }
        }
    }

    func release() {
        // Cancel any pending prefetch
        prefetchTask?.cancel()
        prefetchTask = nil

        if let observer = timeObserver {
            player?.removeTimeObserver(observer)
            timeObserver = nil
        }
        player?.pause()
        player = nil
        playerItem = nil

        // Clean up all cached temp files
        for (_, tempURL) in tempFileCache {
            try? FileManager.default.removeItem(at: tempURL)
        }
        tempFileCache.removeAll()

        // Clear lazy loading caches
        chapterClipsCache.removeAll()
        chapterToSmilIndex.removeAll()
        scannedSmilFiles.removeAll()
    }

    // MARK: - Private Methods

    private func prepareChapter(chapterHref: RelativeURL, initialFragmentId: String? = nil, initialProgression: Double? = nil, initialPositionMs: Int64? = nil) {
        let normalizedHref = normalizeChapterHref(chapterHref.description)

        // Use Task to handle async clip loading
        Task {
            // Get clips using lazy loading
            currentChapterClips = await getClipsForChapter(chapterHref: normalizedHref)

            guard !currentChapterClips.isEmpty else {
                return
            }

            // Determine the position to seek to - prefer initialPositionMs (saved audio position),
            // then fall back to fragment or progression (text-based positions)
            // This matches Android behavior for consistent position restoration
            let positionToSeek = initialPositionMs
                ?? findPositionForFragment(fragmentId: initialFragmentId)
                ?? findPositionForProgression(progression: initialProgression)

            // Set currentPositionMs to the target position before emitting any state updates
            // This prevents the UI from briefly showing 0 while preparing
            if let targetPosition = positionToSeek, targetPosition > 0 {
                currentPositionMs = targetPosition
            }

            // Calculate total duration from clips (last clip's end time)
            if let maxEndTime = currentChapterClips.map({ $0.endTime }).max() {
                let chapterDurationMs = Int64(maxEndTime * 1000)
                if chapterDurationMs > 0 {
                    self.durationMs = chapterDurationMs
                    notifyPlaybackStateChanged()
                }
            }

            // Get the audio file for this chapter
            guard let audioHref = currentChapterClips.first?.audioHref else {
                return
            }

            if currentAudioHref != audioHref {
                currentAudioHref = audioHref
                prepareAudio(audioHref: audioHref, shouldAutoPlay: true, initialPositionMs: positionToSeek)
            } else {
                // Same audio file, use helper to seek and play
                setupAndPlay(initialPositionMs: positionToSeek, shouldAutoPlay: true)
            }

            // Prefetch next chapter in background
            prefetchNextChapter(currentChapterHref: normalizedHref)
        }
    }

    private func prepareAudio(audioHref: RelativeURL, shouldAutoPlay: Bool = false, initialPositionMs: Int64? = nil) {
        let cacheKey = audioHref.description

        // Check if we already have this audio file cached
        if let cachedURL = tempFileCache[cacheKey], FileManager.default.fileExists(atPath: cachedURL.path) {
            setupPlayer(with: cachedURL)
            setupAndPlay(initialPositionMs: initialPositionMs, shouldAutoPlay: shouldAutoPlay)
            return
        }

        // Get the resource from the publication
        guard let resource = publication.get(audioHref) else {
            return
        }

        Task {
            do {
                // Determine file extension from href
                let hrefString: String = audioHref.description
                let pathExtension = (hrefString as NSString).pathExtension.isEmpty
                    ? "mp3"
                    : (hrefString as NSString).pathExtension

                // Create a temporary file URL
                let tempURL = FileManager.default.temporaryDirectory
                    .appendingPathComponent(UUID().uuidString)
                    .appendingPathExtension(pathExtension)

                // Create file handle for streaming write
                FileManager.default.createFile(atPath: tempURL.path, contents: nil)
                let fileHandle = try FileHandle(forWritingTo: tempURL)

                // Stream data directly to file in chunks instead of loading all into memory
                let result = await resource.stream { chunk in
                    try? fileHandle.write(contentsOf: chunk)
                }

                try fileHandle.close()

                // Check if streaming was successful
                guard case .success = result else {
                    try? FileManager.default.removeItem(at: tempURL)
                    return
                }

                // Cache the temp file URL for reuse
                tempFileCache[cacheKey] = tempURL

                setupPlayer(with: tempURL)
                setupAndPlay(initialPositionMs: initialPositionMs, shouldAutoPlay: shouldAutoPlay)
            } catch {
                // Failed to prepare audio
            }
        }
    }

    /// Helper to seek to initial position and optionally start playback.
    /// Ensures playback only starts after seek completes to avoid emitting position 0.
    private func setupAndPlay(initialPositionMs: Int64?, shouldAutoPlay: Bool) {
        if let positionMs = initialPositionMs, positionMs > 0 {
            // Seek first, then start playback after seek completes
            seekTo(positionMs: positionMs) { [weak self] in
                if shouldAutoPlay {
                    self?.startPlayback()
                }
            }
        } else if shouldAutoPlay {
            // No seek needed, start playback immediately
            startPlayback()
        }
    }

    private func setupPlayer(with url: URL) {
        // Remove existing time observer
        if let observer = timeObserver {
            player?.removeTimeObserver(observer)
            timeObserver = nil
        }

        // Create new player item and player
        playerItem = AVPlayerItem(url: url)
        player = AVPlayer(playerItem: playerItem)

        // Get duration when available
        Task {
            if let duration = try? await playerItem?.asset.load(.duration) {
                let durationSeconds = CMTimeGetSeconds(duration)
                if durationSeconds.isFinite {
                    self.durationMs = Int64(durationSeconds * 1000)
                }
            }
        }

        // Add periodic time observer for sync (100ms interval)
        // Only emit position updates when actually playing to avoid emitting 0 during seek
        let interval = CMTime(seconds: 0.1, preferredTimescale: 600)
        timeObserver = player?.addPeriodicTimeObserver(forInterval: interval, queue: .main) { [weak self] time in
            guard let self = self, self.isPlaying else {
                return
            }
            let seconds = CMTimeGetSeconds(time)
            let observedPositionMs = Int64(seconds * 1000)

            // If we recently seeked, wait for AVPlayer's position to catch up
            // before allowing the time observer to update currentPositionMs
            if let seekTarget = self.lastSeekTargetMs {
                // Allow some tolerance (500ms) for the position to be considered "caught up"
                // This handles cases where the seek might not land exactly on the target
                let tolerance: Int64 = 500
                if abs(observedPositionMs - seekTarget) <= tolerance {
                    // AVPlayer has caught up, clear the flag and use observed position
                    self.lastSeekTargetMs = nil
                    self.currentPositionMs = observedPositionMs
                } else if observedPositionMs > seekTarget {
                    // We've passed the seek target, clear the flag
                    self.lastSeekTargetMs = nil
                    self.currentPositionMs = observedPositionMs
                }
                // Otherwise, keep using the seeked position (don't update currentPositionMs)
            } else {
                // Normal playback, update position from observer
                self.currentPositionMs = observedPositionMs
            }

            self.updateCurrentLocator()
            self.notifyPlaybackStateChanged()
        }
    }

    private func notifyPlaybackStateChanged() {
        let state = MediaPlaybackState(
            isPlaying: isPlaying,
            currentPositionMs: currentPositionMs,
            durationMs: durationMs
        )
        onPlaybackStateChanged?(state)
    }

    private var lastFragmentId: String?

    private func updateCurrentLocator() {
        let currentTimeSeconds = Double(currentPositionMs) / 1000.0

        // Find the clip that contains the current time
        let currentClip = currentChapterClips.first { clip in
            currentTimeSeconds >= clip.startTime && currentTimeSeconds < clip.endTime
        }

        guard let clip = currentClip, let fragmentId = clip.fragmentId else {
            return
        }

        // Only emit if fragment changed
        guard fragmentId != lastFragmentId else {
            return
        }
        lastFragmentId = fragmentId

        // Create a locator for the current text fragment
        // Convert RelativeURL to AnyURL using its string representation
        guard let anyUrl = AnyURL(string: clip.textHref.description) else {
            return
        }
        let locator = Locator(
            href: anyUrl,
            mediaType: .html,
            locations: Locator.Locations(
                fragments: [fragmentId]
            )
        )

        onLocatorChanged?(locator)
    }


    // MARK: - Lazy Loading

    /// Builds the initial SMIL→chapter index for the current chapter and nearby chapters.
    /// Uses fast regex scanning instead of full XML parsing.
    private func buildInitialIndex(currentChapterHref: String) async {
        let normalizedCurrent = normalizeChapterHref(currentChapterHref)

        // Find current chapter index in reading order
        let currentIndex = readingOrder.firstIndex(of: normalizedCurrent) ?? 0

        // Determine which chapters we need to find SMILs for
        let startIndex = max(0, currentIndex)
        let endIndex = min(readingOrder.count, currentIndex + initialScanAhead + 1)
        let chaptersToFind = Set(readingOrder[startIndex..<endIndex])

        var remainingChapters = chaptersToFind

        // Scan SMIL files until we find all needed chapters
        for smilLink in allSmilResources {
            // Early exit if we found all needed chapters
            if remainingChapters.isEmpty {
                break
            }

            let smilHref = smilLink.href.description

            // Skip if already scanned
            if scannedSmilFiles.contains(smilHref) {
                continue
            }

            // Quick scan to find chapter reference
            if let chapterHref = await scanSmilFile(smilHref: smilHref) {
                // Register in index
                if chapterToSmilIndex[chapterHref] == nil {
                    chapterToSmilIndex[chapterHref] = []
                }
                if let relativeUrl = RelativeURL(string: smilHref) {
                    chapterToSmilIndex[chapterHref]?.append(relativeUrl)
                }

                // Check if this was one of the chapters we needed
                if remainingChapters.contains(chapterHref) {
                    remainingChapters.remove(chapterHref)
                }
            }

            scannedSmilFiles.insert(smilHref)
        }
    }

    /// Quick scans a SMIL file to extract the chapter it references.
    /// Uses regex instead of full XML parsing for speed.
    private func scanSmilFile(smilHref: String) async -> String? {
        guard let relativeHref = RelativeURL(string: smilHref),
              let resource = publication.get(relativeHref)
        else {
            return nil
        }

        do {
            let data = try await resource.read().get()
            guard let content = String(data: data, encoding: .utf8) else {
                return nil
            }

            // Use shared Kotlin quick scanner
            guard let chapterHref = quickScanner.scanForChapterHref(
                content: content,
                smilHref: smilHref
            )
            else {
                return nil
            }

            return normalizeChapterHref(chapterHref)
        } catch {
            return nil
        }
    }

    /// Gets clips for a chapter, parsing on-demand if not cached.
    private func getClipsForChapter(chapterHref: String) async -> [MediaOverlayClip] {
        let normalizedHref = normalizeChapterHref(chapterHref)

        // Check cache first
        if let cached = chapterClipsCache[normalizedHref] {
            return cached
        }

        // Parse SMIL files for this chapter
        let clips = await parseChapterSmilFiles(chapterHref: normalizedHref)

        // Cache the result
        chapterClipsCache[normalizedHref] = clips

        return clips
    }

    /// Parses all SMIL files for a specific chapter.
    private func parseChapterSmilFiles(chapterHref: String) async -> [MediaOverlayClip] {
        // First, ensure we have the index for this chapter
        if chapterToSmilIndex[chapterHref] == nil {
            // Scan all remaining SMIL files to find this chapter
            for smilLink in allSmilResources {
                let smilHref = smilLink.href.description

                if scannedSmilFiles.contains(smilHref) {
                    continue
                }

                if let foundChapter = await scanSmilFile(smilHref: smilHref) {
                    if chapterToSmilIndex[foundChapter] == nil {
                        chapterToSmilIndex[foundChapter] = []
                    }
                    if let relativeUrl = RelativeURL(string: smilHref) {
                        chapterToSmilIndex[foundChapter]?.append(relativeUrl)
                    }
                }

                scannedSmilFiles.insert(smilHref)

                // Stop if we found the chapter we're looking for
                if chapterToSmilIndex[chapterHref] != nil {
                    break
                }
            }
        }

        // Get SMIL files for this chapter
        guard let smilFiles = chapterToSmilIndex[chapterHref], !smilFiles.isEmpty else {
            return []
        }

        // Parse all SMIL files for this chapter
        var allClips: [MediaOverlayClip] = []
        for smilHref in smilFiles {
            do {
                let clips = try await parseSmilFile(smilHref: smilHref)
                allClips.append(contentsOf: clips)
            } catch {
                // Failed to parse SMIL file - continue with others
            }
        }

        // Sort by start time
        return allClips.sorted {
            $0.startTime < $1.startTime
        }
    }

    /// Prefetches the next chapter's clips in the background.
    private func prefetchNextChapter(currentChapterHref: String) {
        let normalizedCurrent = normalizeChapterHref(currentChapterHref)

        // Find next chapter in reading order
        guard let currentIndex = readingOrder.firstIndex(of: normalizedCurrent),
              currentIndex + 1 < readingOrder.count
        else {
            return
        }

        let nextChapterHref = readingOrder[currentIndex + 1]

        // Skip if already cached
        if chapterClipsCache[nextChapterHref] != nil {
            return
        }

        // Cancel any existing prefetch
        prefetchTask?.cancel()

        // Start background prefetch
        prefetchTask = Task {
            _ = await getClipsForChapter(chapterHref: nextChapterHref)
        }
    }

    /// Normalizes a chapter href for consistent comparison.
    private func normalizeChapterHref(_ href: String) -> String {
        // Use shared Kotlin normalizer
        return quickScanner.normalizeChapterHref(href: href)
    }

    // MARK: - SMIL Parsing

    private func parseSmilFile(smilHref: RelativeURL) async throws -> [MediaOverlayClip] {
        guard let resource = publication.get(smilHref) else {
            return []
        }

        let data = try await resource.read().get()
        guard let content = String(data: data, encoding: .utf8) else {
            return []
        }

        // Parse XML using shared Kotlin SMIL parser
        let rawClips = smilParser.parseClips(content: content)
        var clips: [MediaOverlayClip] = []

        for item in rawClips {
            guard let raw = item as? SmilClip else {
                continue
            }
            guard let textUrl = RelativeURL(string: raw.textSrc),
                  let resolvedTextUrl = smilHref.resolve(textUrl)
            else {
                continue
            }
            let fragmentId = resolvedTextUrl.fragment

            guard let audioUrl = RelativeURL(string: raw.audioSrc),
                  let resolvedAudioUrl = smilHref.resolve(audioUrl)?.removingFragment()
            else {
                continue
            }

            clips.append(
                MediaOverlayClip(
                    textHref: resolvedTextUrl.removingFragment(),
                    fragmentId: fragmentId,
                    audioHref: resolvedAudioUrl,
                    startTime: raw.clipBegin,
                    endTime: raw.clipEnd
                )
            )
        }

        return clips
    }
}
