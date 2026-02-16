package com.retro99.settings.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.retro99.base.ui.BaseScreen
import com.retro99.base.ui.IntentDispatcher
import com.retro99.reader.domain.model.ChapterProgressDisplayMode
import com.retro99.reader.domain.model.HighlightColor
import com.retro99.reader.domain.model.HighlightStyle
import com.retro99.reader.domain.model.ProgressBarPosition
import com.retro99.reader.domain.model.ProgressIndicatorMode
import com.retro99.settings.ui.model.FontFamilyUiModel
import com.retro99.settings.ui.model.ReaderTextAlignUiModel
import com.retro99.settings.ui.model.ReaderThemeUiModel
import com.retro99.translations.StringRes
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import resources.translations.settings_chapter_progress
import resources.translations.settings_chapter_progress_fixed
import resources.translations.settings_chapter_progress_none
import resources.translations.settings_chapter_progress_percentage
import resources.translations.settings_chapter_progress_relative
import resources.translations.settings_font_family
import resources.translations.settings_font_family_accessible_dfa
import resources.translations.settings_font_family_cursive
import resources.translations.settings_font_family_default
import resources.translations.settings_font_family_fantasy
import resources.translations.settings_font_family_ia_writer_duospace
import resources.translations.settings_font_family_monospace
import resources.translations.settings_font_family_open_dyslexic
import resources.translations.settings_font_family_sans_serif
import resources.translations.settings_font_family_serif
import resources.translations.settings_font_size
import resources.translations.settings_fullscreen_mode
import resources.translations.settings_fullscreen_mode_description
import resources.translations.settings_highlight_color
import resources.translations.settings_highlight_style
import resources.translations.settings_highlight_style_highlight
import resources.translations.settings_highlight_style_highlight_underline
import resources.translations.settings_highlight_style_underline
import resources.translations.settings_line_height
import resources.translations.settings_margin_horizontal
import resources.translations.settings_margin_vertical
import resources.translations.settings_progress_bar
import resources.translations.settings_progress_bar_always
import resources.translations.settings_progress_bar_never
import resources.translations.settings_progress_bar_on_tap
import resources.translations.settings_progress_bar_position
import resources.translations.settings_progress_bar_position_bottom
import resources.translations.settings_progress_bar_position_top
import resources.translations.settings_progress_indicator
import resources.translations.settings_progress_indicator_book
import resources.translations.settings_progress_indicator_chapter
import resources.translations.settings_progress_indicator_none
import resources.translations.settings_publisher_styles
import resources.translations.settings_publisher_styles_description
import resources.translations.settings_scroll_mode
import resources.translations.settings_scroll_mode_auto
import resources.translations.settings_scroll_mode_paginated
import resources.translations.settings_scroll_mode_scroll
import resources.translations.settings_section_appearance
import resources.translations.settings_section_appearance_description
import resources.translations.settings_section_layout
import resources.translations.settings_section_layout_description
import resources.translations.settings_section_readaloud
import resources.translations.settings_section_readaloud_description
import resources.translations.settings_section_typography
import resources.translations.settings_section_typography_description
import resources.translations.settings_show_current_time
import resources.translations.settings_show_current_time_description
import resources.translations.settings_show_reading_time
import resources.translations.settings_show_reading_time_description
import resources.translations.settings_show_total_progress
import resources.translations.settings_text_align
import resources.translations.settings_text_align_center
import resources.translations.settings_text_align_end
import resources.translations.settings_text_align_justify
import resources.translations.settings_text_align_start
import resources.translations.settings_theme
import resources.translations.settings_theme_dark
import resources.translations.settings_theme_light
import resources.translations.settings_theme_sepia
import resources.translations.settings_theme_system
import resources.translations.settings_title

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    BaseScreen(
        modifier = modifier,
        viewModel = viewModel,
    ) { viewState, intentDispatcher ->
        SettingsScreenContent(
            viewState = viewState,
            intentDispatcher = intentDispatcher,
            modifier = modifier,
        )
    }
}

