package com.retro99.base.ui.sharing

import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication

/**
 * iOS implementation of FileSharer.
 * Uses UIActivityViewController to share files.
 */
class IosFileSharer : FileSharer {

    override fun shareFile(filePath: String, mimeType: String, title: String?) {
        try {
            val fileManager = NSFileManager.defaultManager
            if (!fileManager.fileExistsAtPath(filePath)) {
                return
            }

            val fileUrl = NSURL.fileURLWithPath(filePath)

            val activityViewController = UIActivityViewController(
                activityItems = listOf(fileUrl),
                applicationActivities = null,
            )

            // Get the root view controller to present from
            val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController
            rootViewController?.presentViewController(
                activityViewController,
                animated = true,
                completion = null,
            )
        } catch (e: Exception) {
            // Silently fail - sharing is not critical
        }
    }
}

