package com.manueldidonna.godottrains.ui.searchresults

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.manueldidonna.godottrains.ui.TrainsViewModel

@Composable
fun SearchResultsRoute(
    viewModel: TrainsViewModel,
    onNavigationUp: () -> Unit
) {
    val viewModelState by viewModel.viewModelState.collectAsState()

    val state = remember(viewModelState) {
        if (viewModelState.isLoadingSolutions) {
            SearchResultsUiState.Loading
        } else {
            SearchResultsUiState.Solutions(
                oneWaySolutions = viewModelState.oneWaySolutions,
                isLoadingMoreSolutions = viewModelState.isLoadingMoreSolutions
            )
        }
    }

    SearchResultsScreen(
        state = state,
        onNavigationUp = onNavigationUp,
        loadMoreSolutions = viewModel::loadMoreOneWaySolutions,
        retrySearch = viewModel::searchOneWaySolutions
    )
}