@Composable
private fun SettingsScreenContent(
    viewState: SettingsViewState,
    intentDispatcher: IntentDispatcher<SettingsIntent>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(StringRes.settings_title),
            style = MaterialTheme.typography.headlineMedium,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Appearance Section - Theme & Font Size
        ExpandableSettingsSection(
            title = stringResource(StringRes.settings_section_appearance),
            description = stringResource(StringRes.settings_section_appearance_description),
            isExpanded = viewState.isSectionExpanded(SettingsSection.APPEARANCE),
            onToggle = { intentDispatcher(SettingsIntent.OnSectionToggled(SettingsSection.APPEARANCE)) },
        ) {
            ThemeSelector(
                selectedTheme = viewState.theme,
                onThemeSelected = { intentDispatcher(SettingsIntent.OnThemeChanged(it)) },
            )

            Spacer(modifier = Modifier.height(16.dp))

            SettingsSlider(
                label = stringResource(StringRes.settings_font_size),
                value = viewState.fontSize.toFloat(),
                valueRange = 0.5f..3.0f,
                onValueChange = {
                    intentDispatcher(SettingsIntent.OnFontSizeChanged(it.toDouble()))
                },
                valueDisplay = { "${(it * 100).toInt()}%" },
            )

            Spacer(modifier = Modifier.height(16.dp))

            FontFamilySelector(
                selectedFontFamily = viewState.fontFamily,
                onFontFamilySelected = {
                    intentDispatcher(SettingsIntent.OnFontFamilyChanged(it))
                },
            )
        }

        // Typography Section - Publisher Styles, Line Height, Text Alignment
        ExpandableSettingsSection(
            title = stringResource(StringRes.settings_section_typography),
            description = stringResource(StringRes.settings_section_typography_description),
            isExpanded = viewState.isSectionExpanded(SettingsSection.TYPOGRAPHY),
            onToggle = { intentDispatcher(SettingsIntent.OnSectionToggled(SettingsSection.TYPOGRAPHY)) },
        ) {
            SettingsSwitch(
                label = stringResource(StringRes.settings_publisher_styles),
                description = stringResource(StringRes.settings_publisher_styles_description),
                checked = viewState.publisherStyles,
                onCheckedChange = { intentDispatcher(SettingsIntent.OnPublisherStylesChanged(it)) },
            )

            AnimatedVisibility(
                visible = !viewState.publisherStyles,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))

                    SettingsSlider(
                        label = stringResource(StringRes.settings_line_height),
                        value = viewState.lineHeight,
                        valueRange = 1.0f..2.5f,
                        onValueChange = {
                            intentDispatcher(SettingsIntent.OnLineHeightChanged(it))
                        },
                        valueDisplay = { ((it * 10).toInt() / 10.0).toString() },
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    TextAlignSelector(
                        selectedAlign = viewState.textAlign,
                        onAlignSelected = {
                            intentDispatcher(SettingsIntent.OnTextAlignChanged(it))
                        },
                    )
                }
            }
        }

        // Layout Section - Margins & Reading Mode
        ExpandableSettingsSection(
            title = stringResource(StringRes.settings_section_layout),
            description = stringResource(StringRes.settings_section_layout_description),
            isExpanded = viewState.isSectionExpanded(SettingsSection.LAYOUT),
            onToggle = { intentDispatcher(SettingsIntent.OnSectionToggled(SettingsSection.LAYOUT)) },
        ) {
            SettingsSlider(
                label = stringResource(StringRes.settings_margin_horizontal),
                value = viewState.marginHorizontal.toFloat(),
                valueRange = 0f..48f,
                onValueChange = {
                    intentDispatcher(SettingsIntent.OnMarginHorizontalChanged(it.toInt()))
                },
                valueDisplay = { "${it.toInt()}dp" },
            )

            Spacer(modifier = Modifier.height(16.dp))

            SettingsSlider(
                label = stringResource(StringRes.settings_margin_vertical),
                value = viewState.marginVertical.toFloat(),
                valueRange = 0f..64f,
                onValueChange = {
                    intentDispatcher(SettingsIntent.OnMarginVerticalChanged(it.toInt()))
                },
                valueDisplay = { "${it.toInt()}dp" },
            )

            Spacer(modifier = Modifier.height(16.dp))

            ScrollModeSelector(
                selectedMode = viewState.scrollMode,
                onModeSelected = { intentDispatcher(SettingsIntent.OnScrollModeChanged(it)) },
            )

            Spacer(modifier = Modifier.height(16.dp))

            ProgressBarModeSelector(
                selectedMode = viewState.showProgressBar,
                onModeSelected = { intentDispatcher(SettingsIntent.OnShowProgressBarChanged(it)) },
            )

            Spacer(modifier = Modifier.height(16.dp))

            ProgressBarPositionSelector(
                selectedPosition = viewState.progressBarPosition,
                onPositionSelected = {
                    intentDispatcher(SettingsIntent.OnProgressBarPositionChanged(it))
                },
            )

            Spacer(modifier = Modifier.height(16.dp))

            ChapterProgressDisplayModeSelector(
                selectedMode = viewState.chapterProgressDisplayMode,
                onModeSelected = {
                    intentDispatcher(SettingsIntent.OnChapterProgressDisplayModeChanged(it))
                },
            )

            Spacer(modifier = Modifier.height(16.dp))

            ShowTotalProgressSwitch(
                isEnabled = viewState.showTotalProgress,
                onToggle = { intentDispatcher(SettingsIntent.OnShowTotalProgressChanged(it)) },
            )

            Spacer(modifier = Modifier.height(16.dp))

            ProgressIndicatorModeSelector(
                selectedMode = viewState.progressIndicatorMode,
                onModeSelected = {
                    intentDispatcher(SettingsIntent.OnProgressIndicatorModeChanged(it))
                },
            )

            Spacer(modifier = Modifier.height(16.dp))

            FullscreenModeSwitch(
                isEnabled = viewState.fullscreenMode,
                onToggle = { intentDispatcher(SettingsIntent.OnFullscreenModeChanged(it)) },
            )

            Spacer(modifier = Modifier.height(16.dp))

            ShowCurrentTimeSwitch(
                isEnabled = viewState.showCurrentTime,
                onToggle = { intentDispatcher(SettingsIntent.OnShowCurrentTimeChanged(it)) },
            )

            Spacer(modifier = Modifier.height(16.dp))

            ShowReadingTimeSwitch(
                isEnabled = viewState.showReadingTime,
                onToggle = { intentDispatcher(SettingsIntent.OnShowReadingTimeChanged(it)) },
            )
        }

        // ReadAloud Section - Highlight Style and Color
        ExpandableSettingsSection(
            title = stringResource(StringRes.settings_section_readaloud),
            description = stringResource(StringRes.settings_section_readaloud_description),
            isExpanded = viewState.isSectionExpanded(SettingsSection.READALOUD),
            onToggle = { intentDispatcher(SettingsIntent.OnSectionToggled(SettingsSection.READALOUD)) },
        ) {
            HighlightStyleSelector(
                selectedStyle = viewState.highlightStyle,
                onStyleSelected = { intentDispatcher(SettingsIntent.OnHighlightStyleChanged(it)) },
            )
            // Only show color selector when highlight is enabled (not underline-only)
            if (viewState.highlightStyle != HighlightStyle.UNDERLINE) {
                Spacer(modifier = Modifier.height(16.dp))
                HighlightColorSelector(
                    selectedColor = viewState.highlightColor,
                    onColorSelected = { intentDispatcher(SettingsIntent.OnHighlightColorChanged(it)) },
                )
            }
        }
    }
}

