package com.retro99.reader.ui.reader

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.retro99.base.ui.IntentDispatcher
import com.retro99.reader.ui.navigator.BookController
import com.retro99.reader.ui.publication.PublicationState

@Composable
internal actual fun EpubReaderViewInternal(
    bookUuid: String,
    publicationState: PublicationState,
    intentDispatcher: IntentDispatcher<ReaderIntent>,
    bookController: BookController,
    modifier: Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text("EPUB reader not yet available on web")
    }
}
