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
package com.manueldidonna.godottrains.trainsresult

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.manueldidonna.godottrains.entities.OneWaySolution
import dev.chrisbanes.accompanist.insets.navigationBarsHeight
import dev.chrisbanes.accompanist.insets.navigationBarsPadding
import dev.chrisbanes.accompanist.insets.statusBarsPadding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDateTime
import java.time.format.DateTimeFormatter

interface TrainsResultCallback {
    fun getOneWaySolutions(): Flow<List<OneWaySolution>>
    suspend fun loadNextOneWaySolutions()
}

@Composable
fun TrainsResultScreen(callback: TrainsResultCallback) {
    val updatedCallback by rememberUpdatedState(callback)

    val oneWaySolutionsGroupedByDay: Map<Int, List<OneWaySolution>> by remember(updatedCallback) {
        updatedCallback.getOneWaySolutions()
            .map { it.groupBy { solution -> solution.departureDateTime.dayOfYear } }
            .flowOn(Dispatchers.Default)
    }.collectAsState(emptyMap())

    var isLoadingNextSolutions by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        TrainsResultAppBar(modifier = Modifier.zIndex(8f))

        Crossfade(
            targetState = oneWaySolutionsGroupedByDay.isEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            animationSpec = remember { tween(durationMillis = 250) }
        ) { showLoadingIndicator ->
            if (showLoadingIndicator) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize()
                        .navigationBarsPadding(),
                    strokeWidth = 8.dp
                )
            } else {
                OneWaySolutionsList(
                    contentPadding = remember { PaddingValues(top = 16.dp, bottom = 24.dp) },
                    oneWaySolutionsGroupedByDay = oneWaySolutionsGroupedByDay,
                    isLoadingNextSolutions = isLoadingNextSolutions,
                    loadNextOneWaySolutions = {
                        isLoadingNextSolutions = true
                        scope.launch {
                            updatedCallback.loadNextOneWaySolutions()
                            isLoadingNextSolutions = false
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun TrainsResultAppBar(modifier: Modifier = Modifier) {
    Surface(
        elevation = AppBarDefaults.TopAppBarElevation,
        color = MaterialTheme.colors.surface,
        contentColor = MaterialTheme.colors.onSurface,
        modifier = modifier
    ) {
        TopAppBar(
            title = { Text("One Way Solutions") },
            backgroundColor = Color.Transparent,
            contentColor = LocalContentColor.current,
            elevation = 0.dp,
            modifier = Modifier.statusBarsPadding(),
            navigationIcon = {
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(Icons.Rounded.ArrowBack, contentDescription = "go back")
                }
            }
        )
    }
}

@Composable
private fun OneWaySolutionsList(
    contentPadding: PaddingValues,
    oneWaySolutionsGroupedByDay: Map<Int, List<OneWaySolution>>,
    isLoadingNextSolutions: Boolean,
    loadNextOneWaySolutions: () -> Unit
) {
    LazyColumn(contentPadding = contentPadding) {
        oneWaySolutionsGroupedByDay.forEach { (_, oneWaySolutions) ->
            item {
                OneWaySolutionsListHeader(
                    departureDateTime = oneWaySolutions.first().departureDateTime,
                    modifier = Modifier.padding(top = 24.dp, start = 24.dp, bottom = 16.dp)
                )
            }
            items(oneWaySolutions) { solution ->
                TrainSolutionDetailsCard(
                    cardShape = MaterialTheme.shapes.medium,
                    cardElevation = 2.dp,
                    modifier = Modifier
                        .padding(vertical = 12.dp, horizontal = 24.dp)
                        .fillMaxWidth(),
                    oneWaySolution = solution
                )
            }
        }

        item {
            LoadNextSolutionsButton(
                modifier = Modifier.padding(vertical = 16.dp),
                isLoading = isLoadingNextSolutions,
                onClick = loadNextOneWaySolutions
            )
        }
        item { Spacer(Modifier.navigationBarsHeight()) }
    }
}

@OptIn(ExperimentalStdlibApi::class)
@Composable
private fun OneWaySolutionsListHeader(modifier: Modifier, departureDateTime: LocalDateTime) {
    val configuration = LocalConfiguration.current
    val dayFullName = remember(configuration, departureDateTime) {
        val locale = configuration.locales.get(0)
        departureDateTime
            .toJavaLocalDateTime()
            .format(DateTimeFormatter.ofPattern("EEEE, dd MMMM", locale))
    }
    Text(
        text = dayFullName,
        style = MaterialTheme.typography.subtitle2,
        color = LocalContentColor.current.copy(alpha = ContentAlpha.medium),
        modifier = modifier
    )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun LoadNextSolutionsButton(
    modifier: Modifier,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .height(56.dp)
            .fillMaxWidth()
            .clickable(
                enabled = !isLoading,
                onClick = onClick,
                indication = rememberRipple(color = MaterialTheme.colors.primary),
                interactionSource = remember { MutableInteractionSource() }
            )
    ) {

        val contentColor =
            if (!isLoading) MaterialTheme.colors.primary
            else LocalContentColor.current.copy(alpha = ContentAlpha.disabled)

        AnimatedVisibility(visible = isLoading, initiallyVisible = false) {
            CircularProgressIndicator(
                color = contentColor,
                modifier = Modifier
                    .padding(end = 12.dp)
                    .size(24.dp)
            )
        }

        Text(
            text = "LOAD NEXT SOLUTIONS",
            color = contentColor,
            style = MaterialTheme.typography.button
        )
    }
}