import Foundation
import UIKit
import WebKit
import os.log
import ComposeApp
import ReadiumShared
import ReadiumStreamer
import ReadiumNavigator
import ReadiumAdapterGCDWebServer

/// Logger for the EPUB reader bridge
private let logger = Logger(subsystem: "com.retro99.storyteller", category: "EpubReaderBridge")

/// Swift implementation of the EPUB reader bridge.
/// This class wraps Readium iOS SDK and exposes it to Kotlin.
@MainActor
class ReadiumEpubReaderBridge: EpubReaderBridge {

    private var publication: Publication?
    private var navigatorViewController: EPUBNavigatorViewController?
    private var onPositionChangedCallback: ((PositionLocator) -> Void)?

    // Media overlay support
    private var mediaOverlayPlayer: MediaOverlayPlayer?
    private var onPlaybackStateChangedCallback: ((PlaybackState) -> Void)?
    private var onMediaPlayerReadyCallback: (() -> Void)?
    private var currentHighlightId: String?
    private var currentChapterHref: RelativeURL?

    // Readium 3.x infrastructure
    private lazy var httpClient: HTTPClient = DefaultHTTPClient()
    private lazy var assetRetriever = AssetRetriever(httpClient: httpClient)
    private lazy var httpServer: HTTPServer = GCDHTTPServer(assetRetriever: assetRetriever)
    private lazy var publicationOpener = PublicationOpener(
        parser: DefaultPublicationParser(
            httpClient: httpClient,
            assetRetriever: assetRetriever,
            pdfFactory: DefaultPDFDocumentFactory()
        ),
        contentProtections: []
    )

    init() {
        logger.info("ReadiumEpubReaderBridge initialized")
    }

    func openPublication(
        filePath: String,
        onSuccess: @escaping () -> Void,
        onError: @escaping (String) -> Void
    ) {
        logger.info("openPublication called with filePath: \(filePath)")
        Task { @MainActor in
            do {
                // Convert file path to Foundation URL, then to Readium's FileURL
                let foundationUrl = URL(fileURLWithPath: filePath)
                guard let fileUrl = FileURL(url: foundationUrl) else {
                    logger.error("Invalid file path: \(filePath)")
                    onError("Invalid file path: \(filePath)")
                    return
                }
                logger.debug("FileURL created: \(fileUrl)")

                // Retrieve the asset
                logger.debug("Retrieving asset...")
                let assetResult = await assetRetriever.retrieve(url: fileUrl)
                guard case .success(let asset) = assetResult else {
                    if case .failure(let error) = assetResult {
                        logger.error("Failed to retrieve asset: \(error)")
                        onError("Failed to retrieve asset: \(error)")
                    }
                    return
                }
                logger.debug("Asset retrieved successfully")

                // Open the publication using PublicationOpener
                logger.debug("Opening publication...")
                let publicationResult = await publicationOpener.open(
                    asset: asset,
                    allowUserInteraction: false
                )

                switch publicationResult {
                case .success(let publication):
                    logger.info("Publication opened successfully: \(publication.metadata.title ?? "Unknown")")
                    self.publication = publication
                    onSuccess()
                case .failure(let error):
                    logger.error("Failed to open publication: \(error)")
                    onError("Failed to open publication: \(error)")
                }
            }
        }
    }

    func closePublication() {
        logger.info("closePublication called")
        mediaOverlayPlayer?.release()
        mediaOverlayPlayer = nil
        navigatorViewController = nil
        publication = nil
    }

