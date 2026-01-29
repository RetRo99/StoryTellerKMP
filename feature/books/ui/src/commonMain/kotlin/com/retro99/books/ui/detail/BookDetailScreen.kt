package com.retro99.books.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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

@Composable
fun BookDetailScreen(
    bookUuid: String,
    modifier: Modifier = Modifier,
    viewModel: BookDetailViewModel = koinViewModel { parametersOf(bookUuid) },
) {
    BaseScreen(
        modifier = modifier,
        viewModel = viewModel,
    ) { viewState, intentDispatcher ->
        viewState.book?.let { book ->
            BookDetailScreenContent(
                book = book,
                intentDispatcher = intentDispatcher,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookDetailScreenContent(
    book: BookDomainModel,
    intentDispatcher: IntentDispatcher<BookDetailIntent>,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { intentDispatcher(BookDetailIntent.OnBackClicked) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            BookHeader(book = book)

            Spacer(modifier = Modifier.height(24.dp))

            MediaActionButtons(
                book = book,
                intentDispatcher = intentDispatcher,
            )

            if (book.description != null) {
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
                DescriptionSection(description = book.description!!)
            }

            if (book.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                TagsSection(
                    tags = book.tags.map { it.name },
                    onTagClick = { intentDispatcher(BookDetailIntent.OnTagClicked(it)) },
                )
            }

            if (book.series.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                SeriesSection(
                    series = book.series,
                    onSeriesClick = { intentDispatcher(BookDetailIntent.OnSeriesClicked(it)) },
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun BookHeader(
    book: BookDomainModel,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CoilImage(
            data = book.coverUrl,
            cacheKey = "${book.uuid}_detail",
            modifier = Modifier
                .width(180.dp)
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop,
            contentDescription = book.title,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = book.title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )

        book.subtitle?.let { subtitle ->
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }

        if (book.authors.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = book.authors.joinToString(", ") { it.name },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        book.rating?.let { rating ->
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = rating.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun MediaActionButtons(
    book: BookDomainModel,
    intentDispatcher: IntentDispatcher<BookDetailIntent>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
    ) {
        book.ebook?.let {
            MediaButton(
                icon = Icons.AutoMirrored.Outlined.MenuBook,
                label = stringResource(StringRes.books_media_ebook),
                onClick = { intentDispatcher(BookDetailIntent.OnReadEbookClicked) },
            )
        }
        book.audiobook?.let {
            MediaButton(
                icon = Icons.Outlined.Headphones,
                label = stringResource(StringRes.books_media_audio),
                onClick = { intentDispatcher(BookDetailIntent.OnPlayAudiobookClicked) },
            )
        }
    }
}

@Composable
private fun MediaButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label)
    }
}

@Composable
private fun DescriptionSection(
    description: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Description",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagsSection(
    tags: List<String>,
    onTagClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Tags",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            tags.forEach { tag ->
                SuggestionChip(
                    onClick = { onTagClick(tag) },
                    label = { Text(text = tag) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    ),
                )
            }
        }
    }
}

@Composable
private fun SeriesSection(
    series: List<com.retro99.books.domain.model.SeriesDomainModel>,
    onSeriesClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Series",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(8.dp))
        series.forEach { seriesItem ->
            SuggestionChip(
                onClick = { onSeriesClick(seriesItem.uuid) },
                label = {
                    Text(
                        text = buildString {
                            append(seriesItem.name)
                            seriesItem.position?.also { append(" #$it") }
                        },
                    )
                },
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    labelColor = MaterialTheme.colorScheme.onTertiaryContainer,
                ),
            )
        }
    }
}