@Composable
private fun HighlightColorSelector(
    selectedColor: HighlightColor,
    onColorSelected: (HighlightColor) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(StringRes.settings_highlight_color),
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            HighlightColor.entries.forEach { color ->
                HighlightColorSwatch(
                    color = color,
                    isSelected = color == selectedColor,
                    onClick = { onColorSelected(color) },
                )
            }
        }
    }
}

@Composable
private fun HighlightColorSwatch(
    color: HighlightColor,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val swatchColor = Color(color.toArgb())
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(swatchColor)
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outline
                },
                shape = CircleShape,
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

/**
 * Returns the ARGB color value for the highlight color.
 */
private fun HighlightColor.toArgb(): Int = when (this) {
    HighlightColor.YELLOW -> 0x80FFEB3B.toInt()
    HighlightColor.GREEN -> 0x8081C784.toInt()
    HighlightColor.BLUE -> 0x8064B5F6.toInt()
    HighlightColor.PINK -> 0x80F48FB1.toInt()
    HighlightColor.ORANGE -> 0x80FFB74D.toInt()
}

@Composable
private fun HighlightStyleSelector(
    selectedStyle: HighlightStyle,
    onStyleSelected: (HighlightStyle) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(StringRes.settings_highlight_style),
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            HighlightStyle.entries.forEach { style ->
                HighlightStyleChip(
                    style = style,
                    isSelected = style == selectedStyle,
                    onClick = { onStyleSelected(style) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun HighlightStyleChip(
    style: HighlightStyle,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = when (style) {
        HighlightStyle.HIGHLIGHT -> stringResource(StringRes.settings_highlight_style_highlight)
        HighlightStyle.HIGHLIGHT_UNDERLINE -> stringResource(
            StringRes.settings_highlight_style_highlight_underline,
        )

        HighlightStyle.UNDERLINE -> stringResource(StringRes.settings_highlight_style_underline)
    }

    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
            )
        },
        modifier = modifier,
    )
}

@Composable
private fun ExpandableSettingsSection(
    title: String,
    description: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "arrow_rotation",
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    )
                }
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.rotate(rotationAngle),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun ThemeSelector(
    selectedTheme: ReaderThemeUiModel,
    onThemeSelected: (ReaderThemeUiModel) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(StringRes.settings_theme),
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.height(8.dp))
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            ReaderThemeUiModel.entries.forEachIndexed { index, theme ->
                SegmentedButton(
                    selected = theme == selectedTheme,
                    onClick = { onThemeSelected(theme) },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = ReaderThemeUiModel.entries.size,
                    ),
                ) {
                    Text(text = theme.toDisplayString())
                }
            }
        }
    }
}

