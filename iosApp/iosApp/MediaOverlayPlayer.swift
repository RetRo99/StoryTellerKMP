import Foundation
import AVFoundation
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
/// This player:
/// 1. Parses SMIL files from the EPUB to get text-audio sync data
/// 2. Uses AVPlayer to play the audio files
/// 3. Tracks playback position and emits the current Locator for text highlighting
@MainActor
class MediaOverlayPlayer {

    private let publication: Publication
    private var player: AVPlayer?
    private var playerItem: AVPlayerItem?
    private var timeObserver: Any?

    private var allClips: [MediaOverlayClip] = []
    private var currentChapterClips: [MediaOverlayClip] = []
    private var currentAudioHref: RelativeURL?

    private(set) var isPlaying: Bool = false
    private(set) var currentPositionMs: Int64 = 0
    private(set) var durationMs: Int64?

    var onPlaybackStateChanged: ((MediaPlaybackState) -> Void)?
    var onLocatorChanged: ((Locator) -> Void)?

    init(publication: Publication) {
        self.publication = publication
    }

    /// Initializes the player by parsing all SMIL files from the publication.
    func initialize() async {
        allClips = await parseAllSmilFiles()
    }

    /// Starts or resumes playback for the current chapter.
    /// - Parameters:
    ///   - chapterHref: The href of the chapter to play
    ///   - initialFragmentId: Optional fragment ID to start from (e.g., "chapter44.xhtml-sentence50")
    ///   - initialProgression: Optional text progression (0.0 to 1.0) to estimate audio position
    ///   - initialPositionMs: Optional initial position in milliseconds to seek to before playing
    ///                        (used if fragment ID and progression are not provided or not found)
    func play(chapterHref: RelativeURL?, initialFragmentId: String? = nil, initialProgression: Double? = nil, initialPositionMs: Int64? = nil) {
        if let chapterHref = chapterHref {
            prepareChapter(chapterHref: chapterHref, initialFragmentId: initialFragmentId, initialProgression: initialProgression, initialPositionMs: initialPositionMs)
        } else {
            // No chapter change - try to seek to fragment, progression, or position
            let positionToSeek = findPositionForFragment(fragmentId: initialFragmentId)
                ?? findPositionForProgression(progression: initialProgression)
                ?? initialPositionMs
            if let positionMs = positionToSeek, positionMs > 0 {
                seekTo(positionMs: positionMs)
            }
            startPlayback()
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

    func seekTo(positionMs: Int64) {
        let time = CMTime(value: positionMs, timescale: 1000)
        player?.seek(to: time)
        updateCurrentLocator()
    }

    func setPlaybackSpeed(speed: Float) {
        player?.rate = speed
    }

    /// Prepares the duration for a specific chapter without starting playback.
    /// This allows the UI to show the chapter duration before the user presses play.
    /// - Parameter chapterHref: The href of the chapter to get duration for
    func prepareChapterDuration(chapterHref: RelativeURL) {
        let chapterPath = chapterHref.removingFragment()

        // Find clips for this chapter
        let chapterClips = allClips.filter { clip in
            clip.textHref.removingFragment() == chapterPath
        }

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
        if let observer = timeObserver {
            player?.removeTimeObserver(observer)
            timeObserver = nil
        }
        player?.pause()
        player = nil
        playerItem = nil
    }

    // MARK: - Private Methods

    private func prepareChapter(chapterHref: RelativeURL, initialFragmentId: String? = nil, initialProgression: Double? = nil, initialPositionMs: Int64? = nil) {
        // Remove fragment from href for comparison
        let chapterPath = chapterHref.removingFragment()

        // Find clips for this chapter
        currentChapterClips = allClips.filter { clip in
            clip.textHref.removingFragment() == chapterPath
        }

        guard !currentChapterClips.isEmpty else {
            return
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

        // Determine the position to seek to - prefer fragment, then progression, then position
        let positionToSeek = findPositionForFragment(fragmentId: initialFragmentId)
            ?? findPositionForProgression(progression: initialProgression)
            ?? initialPositionMs

        if currentAudioHref != audioHref {
            currentAudioHref = audioHref
            prepareAudio(audioHref: audioHref, shouldAutoPlay: true, initialPositionMs: positionToSeek)
        } else if let positionMs = positionToSeek, positionMs > 0 {
            // Same audio file, seek to position and start playback
            seekTo(positionMs: positionMs)
            startPlayback()
        } else {
            // Same audio file, just start playback
            startPlayback()
        }
    }

    private func prepareAudio(audioHref: RelativeURL, shouldAutoPlay: Bool = false, initialPositionMs: Int64? = nil) {
        // Get the resource from the publication
        guard let resource = publication.get(audioHref) else {
            return
        }

        Task {
            do {
                // Read the data from the resource
                let data = try await resource.read().get()

                // Determine file extension from href
                let hrefString: String = audioHref.description
                let pathExtension = (hrefString as NSString).pathExtension.isEmpty
                    ? "mp3"
                    : (hrefString as NSString).pathExtension

                // Write to a temporary file
                let tempURL = FileManager.default.temporaryDirectory
                    .appendingPathComponent(UUID().uuidString)
                    .appendingPathExtension(pathExtension)

                try data.write(to: tempURL)

                setupPlayer(with: tempURL)

                // Seek to initial position if provided
                if let positionMs = initialPositionMs, positionMs > 0 {
                    seekTo(positionMs: positionMs)
                }

                // Auto-play after setup if requested
                if shouldAutoPlay {
                    startPlayback()
                }
            } catch {
                // Failed to read audio data
            }
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
        let interval = CMTime(seconds: 0.1, preferredTimescale: 600)
        timeObserver = player?.addPeriodicTimeObserver(forInterval: interval, queue: .main) { [weak self] time in
            guard let self = self else {
                return
            }
            let seconds = CMTimeGetSeconds(time)
            self.currentPositionMs = Int64(seconds * 1000)
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


    // MARK: - SMIL Parsing

    private func parseAllSmilFiles() async -> [MediaOverlayClip] {
        var clips: [MediaOverlayClip] = []

        // Find all SMIL resources in the publication
        // Use explicit Array filter to avoid SwiftSoup String extension conflict
        var smilResources: [Link] = []
        for link in publication.resources {
            // Use description to get the URL string to avoid SwiftSoup conflict
            let hrefString: String = link.href.description
            if link.mediaType?.matches(.smil) == true ||
                   hrefString.hasSuffix(".smil") {
                smilResources.append(link)
            }
        }

        for smilLink in smilResources {
            // Convert AnyURL to RelativeURL
            guard let relativeHref = RelativeURL(string: smilLink.href.description) else {
                continue
            }
            do {
                let smilClips = try await parseSmilFile(smilHref: relativeHref)
                clips.append(contentsOf: smilClips)
            } catch {
                // Failed to parse SMIL file
            }
        }

        // Sort by audio file and start time
        return clips.sorted {
            ($0.audioHref.description, $0.startTime) < ($1.audioHref.description, $1.startTime)
        }
    }

    private func parseSmilFile(smilHref: RelativeURL) async throws -> [MediaOverlayClip] {
        guard let resource = publication.get(smilHref) else {
            return []
        }

        let data = try await resource.read().get()
        guard let content = String(data: data, encoding: .utf8) else {
            return []
        }

        // Parse XML
        let parser = SmilParser(smilHref: smilHref, content: content)
        let clips = parser.parse()

        return clips
    }
}

// MARK: - SMIL XML Parser

private class SmilParser: NSObject, XMLParserDelegate {
    private let smilHref: RelativeURL
    private let content: String
    private var clips: [MediaOverlayClip] = []

    // Current parsing state
    private var currentTextSrc: String?
    private var currentAudioSrc: String?
    private var currentClipBegin: Double?
    private var currentClipEnd: Double?
    private var inPar: Bool = false

    init(smilHref: RelativeURL, content: String) {
        self.smilHref = smilHref
        self.content = content
    }

    func parse() -> [MediaOverlayClip] {
        guard let data = content.data(using: .utf8) else {
            return []
        }
        let parser = XMLParser(data: data)
        parser.delegate = self
        parser.parse()
        return clips
    }

    // MARK: - XMLParserDelegate

    func parser(_ parser: XMLParser, didStartElement elementName: String,
                namespaceURI: String?, qualifiedName qName: String?,
                attributes attributeDict: [String: String] = [:]) {
        switch elementName {
        case "par":
            inPar = true
            currentTextSrc = nil
            currentAudioSrc = nil
            currentClipBegin = nil
            currentClipEnd = nil

        case "text" where inPar:
            currentTextSrc = attributeDict["src"]

        case "audio" where inPar:
            currentAudioSrc = attributeDict["src"]
            currentClipBegin = parseClockValue(attributeDict["clipBegin"])
            currentClipEnd = parseClockValue(attributeDict["clipEnd"])

        default:
            break
        }
    }

    func parser(_ parser: XMLParser, didEndElement elementName: String,
                namespaceURI: String?, qualifiedName qName: String?) {
        guard elementName == "par", inPar else {
            return
        }
        inPar = false

        guard let textSrc = currentTextSrc,
              let audioSrc = currentAudioSrc
        else {
            return
        }

        // Parse text reference to get href and fragment
        guard let textUrl = RelativeURL(string: textSrc),
              let resolvedTextUrl = smilHref.resolve(textUrl)
        else {
            return
        }
        let fragmentId = resolvedTextUrl.fragment

        // Parse audio reference
        guard let audioUrl = RelativeURL(string: audioSrc),
              let resolvedAudioUrl = smilHref.resolve(audioUrl)?.removingFragment()
        else {
            return
        }

        let clip = MediaOverlayClip(
            textHref: resolvedTextUrl.removingFragment(),
            fragmentId: fragmentId,
            audioHref: resolvedAudioUrl,
            startTime: currentClipBegin ?? 0.0,
            endTime: currentClipEnd ?? 0.0
        )
        clips.append(clip)
    }

    // MARK: - Clock Value Parsing

    private func parseClockValue(_ value: String?) -> Double? {
        guard let value = value?.trimmingCharacters(in: .whitespaces), !value.isEmpty else {
            return nil
        }

        // Handle colon format (HH:MM:SS or MM:SS)
        if value.contains(":") {
            return parseColonClockValue(value)
        }

        // Handle metric format (1.5s, 500ms, 2min, 1h)
        return parseMetricClockValue(value)
    }

    private func parseColonClockValue(_ value: String) -> Double? {
        let parts = value.split(separator: ":").compactMap {
            Double($0)
        }
        switch parts.count {
        case 2: return parts[0] * 60 + parts[1]  // MM:SS
        case 3: return parts[0] * 3600 + parts[1] * 60 + parts[2]  // HH:MM:SS
        default: return parts.last
        }
    }

    private func parseMetricClockValue(_ value: String) -> Double? {
        // Find where the numeric part ends
        let metricStart = value.firstIndex(where: { $0.isLetter }) ?? value.endIndex
        guard let count = Double(value[..<metricStart]) else {
            return nil
        }

        let metric = String(value[metricStart...])
        switch metric {
        case "h": return count * 3600
        case "min": return count * 60
        case "s", "": return count
        case "ms": return count / 1000
        default: return count
        }
    }
}