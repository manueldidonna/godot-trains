package com.manueldidonna.godottrains.ui.searchstation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.manueldidonna.godottrains.ui.TrainsViewModel

@Composable
fun SearchStationRoute(
    viewModel: TrainsViewModel,
    searchForDeparture: Boolean,
    onNavigationUp: () -> Unit
) {
    val viewModelState by viewModel.viewModelState.collectAsState()

    val state = remember(viewModelState) {
        SearchStationUiState(
            recentSearches = viewModelState.recentStationSearches,
            searchResults = viewModelState.stationSearchResults,
            isSearchingStations = viewModelState.isSearchingStations,
            searchHint = if (searchForDeparture) "Search departure station" else "Search arrival station"
        )
    }

    SearchStationScreen(
        state = state,
        onNavigationUp = onNavigationUp,
        searchStations = { query -> viewModel.searchStations(query) },
        onStationSelection = { station ->
            if (searchForDeparture) viewModel.setDepartureStation(station)
            else viewModel.setArrivalStation(station)
            onNavigationUp()
        }
    )
}