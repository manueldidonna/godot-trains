/*
 * Copyright (C) 2021 Manuel Di Donna
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  he Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.manueldidonna.godottrains

import androidx.lifecycle.ViewModel
import com.manueldidonna.godottrains.network.LeFrecceApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.*

class TrainsViewModel : ViewModel() {

    data class State(
        val departureStationName: String? = null,
        val arrivalStationName: String? = null,
        val departureTimeInMinutes: Int = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .run { hour * 60 + minute },
        val departureDate: LocalDate = Clock.System.todayAt(TimeZone.currentSystemDefault())
    )

    private val _stateFlow = MutableStateFlow(State())

    val stateFlow: StateFlow<State>
        get() = _stateFlow

    fun setArrivalStationName(stationName: String) {
        _stateFlow.value = _stateFlow.value.copy(arrivalStationName = stationName)
    }

    fun setDepartureStationName(stationName: String) {
        _stateFlow.value = _stateFlow.value.copy(departureStationName = stationName)
    }

    fun setDepartureTimeInMinutes(timeInMinutes: Int) {
        _stateFlow.value = _stateFlow.value.copy(departureTimeInMinutes = timeInMinutes)
    }

    fun setDepartureDate(localDate: LocalDate) {
        _stateFlow.value = _stateFlow.value.copy(departureDate = localDate)
    }

    // TODO: implement this function
    suspend fun getStationNamesByQuery(query: String): List<String> {
        return LeFrecceApi.Ktor.getStationsByPartialName(query)
    }

    // TODO: save results to the disk with jetpack DataStore
    val recentSearchResults = flowOf(
        listOf(
            "Torre del Greco",
            "Napoli Piazza Garibaldi",
            "Napoli MonteSanto"
        )
    )
}
