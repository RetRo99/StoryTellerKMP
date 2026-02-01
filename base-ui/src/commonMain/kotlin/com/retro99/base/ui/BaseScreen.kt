package com.retro99.base.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun <State, Intent : BaseIntent> BaseScreen(
    viewModel: BaseViewModel<State, Intent>,
    modifier: Modifier = Modifier,
    content: @Composable (State, IntentDispatcher<Intent>) -> Unit,
) {
    val state by viewModel.viewState.collectAsState()

    Box(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        content(state, IntentDispatcher(viewModel::onIntent))
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