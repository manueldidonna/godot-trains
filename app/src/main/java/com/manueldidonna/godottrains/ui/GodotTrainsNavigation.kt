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
package com.manueldidonna.godottrains.ui

import androidx.annotation.Keep
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalTextInputService
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.manueldidonna.godottrains.ui.searchresults.SearchResultsRoute
import com.manueldidonna.godottrains.ui.searchstation.SearchStationRoute
import com.manueldidonna.godottrains.ui.searchtrains.SearchTrainsRoute

@Composable
fun GodotTrainsNavigation() {
    val navController = rememberNavController()
    val viewModel = viewModel<TrainsViewModel>()

    HideKeyboardOnNavigationChange(navController)
    NavHost(navController, startDestination = SearchTrainsRoute) {
        composable(SearchTrainsRoute) {
            SearchTrainsRoute(
                viewModel = viewModel,
                searchDepartureStation = {
                    navController.navigate(SearchStations.createRouteFromArguments(isDeparture = true))
                },
                searchArrivalStation = {
                    navController.navigate(SearchStations.createRouteFromArguments(isDeparture = false))
                },
                navigateToSearchResults = {
                    navController.navigate(OneWayTrainSolutionsRoute)
                }
            )
        }

        composable(SearchStations.Route, SearchStations.Arguments) {
            SearchStationRoute(
                viewModel = viewModel,
                onNavigationUp = navController::navigateUp,
                searchForDeparture = it.arguments
                    ?.getBoolean(SearchStations.IsDepartureArgument) ?: true
            )
        }

        composable(OneWayTrainSolutionsRoute) {
            SearchResultsRoute(
                viewModel = viewModel,
                onNavigationUp = navController::navigateUp
            )
        }
    }
}

@Composable
private fun HideKeyboardOnNavigationChange(navController: NavController) {
    val textInputService = LocalTextInputService.current
    val updatedTextInputService by rememberUpdatedState(textInputService)
    DisposableEffect(navController) {
        val listener = NavController.OnDestinationChangedListener { _, _, _ ->
            updatedTextInputService?.hideSoftwareKeyboard()
        }
        navController.addOnDestinationChangedListener(listener)
        onDispose {
            navController.removeOnDestinationChangedListener(listener)
        }
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
