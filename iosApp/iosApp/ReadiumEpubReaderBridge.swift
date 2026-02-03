import Foundation
import UIKit
import WebKit
import ComposeApp
import ReadiumShared
import ReadiumStreamer
import ReadiumNavigator
import ReadiumAdapterGCDWebServer

/// Swift implementation of the EPUB reader bridge.
/// This class wraps Readium iOS SDK and exposes it to Kotlin.
class ReadiumEpubReaderBridge: EpubReaderBridge {

    private var publication: Publication?
    private var navigatorViewController: EPUBNavigatorViewController?
    private var onPositionChangedCallback: ((PositionLocator) -> Void)?

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
    }

    func openPublication(
        filePath: String,
        onSuccess: @escaping () -> Void,
        onError: @escaping (String) -> Void
    ) {
        Task { @MainActor in
            do {
                // Convert file path to Foundation URL, then to Readium's FileURL
                let foundationUrl = URL(fileURLWithPath: filePath)
                guard let fileUrl = FileURL(url: foundationUrl) else {
                    onError("Invalid file path: \(filePath)")
                    return
                }

                // Retrieve the asset
                let assetResult = await assetRetriever.retrieve(url: fileUrl)
                guard case .success(let asset) = assetResult else {
                    if case .failure(let error) = assetResult {
                        onError("Failed to retrieve asset: \(error)")
                    }
                    return
                }

                // Open the publication using PublicationOpener
                let publicationResult = await publicationOpener.open(
                    asset: asset,
                    allowUserInteraction: false
                )

                switch publicationResult {
                case .success(let publication):
                    self.publication = publication
                    onSuccess()
                case .failure(let error):
                    onError("Failed to open publication: \(error)")
                }
            }
        }
    }

    func closePublication() {
        navigatorViewController = nil
        publication = nil
    }

    func createReaderViewController(settings: EpubReaderSettings) -> UIViewController? {
        guard let publication = self.publication else {
            return nil
        }

        do {
            // Get the screen bounds to constrain the navigator
            let screenBounds = UIScreen.main.bounds

            // Create initial preferences from settings
            let initialPreferences = settings.toEpubPreferences()
            print("Creating EPUB navigator with initial fontSize: \(settings.fontSize)")

            // Create initial locator from settings if available
            var initialLocation: Locator? = nil
            if let locator = settings.initialLocator,
               let href = AnyURL(legacyHREF: locator.href) {
                initialLocation = Locator(
                    href: href,
                    mediaType: MediaType(locator.type) ?? .html,
                    title: locator.title,
                    locations: Locator.Locations(
                        progression: locator.progression?.doubleValue,
                        totalProgression: locator.totalProgression?.doubleValue,
                        position: locator.position.map {
                            Int($0.int32Value)
                        }
                    )
                )
                print("Using initial locator: href=\(locator.href), progression=\(String(describing: locator.progression))")
            }

            let navigator = try EPUBNavigatorViewController(
                publication: publication,
                initialLocation: initialLocation,
                config: EPUBNavigatorViewController.Configuration(
                    preferences: initialPreferences
                ),
                httpServer: httpServer
            )
            self.navigatorViewController = navigator

            // Set delegate to receive location change callbacks
            navigator.delegate = self

            // Force the navigator's view to use screen width
            navigator.view.frame = CGRect(
                x: 0,
                y: 0,
                width: screenBounds.width,
                height: screenBounds.height
            )
            navigator.view.clipsToBounds = true
            navigator.view.autoresizingMask = [.flexibleWidth, .flexibleHeight]

            return navigator
        } catch {
            print("Failed to create EPUBNavigatorViewController: \(error)")
            return nil
        }
    }

    func goToNextPage() {
        Task { @MainActor in
            _ = await navigatorViewController?.goForward()
        }
    }

    func goToPreviousPage() {
        Task { @MainActor in
            _ = await navigatorViewController?.goBackward()
        }
    }

    func goToChapter(href: String) {
        Task { @MainActor in
            guard let publication = self.publication else {
                print("Cannot navigate to chapter: no publication loaded")
                return
            }

            // Find the link with the matching href
            let link = publication.readingOrder.first {
                $0.href == href
            }

            if let link = link {
                _ = await navigatorViewController?.go(to: link)
            } else {
                print("Chapter with href '\(href)' not found in reading order")
            }
        }
    }

    func setSettings(settings: EpubReaderSettings) {
        Task { @MainActor in
            print("Setting font typeScale to: \(settings.fontSize)")
            let preferences = settings.toEpubPreferences()
            navigatorViewController?.submitPreferences(preferences)
        }
    }

    func setOnPositionChangedCallback(callback: ((PositionLocator) -> Void)?) {
        self.onPositionChangedCallback = callback
    }
}

// MARK: - NavigatorDelegate

extension ReadiumEpubReaderBridge: NavigatorDelegate {
    func navigator(_ navigator: any Navigator, didFailToLoadResourceAt href: RelativeURL, withError error: any Error) {
        print("Failed to load resource at \(href): \(error)")
    }

    func navigator(_ navigator: any Navigator, locationDidChange locator: Locator) {
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
            scroll: scrollMode
            // Add more preferences here as needed:
            // fontFamily: fontFamily,
            // lineHeight: lineHeight,
            // etc.
        )
    }
}
