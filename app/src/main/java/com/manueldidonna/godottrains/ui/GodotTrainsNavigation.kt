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
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalTextInputService
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.*
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.manueldidonna.godottrains.ui.searchresults.SearchResultsRoute
import com.manueldidonna.godottrains.ui.searchstation.SearchStationRoute
import com.manueldidonna.godottrains.ui.searchtrains.SearchTrainsRoute

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun GodotTrainsNavigation() {
    val navController = rememberAnimatedNavController()
    val viewModel = viewModel<TrainsViewModel>()

    HideKeyboardOnNavigationChange(navController)

    DefaultAnimatedNavHost(navController, startDestination = SearchTrainsRoute) {
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

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun DefaultAnimatedNavHost(
    navController: NavHostController,
    startDestination: String,
    builder: NavGraphBuilder.() -> Unit
) {
    val density = LocalDensity.current
    AnimatedNavHost(
        navController = navController,
        startDestination = startDestination,
        builder = builder,
        enterTransition = {
            sharedAxisXTransitionIn(density, forward = true)
        },
        popEnterTransition = {
            sharedAxisXTransitionIn(density, forward = false)
        },
        exitTransition = {
            sharedAxisXTransitionOut(density, forward = true)
        },
        popExitTransition = {
            sharedAxisXTransitionOut(density, forward = false)
        }
    )
}


@Stable
private fun sharedAxisXTransitionIn(density: Density, forward: Boolean): EnterTransition {
    val slideDistancePx = with(density) { SharedAxisX.SlideDistance.roundToPx() }
    return slideInHorizontally(
        initialOffsetX = { if (forward) slideDistancePx else -slideDistancePx },
        animationSpec = tween(
            durationMillis = SharedAxisX.Duration,
            easing = FastOutSlowInEasing
        )
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = SharedAxisX.DurationForIncoming,
            delayMillis = SharedAxisX.DurationForOutgoing,
            easing = LinearOutSlowInEasing
        )
    )
}

@Stable
private fun sharedAxisXTransitionOut(density: Density, forward: Boolean): ExitTransition {
    val slideDistancePx = with(density) { SharedAxisX.SlideDistance.roundToPx() }
    return slideOutHorizontally(
        targetOffsetX = { if (forward) -slideDistancePx else slideDistancePx },
        animationSpec = tween(
            durationMillis = SharedAxisX.Duration,
            easing = FastOutSlowInEasing
        )
    ) + fadeOut(
        animationSpec = tween(
            durationMillis = SharedAxisX.DurationForOutgoing,
            delayMillis = 0,
            easing = FastOutLinearInEasing
        )
    )
}

private object SharedAxisX {
    val SlideDistance = 30.dp

    const val Duration = 300
    const val DurationForOutgoing = (Duration * 0.35f).toInt()
    const val DurationForIncoming = Duration - DurationForOutgoing
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