@Composable
private fun FontFamilySelector(
    selectedFontFamily: FontFamilyUiModel,
    onFontFamilySelected: (FontFamilyUiModel) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(StringRes.settings_font_family),
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.height(8.dp))
        // Use FlowRow-like layout with FilterChips for better UX with many options
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // First row: Default, Serif, Sans Serif
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FontFamilyChip(
                    fontFamily = FontFamilyUiModel.DEFAULT,
                    isSelected = selectedFontFamily == FontFamilyUiModel.DEFAULT,
                    onClick = { onFontFamilySelected(FontFamilyUiModel.DEFAULT) },
                    modifier = Modifier.weight(1f),
                )
                FontFamilyChip(
                    fontFamily = FontFamilyUiModel.SERIF,
                    isSelected = selectedFontFamily == FontFamilyUiModel.SERIF,
                    onClick = { onFontFamilySelected(FontFamilyUiModel.SERIF) },
                    modifier = Modifier.weight(1f),
                )
                FontFamilyChip(
                    fontFamily = FontFamilyUiModel.SANS_SERIF,
                    isSelected = selectedFontFamily == FontFamilyUiModel.SANS_SERIF,
                    onClick = { onFontFamilySelected(FontFamilyUiModel.SANS_SERIF) },
                    modifier = Modifier.weight(1f),
                )
            }
            // Second row: Cursive, Fantasy, Monospace
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FontFamilyChip(
                    fontFamily = FontFamilyUiModel.CURSIVE,
                    isSelected = selectedFontFamily == FontFamilyUiModel.CURSIVE,
                    onClick = { onFontFamilySelected(FontFamilyUiModel.CURSIVE) },
                    modifier = Modifier.weight(1f),
                )
                FontFamilyChip(
                    fontFamily = FontFamilyUiModel.FANTASY,
                    isSelected = selectedFontFamily == FontFamilyUiModel.FANTASY,
                    onClick = { onFontFamilySelected(FontFamilyUiModel.FANTASY) },
                    modifier = Modifier.weight(1f),
                )
                FontFamilyChip(
                    fontFamily = FontFamilyUiModel.MONOSPACE,
                    isSelected = selectedFontFamily == FontFamilyUiModel.MONOSPACE,
                    onClick = { onFontFamilySelected(FontFamilyUiModel.MONOSPACE) },
                    modifier = Modifier.weight(1f),
                )
            }
            // Third row: Accessibility fonts
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FontFamilyChip(
                    fontFamily = FontFamilyUiModel.ACCESSIBLE_DFA,
                    isSelected = selectedFontFamily == FontFamilyUiModel.ACCESSIBLE_DFA,
                    onClick = { onFontFamilySelected(FontFamilyUiModel.ACCESSIBLE_DFA) },
                    modifier = Modifier.weight(1f),
                )
                FontFamilyChip(
                    fontFamily = FontFamilyUiModel.IA_WRITER_DUOSPACE,
                    isSelected = selectedFontFamily == FontFamilyUiModel.IA_WRITER_DUOSPACE,
                    onClick = { onFontFamilySelected(FontFamilyUiModel.IA_WRITER_DUOSPACE) },
                    modifier = Modifier.weight(1f),
                )
                FontFamilyChip(
                    fontFamily = FontFamilyUiModel.OPEN_DYSLEXIC,
                    isSelected = selectedFontFamily == FontFamilyUiModel.OPEN_DYSLEXIC,
                    onClick = { onFontFamilySelected(FontFamilyUiModel.OPEN_DYSLEXIC) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun FontFamilyChip(
    fontFamily: FontFamilyUiModel,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                text = fontFamily.toDisplayString(),
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
            )
        },
        modifier = modifier,
    )
}

