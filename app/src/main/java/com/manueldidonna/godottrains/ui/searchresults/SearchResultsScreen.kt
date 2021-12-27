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

package com.manueldidonna.godottrains.ui.searchresults

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.insets.navigationBarsHeight
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.statusBarsPadding
import com.manueldidonna.godottrains.R
import com.manueldidonna.godottrains.data.models.OneWaySolution
import com.manueldidonna.godottrains.data.models.firstDepartureDateTime
import com.manueldidonna.godottrains.ui.searchresults.components.OneWaySolutionCard
import kotlinx.datetime.*
import java.time.format.TextStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultsScreen(
    state: SearchResultsUiState,
    loadMoreSolutions: () -> Unit,
    retrySearch: () -> Unit,
    onNavigationUp: () -> Unit,
) {

    Box(modifier = Modifier.fillMaxSize()) {
        val scrollBehavior = remember { TopAppBarDefaults.pinnedScrollBehavior() }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.surface)
        ) {
            SearchResultsAppBar(
                scrollBehavior = scrollBehavior,
                onNavigationUp = onNavigationUp
            )
            if (state !is SearchResultsUiState.Solutions) {
                LoadingIndicator(modifier = Modifier.weight(1f))
            } else {
                TrainsList(
                    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                    solutions = state.oneWaySolutions,
                    isLoadingMoreResults = state.isLoadingMoreSolutions,
                    onLoadMoreResultsClick = loadMoreSolutions,
                    retrySearch = retrySearch
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchResultsAppBar(
    scrollBehavior: TopAppBarScrollBehavior,
    onNavigationUp: () -> Unit
) {
    SmallTopAppBar(
        modifier = Modifier
            .statusBarsPadding()
            .navigationBarsPadding(bottom = false, start = true, end = true),
        title = { Text(text = stringResource(id = R.string.search_results_app_bar_title)) },
        navigationIcon = {
            IconButton(onClick = onNavigationUp) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = stringResource(id = R.string.navigate_up_action)
                )
            }
        },
        scrollBehavior = scrollBehavior
    )
}

@Composable
private fun LoadingIndicator(modifier: Modifier) {
    Box(modifier = modifier) {
        CircularProgressIndicator(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize()
                .size(56.dp)
                .navigationBarsPadding(),
            strokeWidth = 8.dp,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun TrainsList(
    modifier: Modifier = Modifier,
    solutions: List<OneWaySolution>,
    isLoadingMoreResults: Boolean,
    onLoadMoreResultsClick: () -> Unit,
    retrySearch: () -> Unit,
) {
    LazyColumn(
        contentPadding = remember { PaddingValues(horizontal = 16.dp) },
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding(bottom = false, start = true, end = true)
    ) {
        solutions
            .groupBy { solution -> solution.firstDepartureDateTime.date }
            .forEach { (date, trains) ->
                item {
                    TrainsGroupHeader(departureDate = date)
                }

                items(trains) { train ->
                    OneWaySolutionCard(
                        modifier = Modifier.fillMaxWidth(),
                        oneWaySolution = train
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

        if (solutions.isNotEmpty()) {
            item {
                LoadMoreResultsButton(
                    isLoading = isLoadingMoreResults,
                    onClick = onLoadMoreResultsClick
                )
                Spacer(Modifier.navigationBarsHeight(additional = 8.dp))
            }
        } else {
            item {
                RetrySearch(
                    onClick = retrySearch,
                    modifier = Modifier
                        .fillParentMaxSize()
                        .navigationBarsPadding()
                )
            }
        }
    }
}

@Composable
private fun TrainsGroupHeader(departureDate: LocalDate) {
    val configuration = LocalConfiguration.current
    val resources = LocalContext.current.resources
    val dayFullName = remember(configuration, departureDate, resources) {
        val today = Clock.System.todayAt(TimeZone.currentSystemDefault())
        when (departureDate) {
            today -> resources.getString(R.string.time_today)
            today.plus(1, DateTimeUnit.DAY) -> resources.getString(R.string.time_tomorrow)
            else -> {
                val locale = configuration.locales.get(0)
                val dayOfWeek = departureDate.dayOfWeek
                    .getDisplayName(TextStyle.FULL, locale)
                    .replaceFirstChar { it.titlecase(locale) }
                val month = departureDate.month.getDisplayName(TextStyle.FULL, locale)
                "$dayOfWeek, ${departureDate.dayOfMonth} $month"
            }
        }
    }
    Text(
        text = dayFullName,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
    )
}

@Composable
private fun LoadMoreResultsButton(isLoading: Boolean, onClick: () -> Unit) {
    FilledTonalButton(
        onClick = onClick,
        enabled = !isLoading,
        modifier = Modifier
            .padding(vertical = 16.dp)
            .fillMaxWidth()
            .wrapContentWidth()
    ) {
        AnimatedVisibility(visible = isLoading) {
            CircularProgressIndicator(
                color = LocalContentColor.current,
                strokeWidth = 2.dp,
                modifier = Modifier
                    .padding(end = 12.dp)
                    .size(16.dp)
            )
        }
        Text(text = stringResource(id = R.string.load_more_results_button_text))
    }
}

@Composable
private fun RetrySearch(modifier: Modifier, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(id = R.string.no_results_found_error),
            style = MaterialTheme.typography.displaySmall,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onClick) {
            Text(text = stringResource(id = R.string.retry_button_text))
        }
    }
}
