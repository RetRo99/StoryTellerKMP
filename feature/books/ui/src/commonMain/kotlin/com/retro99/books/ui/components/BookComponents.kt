package com.retro99.books.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.delete
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.outlined.RecordVoiceOver
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.retro99.base.ui.compose.CoilImage
import com.retro99.books.ui.model.BookUiModel
import com.retro99.translations.StringRes
import org.jetbrains.compose.resources.stringResource
import resources.translations.books_media_audio
import resources.translations.books_media_ebook
import resources.translations.books_media_readaloud
import resources.translations.books_search_clear
import resources.translations.books_search_placeholder

/**
 * A reusable book item card component that displays book information in a card layout.
 *
 * @param book The book data to display
 * @param isFavorite Whether the book is marked as favorite
 * @param onClick Callback when the card is clicked
 * @param onFavoriteClick Callback when the favorite button is clicked
 * @param modifier Modifier for the card
 * @param headerContent Optional composable content to display above the title (e.g., series position)
 * @param subtitleContent Optional composable content to display below the author (e.g., series info)
 */
@Composable
fun BookItemCard(
    book: BookUiModel,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier,
    headerContent: @Composable (() -> Unit)? = null,
    subtitleContent: @Composable (() -> Unit)? = null,
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

            Column(modifier = Modifier.weight(1f)) {
                headerContent?.invoke()

                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                if (book.authors.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = book.authors.joinToString(", "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                subtitleContent?.invoke()

                Spacer(modifier = Modifier.height(12.dp))

                MediaTypeRow(book = book)
            }

            IconButton(onClick = onFavoriteClick) {
                Icon(
                    imageVector = if (isFavorite) {
                        Icons.Filled.Favorite
                    } else {
                        Icons.Outlined.FavoriteBorder
                    },
                    contentDescription = null,
                    tint = if (isFavorite) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }
    }
}

@Composable
fun MediaTypeRow(
    book: BookUiModel,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        book.statusName?.let { status ->
            StatusChip(status = status)
        }

        if (book.hasEbook) {
            MediaTypeIndicator(
                icon = Icons.AutoMirrored.Outlined.MenuBook,
                label = stringResource(StringRes.books_media_ebook),
            )
        }

        if (book.hasAudiobook) {
            MediaTypeIndicator(
                icon = Icons.Outlined.Headphones,
                label = stringResource(StringRes.books_media_audio),
            )
        }

        if (book.hasReadaloud) {
            MediaTypeIndicator(
                icon = Icons.Outlined.RecordVoiceOver,
                label = stringResource(StringRes.books_media_readaloud),
            )
        }
    }
}

@Composable
fun StatusChip(
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
fun MediaTypeIndicator(
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

/**
 * A reusable search bar component for filtering books.
 *
 * @param searchFieldState The text field state for the search input
 * @param isVisible Whether the search bar is currently visible (used for focus management)
 * @param modifier Modifier for the search bar
 */
@Composable
fun BookSearchBar(
    searchFieldState: TextFieldState,
    isVisible: Boolean,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            focusRequester.requestFocus()
        }
    }

    OutlinedTextField(
        state = searchFieldState,
        modifier = modifier.focusRequester(focusRequester),
        placeholder = { Text(stringResource(StringRes.books_search_placeholder)) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
            )
        },
        trailingIcon = {
            if (searchFieldState.text.isNotEmpty()) {
                IconButton(
                    onClick = {
                        searchFieldState.edit { delete(0, length) }
                    },
                ) {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = stringResource(StringRes.books_search_clear),
                    )
                }
            }
        },
        lineLimits = TextFieldLineLimits.SingleLine,
        shape = RoundedCornerShape(12.dp),
    )
}

