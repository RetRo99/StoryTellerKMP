package com.retro99.settings.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.retro99.base.ui.BaseScreen
import com.retro99.base.ui.IntentDispatcher
import com.retro99.settings.ui.model.ReaderTextAlignUiModel
import com.retro99.settings.ui.model.ReaderThemeUiModel
import com.retro99.translations.StringRes
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import resources.translations.settings_font_size
import resources.translations.settings_line_height
import resources.translations.settings_logout
import resources.translations.settings_margin_horizontal
import resources.translations.settings_margin_vertical
import resources.translations.settings_publisher_styles
import resources.translations.settings_publisher_styles_description
import resources.translations.settings_reader_section
import resources.translations.settings_scroll_mode
import resources.translations.settings_scroll_mode_auto
import resources.translations.settings_scroll_mode_paginated
import resources.translations.settings_scroll_mode_scroll
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
    onLogoutSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = koinViewModel { parametersOf(onLogoutSuccess) },
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
    ) {
        Text(
            text = stringResource(StringRes.settings_title),
            style = MaterialTheme.typography.headlineMedium,
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Reader Settings Section
        ReaderSettingsSection(
            viewState = viewState,
            intentDispatcher = intentDispatcher,
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Logout Button
        Button(
            onClick = { intentDispatcher(SettingsIntent.OnLogoutClicked) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !viewState.isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError,
            ),
        ) {
            Text(text = stringResource(StringRes.settings_logout))
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ReaderSettingsSection(
    viewState: SettingsViewState,
    intentDispatcher: IntentDispatcher<SettingsIntent>,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = stringResource(StringRes.settings_reader_section),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        // Theme Selection
        ThemeSelector(
            selectedTheme = viewState.theme,
            onThemeSelected = { intentDispatcher(SettingsIntent.OnThemeChanged(it)) },
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Font Size Slider
        SettingsSlider(
            label = stringResource(StringRes.settings_font_size),
            value = viewState.fontSize.toFloat(),
            valueRange = 0.5f..3.0f,
            onValueChange = { intentDispatcher(SettingsIntent.OnFontSizeChanged(it.toDouble())) },
            valueDisplay = { "${(it * 100).toInt()}%" },
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Publisher Styles Toggle
        SettingsSwitch(
            label = stringResource(StringRes.settings_publisher_styles),
            description = stringResource(StringRes.settings_publisher_styles_description),
            checked = viewState.publisherStyles,
            onCheckedChange = { intentDispatcher(SettingsIntent.OnPublisherStylesChanged(it)) },
        )

        // Custom styling options - only visible when publisher styles is disabled
        // Spacing is included inside AnimatedVisibility to animate smoothly
        AnimatedVisibility(
            visible = !viewState.publisherStyles,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            Column {
                Spacer(modifier = Modifier.height(16.dp))

                // Line Height Slider
                SettingsSlider(
                    label = stringResource(StringRes.settings_line_height),
                    value = viewState.lineHeight,
                    valueRange = 1.0f..2.5f,
                    onValueChange = { intentDispatcher(SettingsIntent.OnLineHeightChanged(it)) },
                    valueDisplay = { ((it * 10).toInt() / 10.0).toString() },
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Text Alignment
                TextAlignSelector(
                    selectedAlign = viewState.textAlign,
                    onAlignSelected = { intentDispatcher(SettingsIntent.OnTextAlignChanged(it)) },
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Horizontal Margin Slider
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

        // Vertical Margin Slider
        SettingsSlider(
            label = stringResource(StringRes.settings_margin_vertical),
            value = viewState.marginVertical.toFloat(),
            valueRange = 0f..48f,
            onValueChange = {
                intentDispatcher(SettingsIntent.OnMarginVerticalChanged(it.toInt()))
            },
            valueDisplay = { "${it.toInt()}dp" },
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Scroll Mode Selector
        ScrollModeSelector(
            selectedMode = viewState.scrollMode,
            onModeSelected = { intentDispatcher(SettingsIntent.OnScrollModeChanged(it)) },
        )

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
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

