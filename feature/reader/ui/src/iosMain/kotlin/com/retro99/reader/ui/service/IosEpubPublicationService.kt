package com.retro99.reader.ui.service

import com.github.michaelbull.result.Ok
import com.retro99.analytics.api.Analytics
import com.retro99.base.result.AppResult
import com.retro99.books.domain.model.BookType
import com.retro99.reader.ui.bridge.EpubReaderBridgeRegistry
import com.retro99.reader.ui.publication.EpubPublication
import org.koin.core.annotation.Single
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Single(binds = [EpubPublicationService::class])
class IosEpubPublicationService(
    analytics: Analytics,
) : BaseEpubPublicationService(analytics) {

    private var currentBookUuid: String? = null

    val bridge get() = EpubReaderBridgeRegistry.getBridge()

    override suspend fun openPublication(
        filePath: String,
        serverId: String,
        bookUuid: String,
        bookType: BookType,
    ): AppResult<EpubPublication> {
        val currentBridge = bridge
        if (currentBridge == null) {
            return createError("EPUB reader bridge not registered")
        }

        if (currentBookUuid == bookUuid) {
            return Ok(EpubPublication(currentBridge, serverId, bookUuid, bookType))
        }

        currentBookUuid = bookUuid
        currentBridge.closePublication()

        return suspendCoroutine { continuation ->
            currentBridge.openPublication(
                filePath = filePath,
                onSuccess = {
                    val publication = EpubPublication(
                        currentBridge,
                        serverId,
                        bookUuid,
                        bookType,
                    )
                    continuation.resume(Ok(publication))
                },
                onError = { errorMessage ->
                    currentBookUuid = null
                    continuation.resume(createError(errorMessage))
                },
            )
        }
    }
}