    func createReaderViewController(settings: EpubReaderSettings) -> UIViewController? {
        logger.info("createReaderViewController called")
        logger.debug("Settings - fontSize: \(settings.fontSize), fontFamily: \(settings.fontFamily)")
        logger.debug("Publication is \(self.publication == nil ? "nil" : "set")")

        guard let publication = self.publication else {
            logger.warning("createReaderViewController returning nil - publication is nil")
            return nil
        }
        logger.debug("Publication found: \(publication.metadata.title ?? "Unknown")")

        do {
            // Get the screen bounds to constrain the navigator
            let screenBounds = UIScreen.main.bounds
            logger.debug("Screen bounds: \(screenBounds.width)x\(screenBounds.height)")

            // Create initial preferences from settings
            let initialPreferences = settings.toEpubPreferences()
            logger.debug("Initial preferences created")

            // Create initial locator from settings if available
            var initialLocation: Locator? = nil
            if let position = settings.initialPosition,
               let href = AnyURL(legacyHREF: position.href) {
                logger.debug("Creating initial locator from position: href=\(position.href)")
                initialLocation = Locator(
                    href: href,
                    mediaType: MediaType(position.type) ?? .html,
                    title: position.title,
                    locations: Locator.Locations(
                        progression: position.progression?.doubleValue,
                        totalProgression: position.totalProgression?.doubleValue,
                        position: position.position.map {
                            Int($0.int32Value)
                        }
                    )
                )
            } else {
                logger.debug("No initial position provided")
            }

            logger.debug("Creating EPUBNavigatorViewController...")
            let navigator = try EPUBNavigatorViewController(
                publication: publication,
                initialLocation: initialLocation,
                config: EPUBNavigatorViewController.Configuration(
                    preferences: initialPreferences
                ),
                httpServer: httpServer
            )
            logger.info("EPUBNavigatorViewController created successfully")
            self.navigatorViewController = navigator

            // Set delegate to receive location change callbacks
            navigator.delegate = self
            logger.debug("Delegate set")

            // Force the navigator's view to use screen width
            navigator.view.frame = CGRect(
                x: 0,
                y: 0,
                width: screenBounds.width,
                height: screenBounds.height
            )
            navigator.view.clipsToBounds = true
            navigator.view.autoresizingMask = [.flexibleWidth, .flexibleHeight]
            logger.debug("Navigator view configured with frame: \(navigator.view.frame.width)x\(navigator.view.frame.height)")

            logger.info("createReaderViewController returning navigator successfully")
            return navigator
        } catch {
            logger.error("Failed to create EPUBNavigatorViewController: \(error)")
            return nil
        }
    }

    func goToNextPage() {
        Task { @MainActor in
            _ = await navigatorViewController?.goForward(options: .animated)
        }
    }

    func goToPreviousPage() {
        Task { @MainActor in
            _ = await navigatorViewController?.goBackward(options: .animated)
        }
    }

    func goToChapter(href: String) {
        Task { @MainActor in
            guard let publication = self.publication else {
                return
            }

            // Find the link with the matching href
            let link = publication.readingOrder.first {
                $0.href == href
            }

            if let link = link {
                _ = await navigatorViewController?.go(to: link)
            }
        }
    }

    func setSettings(settings: EpubReaderSettings) {
        Task { @MainActor in
            let preferences = settings.toEpubPreferences()
            navigatorViewController?.submitPreferences(preferences)
        }
    }

    func goToPosition(href: String, type: String, progression: KotlinDouble?, position: KotlinInt?) {
        Task { @MainActor in
            guard let url = AnyURL(legacyHREF: href) else {
                return
            }

            let locator = Locator(
                href: url,
                mediaType: MediaType(type) ?? .html,
                locations: Locator.Locations(
                    progression: progression?.doubleValue,
                    position: position.map {
                        Int($0.int32Value)
                    }
                )
            )
            _ = await navigatorViewController?.go(to: locator)
        }
    }

    func setOnPositionChangedCallback(callback: ((PositionLocator) -> Void)?) {
        self.onPositionChangedCallback = callback
    }

    // MARK: - Media Overlay Methods

    func hasMediaOverlays() -> Bool {
        guard let publication = self.publication else {
            return false
        }
        // Check if publication has SMIL resources
        // Use description to get the URL string to avoid SwiftSoup conflict
        let hasSmil = publication.resources.contains(where: { link in
            let hrefString: String = link.href.description
            return hrefString.hasSuffix(".smil")
        })
        return hasSmil
    }

    func initializeMediaOverlays(onReady: @escaping () -> Void) {
        guard let publication = self.publication else {
            onReady()
            return
        }

        let player = MediaOverlayPlayer(publication: publication)
        self.mediaOverlayPlayer = player

        // Set up callbacks
        player.onPlaybackStateChanged = { [weak self] state in
            self?.handlePlaybackStateChanged(state)
        }

        player.onLocatorChanged = { [weak self] locator in
            self?.handleLocatorChanged(locator)
        }

        Task {
            // Get initial chapter href for optimized lazy loading
            let initialChapterHref: String?
            if let currentLocator = self.navigatorViewController?.currentLocation,
               let chapterHref = currentLocator.href.relativeURL {
                self.currentChapterHref = chapterHref
                initialChapterHref = chapterHref.description
            } else {
                initialChapterHref = nil
            }

            // Initialize with lazy loading, passing initial chapter for optimization
            await player.initialize(initialChapterHref: initialChapterHref)

            // Prepare duration for the initial chapter
            if let chapterHref = self.currentChapterHref {
                await player.prepareChapterDuration(chapterHref: chapterHref)
            }

            onReady()
            self.onMediaPlayerReadyCallback?()
        }
    }

