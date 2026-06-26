import Foundation
import MediaPlayer
import UIKit

/// Manages the iOS Now Playing info center and remote command center.
/// Provides lock screen / Control Center playback controls and metadata display.
class NowPlayingInfo {

    static let shared = NowPlayingInfo()

    struct Media {
        let title: String
        let artist: String
        let artwork: UIImage?
    }

    struct Playback {
        let duration: Double?
        let elapsedTime: Double
        let rate: Double
    }

    var media: Media?
    var playback: Playback?

    private var hasRegisteredRemoteCommands = false

    private init() {
    }

    /// Callbacks invoked when the user interacts with lock screen / Control Center controls.
    var onPlay: (() -> Void)?
    var onPause: (() -> Void)?
    var onSkipForward: (() -> Void)?
    var onSkipBackward: (() -> Void)?
    var onNextChapter: (() -> Void)?
    var onPreviousChapter: (() -> Void)?

    func setMedia(_ media: Media, artwork: UIImage? = nil) {
        self.media = Media(
            title: media.title,
            artist: media.artist,
            artwork: artwork ?? media.artwork
        )
        updateNowPlayingInfoCenter()
        registerRemoteCommandsIfNeeded()
    }

    func setPlayback(_ playback: Playback) {
        self.playback = playback
        updateNowPlayingInfoCenter()
    }

    func clear() {
        media = nil
        playback = nil
        MPNowPlayingInfoCenter.default().nowPlayingInfo = nil
        unregisterRemoteCommands()
    }

    // MARK: - Private

    private func updateNowPlayingInfoCenter() {
        var info: [String: Any] = [:]

        if let media = media {
            info[MPMediaItemPropertyTitle] = media.title
            info[MPMediaItemPropertyArtist] = media.artist
            if let artwork = media.artwork {
                info[MPMediaItemPropertyArtwork] = MPMediaItemArtwork(boundsSize: artwork.size) { _ in
                    artwork
                }
            }
            info[MPNowPlayingInfoPropertyMediaType] = MPNowPlayingInfoMediaType.audio.rawValue
        }

        if let playback = playback {
            if let duration = playback.duration, duration > 0 {
                info[MPMediaItemPropertyPlaybackDuration] = duration
            }
            info[MPNowPlayingInfoPropertyElapsedPlaybackTime] = playback.elapsedTime
            info[MPNowPlayingInfoPropertyPlaybackRate] = playback.rate
        }

        MPNowPlayingInfoCenter.default().nowPlayingInfo = info.isEmpty ? nil : info
    }

    private func registerRemoteCommandsIfNeeded() {
        guard !hasRegisteredRemoteCommands else {
            return
        }
        hasRegisteredRemoteCommands = true

        let commandCenter = MPRemoteCommandCenter.shared()

        commandCenter.playCommand.isEnabled = true
        commandCenter.playCommand.addTarget { [weak self] _ in
            self?.onPlay?()
            return .success
        }

        commandCenter.pauseCommand.isEnabled = true
        commandCenter.pauseCommand.addTarget { [weak self] _ in
            self?.onPause?()
            return .success
        }

        commandCenter.skipForwardCommand.isEnabled = true
        commandCenter.skipForwardCommand.preferredIntervals = [10]
        commandCenter.skipForwardCommand.addTarget { [weak self] _ in
            self?.onSkipForward?()
            return .success
        }

        commandCenter.skipBackwardCommand.isEnabled = true
        commandCenter.skipBackwardCommand.preferredIntervals = [10]
        commandCenter.skipBackwardCommand.addTarget { [weak self] _ in
            self?.onSkipBackward?()
            return .success
        }

        commandCenter.nextTrackCommand.isEnabled = true
        commandCenter.nextTrackCommand.addTarget { [weak self] _ in
            self?.onNextChapter?()
            return .success
        }

        commandCenter.previousTrackCommand.isEnabled = true
        commandCenter.previousTrackCommand.addTarget { [weak self] _ in
            self?.onPreviousChapter?()
            return .success
        }
    }

    private func unregisterRemoteCommands() {
        let commandCenter = MPRemoteCommandCenter.shared()

        commandCenter.playCommand.removeTarget(nil)
        commandCenter.pauseCommand.removeTarget(nil)
        commandCenter.skipForwardCommand.removeTarget(nil)
        commandCenter.skipBackwardCommand.removeTarget(nil)
        commandCenter.nextTrackCommand.removeTarget(nil)
        commandCenter.previousTrackCommand.removeTarget(nil)

        commandCenter.playCommand.isEnabled = false
        commandCenter.pauseCommand.isEnabled = false
        commandCenter.skipForwardCommand.isEnabled = false
        commandCenter.skipBackwardCommand.isEnabled = false
        commandCenter.nextTrackCommand.isEnabled = false
        commandCenter.previousTrackCommand.isEnabled = false

        hasRegisteredRemoteCommands = false
    }
}
