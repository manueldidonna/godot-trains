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

import androidx.annotation.Keep
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import com.manueldidonna.godottrains.entities.OneWaySolution
import com.manueldidonna.godottrains.onewaysolutions.OneWayTrainSolutionsCallback
import com.manueldidonna.godottrains.onewaysolutions.OneWayTrainSolutionsScreen
import com.manueldidonna.godottrains.searchstations.SearchStationsCallback
import com.manueldidonna.godottrains.searchstations.SearchStationsScreen
import com.manueldidonna.godottrains.searchtrains.SearchTrainsCallback
import com.manueldidonna.godottrains.searchtrains.SearchTrainsScreen
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

@Composable
fun GodotTrainsNavigation() {
    val navController = rememberNavController()
    val viewModel = viewModel<TrainsViewModel>()

    NavHost(navController, startDestination = SearchTrainsRoute) {

        composable(SearchTrainsRoute) {
            val callback = remember(viewModel, navController) {
                createSearchTrainsCallback(viewModel, navController)
            }
            SearchTrainsScreen(callback = callback)
        }

        composable(SearchStations.Route, SearchStations.Arguments) {
            val searchForDeparture = it.arguments
                ?.getBoolean(SearchStations.IsDepartureArgument) ?: true
            val callback = remember(viewModel, navController, searchForDeparture) {
                createSearchStationsCallback(viewModel, navController, searchForDeparture)
            }
            SearchStationsScreen(
                callback = callback,
                searchDepartureStation = searchForDeparture
            )
        }

        composable(OneWayTrainSolutionsRoute) {
            val callback = remember(viewModel, navController) {
                createOneWayTrainSolutionsCallback(viewModel, navController)
            }
            OneWayTrainSolutionsScreen(callback = callback)
        }
    }
}

private fun createSearchTrainsCallback(
    trainsViewModel: TrainsViewModel,
    navController: NavController,
) = object : SearchTrainsCallback {
    override val arrivalStationName: Flow<String?> =
        trainsViewModel.stateFlow.map { it.arrivalStationName }

    override val departureStationName: Flow<String?> =
        trainsViewModel.stateFlow.map { it.departureStationName }

    override val departureTimeInMinutes: Flow<Int> =
        trainsViewModel.stateFlow.map { it.departureTimeInMinutes }

    override val departureDate: Flow<LocalDate> =
        trainsViewModel.stateFlow.map { it.departureDate }

    override val isSearchAllowed: Flow<Boolean> =
        trainsViewModel.stateFlow.map { it.isSearchAllowed }

    override fun searchArrivalStation() {
        navController.navigate(SearchStations.createRouteFromArguments(isDeparture = false))
    }

    override fun searchDepartureStation() {
        navController.navigate(SearchStations.createRouteFromArguments(isDeparture = true))
    }

    override fun setDepartureTimeInMinutes(timeInMinutes: Int) {
        trainsViewModel.setDepartureTimeInMinutes(timeInMinutes)
    }

    override fun setDepartureDate(localDate: LocalDate) {
        trainsViewModel.setDepartureDate(localDate)
    }

    override fun searchOneWaySolutions() {
        navController.navigate(OneWayTrainSolutionsRoute)
    }

    override fun swapStationNames() {
        trainsViewModel.swapStationNames()
    }
}

private fun createSearchStationsCallback(
    trainsViewModel: TrainsViewModel,
    navController: NavController,
    searchForDeparture: Boolean
) = object : SearchStationsCallback {
    override val recentSearchResults: Flow<List<String>>
        get() = trainsViewModel.recentSearchResults

    override suspend fun getStationNamesByQuery(query: String): List<String> {
        return trainsViewModel.getStationNamesByQuery(query)
    }

    override fun selectStationByName(stationName: String) {
        if (searchForDeparture) trainsViewModel.setDepartureStationName(stationName)
        else trainsViewModel.setArrivalStationName(stationName)
        navController.popBackStack()
    }

    override fun cancelSearchAndGoBack() {
        navController.popBackStack()
    }
}

private fun createOneWayTrainSolutionsCallback(
    trainsViewModel: TrainsViewModel,
    navController: NavController
) = object : OneWayTrainSolutionsCallback {
    override fun getOneWaySolutions(): StateFlow<List<OneWaySolution>?> {
        return trainsViewModel.getOneWaySolutions()
    }

    override suspend fun loadNextOneWaySolutions() {
        trainsViewModel.loadNextOneWaySolutions()
    }

    override fun closeOneWaySolutionsScreen() {
        navController.popBackStack()
    }
}

@Keep
private const val SearchTrainsRoute = "search_trains"

@Keep
private const val OneWayTrainSolutionsRoute = "one_way_train_solutions"

private object SearchStations {

    @Keep
    const val IsDepartureArgument = "departure"

    @Keep
    const val Route = "search_stations?is_departure={$IsDepartureArgument}"

    val Arguments = listOf(
        navArgument(IsDepartureArgument) {
            type = NavType.BoolType
            defaultValue = true
        }
    )

    fun createRouteFromArguments(isDeparture: Boolean): String {
        return Route.replace("{$IsDepartureArgument}", isDeparture.toString())
    }
}
