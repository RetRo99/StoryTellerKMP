package com.retro99.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.retro99.base.ui.BaseScreen
import com.retro99.base.ui.IntentDispatcher
import com.retro99.translations.StringRes
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import resources.translations.settings_logout
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
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stringResource(StringRes.settings_title),
            style = MaterialTheme.typography.headlineMedium,
        )

        Spacer(modifier = Modifier.weight(1f))

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