@Composable
private fun FontFamilyUiModel.toDisplayString(): String = when (this) {
    FontFamilyUiModel.DEFAULT -> stringResource(StringRes.settings_font_family_default)
    FontFamilyUiModel.SERIF -> stringResource(StringRes.settings_font_family_serif)
    FontFamilyUiModel.SANS_SERIF -> stringResource(StringRes.settings_font_family_sans_serif)
    FontFamilyUiModel.CURSIVE -> stringResource(StringRes.settings_font_family_cursive)
    FontFamilyUiModel.FANTASY -> stringResource(StringRes.settings_font_family_fantasy)
    FontFamilyUiModel.MONOSPACE -> stringResource(StringRes.settings_font_family_monospace)
    FontFamilyUiModel.ACCESSIBLE_DFA -> stringResource(StringRes.settings_font_family_accessible_dfa)
    FontFamilyUiModel.IA_WRITER_DUOSPACE -> stringResource(
        StringRes.settings_font_family_ia_writer_duospace,
    )

    FontFamilyUiModel.OPEN_DYSLEXIC -> stringResource(StringRes.settings_font_family_open_dyslexic)
}

@Composable
private fun TextAlignSelector(
    selectedAlign: ReaderTextAlignUiModel,
    onAlignSelected: (ReaderTextAlignUiModel) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(StringRes.settings_text_align),
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.height(8.dp))
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            ReaderTextAlignUiModel.entries.forEachIndexed { index, align ->
                SegmentedButton(
                    selected = align == selectedAlign,
                    onClick = { onAlignSelected(align) },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = ReaderTextAlignUiModel.entries.size,
                    ),
                ) {
                    Text(text = align.toDisplayString())
                }
            }
        }
    }
}

/**
 * Scroll mode selector with three options:
 * - Auto (null): Let the publication/reader decide based on EPUB metadata
 * - Scroll (true): Force continuous scrolling mode
 * - Paginated (false): Force page-by-page reading
 */
