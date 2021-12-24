package com.manueldidonna.godottrains.ui.searchtrains

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.manueldidonna.godottrains.ui.TrainsViewModel
import com.manueldidonna.godottrains.ui.isSearchTrainsAllowed

@Composable
fun SearchTrainsRoute(
    viewModel: TrainsViewModel,
    selectDepartureStation: () -> Unit,
    selectArrivalStation: () -> Unit,
    navigateToSearchResults: () -> Unit
) {
    val viewModelState by viewModel.viewModelState.collectAsState()

    val state = remember(viewModelState) {
        SearchTrainsUiState(
            departureStation = viewModelState.departureStation,
            arrivalStation = viewModelState.arrivalStation,
            recentStationSearches = viewModelState.recentStationSearches,
            departureDateTime = viewModelState.departureDateTime,
            isSearchAllowed = viewModelState.isSearchTrainsAllowed
        )
    }

    SearchTrainsScreen(
        state = state,
        selectArrivalStation = selectArrivalStation,
        selectDepartureStation = selectDepartureStation,
        searchOneWaySolutions = {
            viewModel.searchOneWaySolutions()
            navigateToSearchResults()
        },
        setDepartureDateTime = viewModel::setDepartureDateTime
    )
}
