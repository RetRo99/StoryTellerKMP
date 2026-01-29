package com.retro99.books.ui.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.outlined.RecordVoiceOver
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.retro99.base.ui.BaseScreen
import com.retro99.base.ui.IntentDispatcher
import com.retro99.base.ui.compose.CoilImage
import com.retro99.books.domain.model.BookDomainModel
import com.retro99.translations.StringRes
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import resources.translations.books_media_audio
import resources.translations.books_media_ebook
import resources.translations.books_media_readaloud
import resources.translations.books_series_with_position

@Composable
fun BooksListScreen(
    onNavigateToBookDetail: (bookUuid: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BooksViewModel = koinViewModel { parametersOf(onNavigateToBookDetail) },
) {
    BaseScreen(
        modifier = modifier,
        viewModel = viewModel,
    ) { viewState, intentDispatcher ->
        BooksListScreenContent(
            viewState = viewState,
            intentDispatcher = intentDispatcher,
            modifier = modifier,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BooksListScreenContent(
    viewState: BooksListViewState,
    intentDispatcher: IntentDispatcher<BooksListIntent>,
    modifier: Modifier = Modifier,
) {
    PullToRefreshBox(
        isRefreshing = viewState.isRefreshing,
        onRefresh = { intentDispatcher(BooksListIntent.OnRefresh) },
        modifier = modifier.fillMaxSize(),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(
                items = viewState.books,
                key = { it.uuid },
            ) { book ->
                BookItem(
                    book = book,
                    onClick = {
                        intentDispatcher(BooksListIntent.OnBookClicked(book))
                    },
                )
            }
        }
    }
}

@Composable
private fun BookItem(
    book: BookDomainModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            CoilImage(
                data = book.coverUrl,
                cacheKey = book.uuid,
                modifier = Modifier
                    .size(width = 80.dp, height = 120.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                contentDescription = book.title,
            )

            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                if (book.authors.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = book.authors.joinToString(", ") { it.name },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                if (book.series.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    val seriesInfo = book.series.first()
                    val seriesText = if (seriesInfo.position != null) {
                        stringResource(
                            StringRes.books_series_with_position,
                            seriesInfo.name,
                            seriesInfo.position!!,
                        )
                    } else {
                        seriesInfo.name
                    }
                    Text(
                        text = seriesText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    book.status?.let { status ->
                        StatusChip(status = status.name)
                    }

                    if (book.ebook != null) {
                        MediaTypeIndicator(
                            icon = Icons.AutoMirrored.Outlined.MenuBook,
                            label = stringResource(StringRes.books_media_ebook),
                        )
                    }

                    if (book.audiobook != null) {
                        MediaTypeIndicator(
                            icon = Icons.Outlined.Headphones,
                            label = stringResource(StringRes.books_media_audio),
                        )
                    }
                    if (book.readaloud != null) {
                        MediaTypeIndicator(
                            icon = Icons.Outlined.RecordVoiceOver,
                            label = stringResource(StringRes.books_media_readaloud),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusChip(
    status: String,
    modifier: Modifier = Modifier,
) {
    SuggestionChip(
        onClick = { },
        label = {
            Text(
                text = status,
                style = MaterialTheme.typography.labelSmall,
            )
        },
        modifier = modifier,
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ),
    )
}

@Composable
private fun MediaTypeIndicator(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