@Composable
private fun ScrollModeSelector(
    selectedMode: Boolean?,
    onModeSelected: (Boolean?) -> Unit,
) {
    // Options: null = Auto, false = Paginated, true = Scroll
    val options = listOf<Boolean?>(null, false, true)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(StringRes.settings_scroll_mode),
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.height(8.dp))
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            options.forEachIndexed { index, mode ->
                SegmentedButton(
                    selected = mode == selectedMode,
                    onClick = { onModeSelected(mode) },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = options.size,
                    ),
                ) {
                    Text(text = mode.toScrollModeDisplayString())
                }
            }
        }
    }
}

@Composable
private fun Boolean?.toScrollModeDisplayString(): String = when (this) {
    null -> stringResource(StringRes.settings_scroll_mode_auto)
    false -> stringResource(StringRes.settings_scroll_mode_paginated)
    true -> stringResource(StringRes.settings_scroll_mode_scroll)
}

/**
 * Selector for progress bar visibility mode.
 * Options:
 * - Always (true): Progress bar is always visible
 * - On Tap (null): Progress bar shows/hides with controls when tapping
 * - Never (false): Progress bar is never visible
 */
@Composable
private fun ProgressBarModeSelector(
    selectedMode: Boolean?,
    onModeSelected: (Boolean?) -> Unit,
) {
    // Options: true = Always, null = On Tap, false = Never
    val options = listOf<Boolean?>(true, null, false)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(StringRes.settings_progress_bar),
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.height(8.dp))
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            options.forEachIndexed { index, mode ->
                SegmentedButton(
                    selected = mode == selectedMode,
                    onClick = { onModeSelected(mode) },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = options.size,
                    ),
                ) {
                    Text(text = mode.toProgressBarModeDisplayString())
                }
            }
        }
    }
}

@Composable
private fun Boolean?.toProgressBarModeDisplayString(): String = when (this) {
    true -> stringResource(StringRes.settings_progress_bar_always)
    null -> stringResource(StringRes.settings_progress_bar_on_tap)
    false -> stringResource(StringRes.settings_progress_bar_never)
}

/**
 * Selector for progress bar position.
 * Options:
 * - Top: Progress bar at the top, overlaid with toolbar when visible
 * - Bottom: Progress bar at the bottom (default)
 */
@Composable
private fun ProgressBarPositionSelector(
    selectedPosition: ProgressBarPosition,
    onPositionSelected: (ProgressBarPosition) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(StringRes.settings_progress_bar_position),
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.height(8.dp))
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            ProgressBarPosition.entries.forEachIndexed { index, position ->
                SegmentedButton(
                    selected = position == selectedPosition,
                    onClick = { onPositionSelected(position) },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = ProgressBarPosition.entries.size,
                    ),
                ) {
                    Text(text = position.toDisplayString())
                }
            }
        }
    }
}

@Composable
private fun ProgressBarPosition.toDisplayString(): String = when (this) {
    ProgressBarPosition.TOP -> stringResource(StringRes.settings_progress_bar_position_top)
    ProgressBarPosition.BOTTOM -> stringResource(StringRes.settings_progress_bar_position_bottom)
}

/**
 * Selector for chapter progress display mode.
 * Options:
 * - None: No chapter progress info shown
 * - Percentage: Show chapter progress as percentage
 * - Pages (Relative): Show page numbers based on viewport
 * - Position (Fixed): Show EPUB position
 */
@Composable
private fun ChapterProgressDisplayModeSelector(
    selectedMode: ChapterProgressDisplayMode,
    onModeSelected: (ChapterProgressDisplayMode) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(StringRes.settings_chapter_progress),
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.height(8.dp))
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            ChapterProgressDisplayMode.entries.forEachIndexed { index, mode ->
                SegmentedButton(
                    selected = mode == selectedMode,
                    onClick = { onModeSelected(mode) },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = ChapterProgressDisplayMode.entries.size,
                    ),
                ) {
                    Text(text = mode.toDisplayString())
                }
            }
        }
    }
}

