package com.manueldidonna.godottrains.ui.searchstation

import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.manueldidonna.godottrains.R
import com.manueldidonna.godottrains.ui.TrainsViewModel

@Composable
fun SearchStationRoute(
    viewModel: TrainsViewModel,
    searchForDeparture: Boolean,
    onNavigationUp: () -> Unit
) {
    val viewModelState by viewModel.viewModelState.collectAsState()

    NavigateUpOnConnectionLost(
        isConnectedToNetwork = viewModelState.isConnectedToNetwork,
        onNavigationUp = onNavigationUp
    )

    val state = remember(viewModelState) {
        SearchStationUiState(
            recentSearches = viewModelState.recentStationSearches,
            searchResults = viewModelState.stationSearchResults,
            isSearchingStations = viewModelState.isSearchingStations,
            searchForDepartureStation = searchForDeparture
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

@Composable
private fun NavigateUpOnConnectionLost(isConnectedToNetwork: Boolean, onNavigationUp: () -> Unit) {
    val updatedOnNavigationUp by rememberUpdatedState(onNavigationUp)
    val context = LocalContext.current
    LaunchedEffect(isConnectedToNetwork, context) {
        if (!isConnectedToNetwork) {
            updatedOnNavigationUp()
            Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show()
        }
    }
}