    func playAudio(initialPositionMs: KotlinLong?) {
        guard let player = mediaOverlayPlayer else {
            return
        }

        // Get current locator from navigator
        let currentLocator = navigatorViewController?.currentLocation
        // Extract the fragment ID from the locator (e.g., "chapter44.xhtml-sentence50")
        let fragmentId = currentLocator?.locations.fragments.first
        // Extract the progression (0.0 to 1.0) through the chapter
        let progression = currentLocator?.locations.progression

        // Get current chapter href from navigator and convert to RelativeURL
        if let currentHref = currentLocator?.href,
           let relativeHref = RelativeURL(string: currentHref.string) {
            player.play(chapterHref: relativeHref, initialFragmentId: fragmentId, initialProgression: progression, initialPositionMs: initialPositionMs?.int64Value)
        } else {
            player.play(chapterHref: nil, initialFragmentId: fragmentId, initialProgression: progression, initialPositionMs: initialPositionMs?.int64Value)
        }
    }

    func resumeAudio() {
        mediaOverlayPlayer?.resume()
    }

    func pauseAudio() {
        mediaOverlayPlayer?.pause()
    }

    func seekToAudioPosition(timestampMs: Int64) {
        mediaOverlayPlayer?.seekTo(positionMs: timestampMs)
    }

    func setPlaybackSpeed(speed: Float) {
        mediaOverlayPlayer?.setPlaybackSpeed(speed: speed)
    }

    func setOnPlaybackStateChangedCallback(callback: ((PlaybackState) -> Void)?) {
        self.onPlaybackStateChangedCallback = callback
    }

    func setOnMediaPlayerReadyCallback(callback: (() -> Void)?) {
        self.onMediaPlayerReadyCallback = callback
    }

    // MARK: - Private Media Overlay Helpers

    private func handlePlaybackStateChanged(_ state: MediaPlaybackState) {
        let playbackState = PlaybackState(
            isPlaying: state.isPlaying,
            currentPositionMs: state.currentPositionMs,
            durationMs: state.durationMs.map {
                KotlinLong(value: $0)
            }
        )
        onPlaybackStateChangedCallback?(playbackState)
    }

    private func handleLocatorChanged(_ locator: Locator) {
        applyHighlightDecoration(locator: locator)
    }

    private func applyHighlightDecoration(locator: Locator) {
        guard let navigator = navigatorViewController else {
            return
        }

        guard let fragmentId = locator.locations.fragments.first else {
            return
        }

        // Skip if same fragment
        guard fragmentId != currentHighlightId else {
            return
        }
        currentHighlightId = fragmentId

        // Create decoration for the current text fragment
        // Decoration.Id is a String typealias, group is also a String
        let decoration = Decoration(
            id: "media-overlay-highlight",
            locator: locator,
            style: .highlight(tint: .yellow, isActive: true)
        )

        // Apply decoration using Readium's Decoration API
        // EPUBNavigatorViewController conforms to DecorableNavigator
        navigator.apply(decorations: [decoration], in: "media-overlay")

        // Navigate to the locator to ensure the highlighted text is visible on screen
        // This is especially important when seeking audio - the text should follow
        Task {
            _ = await navigator.go(to: locator, options: .init(animated: false))
        }
    }
}

// MARK: - EPUBNavigatorDelegate

extension ReadiumEpubReaderBridge: EPUBNavigatorDelegate {
    func navigator(_ navigator: any Navigator, presentError error: NavigatorError) {
        print("Navigator error: \(error)")
    }

    func navigator(_ navigator: any Navigator, didFailToLoadResourceAt href: RelativeURL, withError error: ReadError) {
        print("Failed to load resource at \(href): \(error)")
    }

    func navigator(_ navigator: any Navigator, locationDidChange locator: Locator) {
        // Update chapter duration if chapter changed
        if let chapterHref = locator.href.relativeURL,
           chapterHref.removingFragment() != currentChapterHref?.removingFragment() {
            currentChapterHref = chapterHref
            Task {
                await mediaOverlayPlayer?.prepareChapterDuration(chapterHref: chapterHref)
            }
        }

        guard let callback = onPositionChangedCallback else {
            return
        }

        let positionLocator = PositionLocator(
            href: locator.href.string,
            type: locator.mediaType.string,
            title: locator.title,
            progression: locator.locations.progression.map {
                KotlinDouble(value: $0)
            },
            position: locator.locations.position.map {
                KotlinInt(value: Int32($0))
            },
            totalProgression: locator.locations.totalProgression.map {
                KotlinDouble(value: $0)
            }
        )
        callback(positionLocator)
    }
}

// MARK: - EpubReaderSettings Extension

extension EpubReaderSettings {
    /// Converts reader settings to Readium EPUBPreferences.
    /// This is used both for initial preferences and dynamic updates.
    /// Add new preference mappings here as needed.
    func toEpubPreferences() -> EPUBPreferences {
        return EPUBPreferences(
            fontSize: fontSize,
            scroll: scrollMode?.boolValue
            // Add more preferences here as needed:
            // fontFamily: fontFamily,
            // lineHeight: lineHeight,
            // etc.
        )
    }
}
