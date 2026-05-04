package com.kynzai.petmates.ui.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.kynzai.domain.common.LoadState
import com.kynzai.petmates.ui.common.isServerUnavailable
import com.kynzai.petmates.ui.common.toUiMessage

@Composable
fun NotificationsRoute(
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
    showHeader: Boolean = true,
    vm: NotificationsViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()
    val items = (state as? LoadState.Data)?.value.orEmpty()

    NotificationsTabContent(
        modifier = modifier,
        showHeader = showHeader,
        items = items,
        isLoading = state is LoadState.Loading,
        errorMessage = (state as? LoadState.Error)?.error?.toUiMessage(),
        isServerUnavailable = (state as? LoadState.Error)?.error?.isServerUnavailable() == true,
        onReadAllClick = { vm.markAllRead() },
        onRetryClick = { vm.refresh() },
    )
}
