package com.manueldidonna.godottrains.ui.searchstation

import com.manueldidonna.godottrains.data.models.Station

data class SearchStationUiState(
    val recentSearches: List<Station> = emptyList(),
    val searchResults: List<Station> = emptyList(),
    val searchHint: String = "",
    val isSearchingStations: Boolean = false
)
