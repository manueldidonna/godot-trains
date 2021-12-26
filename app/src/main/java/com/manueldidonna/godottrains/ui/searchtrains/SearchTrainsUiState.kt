package com.manueldidonna.godottrains.ui.searchtrains

import com.manueldidonna.godottrains.data.models.Station
import kotlinx.datetime.LocalDateTime

data class SearchTrainsUiState(
    val departureStation: Station?,
    val arrivalStation: Station?,
    val departureDateTime: LocalDateTime,
    val recentStationSearches: List<Station>,
    val isSearchAllowed: Boolean,
    val isConnectedToNetwork: Boolean
)
