package com.manueldidonna.godottrains.ui.searchtrains

import com.manueldidonna.godottrains.data.models.Station
import kotlinx.datetime.LocalDateTime

data class SearchTrainsUiState(
    val departureStation: Station?,
    val arrivalStation: Station?,
    val departureDateTime: LocalDateTime,
    private val recentStationSearches: List<Station>,
    val isSearchAllowed: Boolean,
    val isConnectedToNetwork: Boolean
) {
    val orderedRecentStationSearches: List<Station> by lazy {
        if (departureStation == null && arrivalStation == null) {
            recentStationSearches
        } else {
            recentStationSearches.sortedByDescending { it.id != departureStation?.id && it.id != arrivalStation?.id }
        }
    }
}