@Composable
private fun ChapterProgressDisplayMode.toDisplayString(): String = when (this) {
    ChapterProgressDisplayMode.NONE -> stringResource(StringRes.settings_chapter_progress_none)
    ChapterProgressDisplayMode.PERCENTAGE ->
        stringResource(StringRes.settings_chapter_progress_percentage)

    ChapterProgressDisplayMode.RELATIVE ->
        stringResource(StringRes.settings_chapter_progress_relative)

    ChapterProgressDisplayMode.FIXED -> stringResource(StringRes.settings_chapter_progress_fixed)
}

/**
 * Switch to toggle total progress display.
 */
@Composable
private fun ShowTotalProgressSwitch(
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(StringRes.settings_show_total_progress),
            style = MaterialTheme.typography.bodyMedium,
        )
        Switch(
            checked = isEnabled,
            onCheckedChange = onToggle,
        )
    }
}

/**
 * Switch to toggle fullscreen mode (hide status bar while reading).
 */
@Composable
private fun FullscreenModeSwitch(
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(StringRes.settings_fullscreen_mode),
                style = MaterialTheme.typography.bodyMedium,
            )
            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle,
            )
        }
        Text(
            text = stringResource(StringRes.settings_fullscreen_mode_description),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Switch to toggle current time display in the progress bar.
 */
@Composable
private fun ShowCurrentTimeSwitch(
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(StringRes.settings_show_current_time),
                style = MaterialTheme.typography.bodyMedium,
            )
            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle,
            )
        }
        Text(
            text = stringResource(StringRes.settings_show_current_time_description),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Switch to toggle remaining reading time display in the progress bar.
 */
@Composable
private fun ShowReadingTimeSwitch(
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(StringRes.settings_show_reading_time),
                style = MaterialTheme.typography.bodyMedium,
            )
            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle,
            )
        }
        Text(
            text = stringResource(StringRes.settings_show_reading_time_description),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Selector for progress indicator mode.
 * Options:
 * - None: No progress indicator shown
 * - Chapter: Show chapter progress indicator
 * - Book: Show book (total) progress indicator
 */
@Composable
private fun ProgressIndicatorModeSelector(
    selectedMode: ProgressIndicatorMode,
    onModeSelected: (ProgressIndicatorMode) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(StringRes.settings_progress_indicator),
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.height(8.dp))
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            ProgressIndicatorMode.entries.forEachIndexed { index, mode ->
                SegmentedButton(
                    selected = mode == selectedMode,
                    onClick = { onModeSelected(mode) },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = ProgressIndicatorMode.entries.size,
                    ),
                ) {
                    Text(text = mode.toDisplayString())
                }
            }
        }
    }
}

@Composable
private fun ProgressIndicatorMode.toDisplayString(): String = when (this) {
    ProgressIndicatorMode.NONE -> stringResource(StringRes.settings_progress_indicator_none)
    ProgressIndicatorMode.CHAPTER -> stringResource(StringRes.settings_progress_indicator_chapter)
    ProgressIndicatorMode.BOOK -> stringResource(StringRes.settings_progress_indicator_book)
}

@Composable
private fun SettingsSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    valueDisplay: (Float) -> String,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = valueDisplay(value),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Slider(
            modifier = Modifier.fillMaxWidth(),
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
        )
    }
}

@Composable
private fun SettingsSwitch(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Composable
private fun ReaderThemeUiModel.toDisplayString(): String = when (this) {
    ReaderThemeUiModel.LIGHT -> stringResource(StringRes.settings_theme_light)
    ReaderThemeUiModel.DARK -> stringResource(StringRes.settings_theme_dark)
    ReaderThemeUiModel.SEPIA -> stringResource(StringRes.settings_theme_sepia)
    ReaderThemeUiModel.SYSTEM -> stringResource(StringRes.settings_theme_system)
}

@Composable
private fun ReaderTextAlignUiModel.toDisplayString(): String = when (this) {
    ReaderTextAlignUiModel.START -> stringResource(StringRes.settings_text_align_start)
    ReaderTextAlignUiModel.END -> stringResource(StringRes.settings_text_align_end)
    ReaderTextAlignUiModel.CENTER -> stringResource(StringRes.settings_text_align_center)
    ReaderTextAlignUiModel.JUSTIFY -> stringResource(StringRes.settings_text_align_justify)
}

