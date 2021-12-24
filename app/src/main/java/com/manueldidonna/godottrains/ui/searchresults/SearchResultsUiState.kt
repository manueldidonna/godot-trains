package com.manueldidonna.godottrains.ui.searchresults

import com.manueldidonna.godottrains.data.models.OneWaySolution

sealed interface SearchResultsUiState {
    object Loading : SearchResultsUiState

    data class Solutions(
        val oneWaySolutions: List<OneWaySolution>,
        val isLoadingMoreSolutions: Boolean
    ) : SearchResultsUiState
}