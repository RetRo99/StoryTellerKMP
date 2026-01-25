package com.retro99.base.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.retro99.base.result.AppError
import org.jetbrains.compose.resources.stringResource

@Composable
fun <State, Intent : BaseIntent> BaseScreen(
    viewModel: BaseViewModel<State, Intent>,
    loadingContent: @Composable () -> Unit = { LoadingScreen() },
    errorContent: @Composable (AppError) -> Unit = { ErrorScreen(it) },
    content: @Composable (State, IntentDispatcher<Intent>) -> Unit
) {
    val state by viewModel.viewState.collectAsState()

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        when (val s = state) {
            is BaseViewState.Loading -> loadingContent()
            is BaseViewState.Error -> errorContent(s.error)
            is BaseViewState.Success -> content(s.data, IntentDispatcher(viewModel::onIntent))
        }
    }
}

@Composable
fun ErrorScreen(error: AppError) {
    Box(Modifier.fillMaxSize()) {
        val errorMessage = stringResource(error.toStringRes())

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun LoadingScreen() {
    Box(Modifier.fillMaxSize()) {
        CircularProgressIndicator(
            modifier = Modifier.align(Alignment.Center)
        )
    }
}