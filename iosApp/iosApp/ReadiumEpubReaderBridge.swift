import Foundation
import UIKit
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

    func createReaderViewController() -> UIViewController? {
        guard let publication = self.publication else {
            return nil
        }

        do {
            // Get the screen bounds to constrain the navigator
            let screenBounds = UIScreen.main.bounds

            let navigator = try EPUBNavigatorViewController(
                publication: publication,
                initialLocation: nil,
                config: EPUBNavigatorViewController.Configuration(),
                httpServer: httpServer
            )
            self.navigatorViewController = navigator

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
}

