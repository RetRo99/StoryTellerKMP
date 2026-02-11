package com.retro99.reader.data.download

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import co.touchlab.kermit.Logger
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.retro99.reader.data.source.EbookFileDownloader
import com.retro99.reader.domain.model.BookType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

/**
 * Foreground service for downloading ebooks.
 *
 * This service runs downloads in the foreground with a notification,
 * ensuring downloads continue even when the app is killed.
 */
class DownloadForegroundService : Service() {

    private val fileDownloader: EbookFileDownloader by inject()
    private val downloadStateHolder: DownloadStateHolder by inject()
    private val logger = Logger.withTag("DownloadService")

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val activeJobs = mutableMapOf<String, Job>()
    private val activeDownloadTitles = mutableMapOf<String, String>()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_DOWNLOAD -> {
                val bookUuid = intent.getStringExtra(EXTRA_BOOK_UUID) ?: return START_NOT_STICKY
                val bookTypeValue =
                    intent.getStringExtra(EXTRA_BOOK_TYPE) ?: return START_NOT_STICKY
                val filePath = intent.getStringExtra(EXTRA_FILE_PATH) ?: return START_NOT_STICKY
                val bookTitle = intent.getStringExtra(EXTRA_BOOK_TITLE) ?: "Book"
                val bookType = BookType.entries.find { it.value == bookTypeValue }
                    ?: return START_NOT_STICKY

                startForegroundWithNotification(bookTitle)
                startDownload(bookUuid, bookType, filePath, bookTitle)
            }

            ACTION_CANCEL_DOWNLOAD -> {
                val bookUuid = intent.getStringExtra(EXTRA_BOOK_UUID) ?: return START_NOT_STICKY
                val bookTypeValue =
                    intent.getStringExtra(EXTRA_BOOK_TYPE) ?: return START_NOT_STICKY
                val bookType = BookType.entries.find { it.value == bookTypeValue }
                    ?: return START_NOT_STICKY

                cancelDownload(bookUuid, bookType)
            }
        }

        return START_NOT_STICKY
    }

    private fun startForegroundWithNotification(bookTitle: String) {
        val notification = createNotification(bookTitle, 0)
        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            notification,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            } else {
                0
            },
        )
    }

    private fun startDownload(
        bookUuid: String,
        bookType: BookType,
        filePath: String,
        bookTitle: String,
    ) {
        val key = "$bookUuid:${bookType.value}"

        // Cancel existing job if any
        activeJobs[key]?.cancel()

        // Track the title for this download
        activeDownloadTitles[key] = bookTitle

        val job = serviceScope.launch {
            downloadStateHolder.updateProgress(bookUuid, bookType, null)

            fileDownloader.downloadEbookWithProgress(
                ebookFilePath = filePath,
                bookUuid = bookUuid,
                bookType = bookType,
                onProgress = { bytesDownloaded, totalBytes ->
                    val progress = if (totalBytes != null && totalBytes > 0) {
                        (bytesDownloaded.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
                    } else {
                        null
                    }
                    downloadStateHolder.updateProgress(bookUuid, bookType, progress)
                    updateNotification(bookTitle, progress)
                },
            ).onSuccess {
                logger.i { "Download completed: $bookUuid" }
                downloadStateHolder.markCached(bookUuid, bookType)
            }.onFailure { error ->
                logger.e { "Download failed: $bookUuid - $error" }
                downloadStateHolder.markFailed(bookUuid, bookType, error)
            }

            activeJobs.remove(key)
            activeDownloadTitles.remove(key)
            stopSelfIfNoActiveDownloads()
        }

        activeJobs[key] = job
    }

    private fun cancelDownload(bookUuid: String, bookType: BookType) {
        val key = "$bookUuid:${bookType.value}"
        activeJobs[key]?.cancel()
        activeJobs.remove(key)
        activeDownloadTitles.remove(key)
        downloadStateHolder.markIdle(bookUuid, bookType)
        stopSelfIfNoActiveDownloads()
    }

    private fun stopSelfIfNoActiveDownloads() {
        if (activeJobs.isEmpty()) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun updateNotification(bookTitle: String, progress: Float?) {
        val notification = createNotification(
            bookTitle,
            ((progress ?: 0f) * 100).toInt(),
        )
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotification(bookTitle: String, progress: Int): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Downloading: $bookTitle")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setProgress(100, progress, progress == 0)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Book Downloads",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "Shows download progress for books"
        }
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val CHANNEL_ID = "book_downloads"
        private const val NOTIFICATION_ID = 2001

        const val ACTION_START_DOWNLOAD = "com.retro99.reader.START_DOWNLOAD"
        const val ACTION_CANCEL_DOWNLOAD = "com.retro99.reader.CANCEL_DOWNLOAD"
        const val EXTRA_BOOK_UUID = "book_uuid"
        const val EXTRA_BOOK_TYPE = "book_type"
        const val EXTRA_FILE_PATH = "file_path"
        const val EXTRA_BOOK_TITLE = "book_title"

        fun createStartIntent(
            context: Context,
            bookUuid: String,
            bookType: BookType,
            filePath: String,
            bookTitle: String,
        ): Intent {
            return Intent(context, DownloadForegroundService::class.java).apply {
                action = ACTION_START_DOWNLOAD
                putExtra(EXTRA_BOOK_UUID, bookUuid)
                putExtra(EXTRA_BOOK_TYPE, bookType.value)
                putExtra(EXTRA_FILE_PATH, filePath)
                putExtra(EXTRA_BOOK_TITLE, bookTitle)
            }
        }

        fun createCancelIntent(
            context: Context,
            bookUuid: String,
            bookType: BookType,
        ): Intent {
            return Intent(context, DownloadForegroundService::class.java).apply {
                action = ACTION_CANCEL_DOWNLOAD
                putExtra(EXTRA_BOOK_UUID, bookUuid)
                putExtra(EXTRA_BOOK_TYPE, bookType.value)
            }
        }
    }
}

