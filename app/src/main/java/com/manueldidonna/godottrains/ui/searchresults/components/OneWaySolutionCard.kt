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
package com.manueldidonna.godottrains.ui.searchresults.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.manueldidonna.godottrains.GodotTrainsTheme
import com.manueldidonna.godottrains.ThemeShapes
import com.manueldidonna.godottrains.data.models.OneWaySolution
import com.manueldidonna.godottrains.data.models.Train
import kotlinx.datetime.*

@Composable
fun OneWaySolutionCard(
    modifier: Modifier = Modifier,
    oneWaySolution: OneWaySolution
) {
    TrainCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            oneWaySolution.trains.forEach { train ->
                Text(
                    text = train.name,
                    style = MaterialTheme.typography.headlineSmall,
                )
                Spacer(modifier = Modifier.height(16.dp))
                StartStopStations(train)
                Spacer(modifier = Modifier.height(24.dp))
            }
            DashedDivider()
            Spacer(modifier = Modifier.height(16.dp))
            TravelTimeLabel(timeInMinutes = oneWaySolution.durationInMinutes)
        }
    }
}

@Composable
private fun TrainCard(modifier: Modifier, content: @Composable () -> Unit) {
    // TODO: remove when M3 card support will be added
    Surface(
        shape = ThemeShapes.Card,
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        content = content,
        modifier = modifier
    )
}

@Composable
private fun StartStopStations(train: Train) {
    Layout(
        modifier = Modifier.padding(start = 16.dp),
        content = {
            StationInfo(
                stationName = train.departureStation,
                arriveAt = train.departureDateTime,
            )
            StationInfo(
                stationName = train.arrivalStation,
                arriveAt = train.arrivalDateTime,
            )
            TrackBetweenStations(
                modifier = Modifier.fillMaxHeight(),
                stationPointCircleRadius = 8.dp,
                trackWidth = 3.dp
            )
        }
    ) { measurables, constraints ->
        var layoutHeight = 0
        val placeables = buildList(3) {
            add(measurables[0].measure(constraints).also { layoutHeight += it.height })
            add(measurables[1].measure(constraints).also { layoutHeight += it.height })
            val trackHeight = layoutHeight / 2 + 32.dp.roundToPx()
            layoutHeight += 16.dp.roundToPx()
            add(measurables[2].measure(constraints.copy(maxHeight = trackHeight)))
        }
        layout(constraints.maxWidth, layoutHeight) {
            val verticalAlignmentX = placeables[2].measuredWidth + 24.dp.roundToPx()
            val firstPlaceableHeight = placeables.first().measuredHeight
            placeables[2].placeRelative(x = 0, y = firstPlaceableHeight / 2 - 8.dp.roundToPx())
            placeables[0].placeRelative(x = verticalAlignmentX, y = 0)
            placeables[1].placeRelative(
                x = verticalAlignmentX,
                y = firstPlaceableHeight + 16.dp.roundToPx()
            )
        }
    }
}

@Composable
private fun TrackBetweenStations(modifier: Modifier, stationPointCircleRadius: Dp, trackWidth: Dp) {
    val departurePointColor = LocalContentColor.current
    val arrivalPointColor = MaterialTheme.colorScheme.primary
    val trackGradient = remember(departurePointColor, arrivalPointColor) {
        Brush.verticalGradient(
            0.25f to departurePointColor,
            0.8f to arrivalPointColor,
        )
    }
    Canvas(modifier = modifier.width(16.dp)) {
        val circleRadiusPx = stationPointCircleRadius.toPx()
        drawLine(
            brush = trackGradient,
            strokeWidth = trackWidth.toPx(),
            start = Offset(x = circleRadiusPx, y = circleRadiusPx * 2 - 1.dp.toPx()),
            end = Offset(x = circleRadiusPx, y = size.height - (circleRadiusPx * 2) + 1.dp.toPx())
        )
        drawCircle(
            color = departurePointColor,
            center = Offset(x = circleRadiusPx, y = circleRadiusPx),
            radius = circleRadiusPx
        )
        drawCircle(
            color = arrivalPointColor,
            center = Offset(x = circleRadiusPx, y = size.height - circleRadiusPx)
        )
    }
}

@Composable
private fun StationInfo(stationName: String, arriveAt: LocalDateTime) {
    Column {
        Text(
            text = String.format("%02d : %02d", arriveAt.hour, arriveAt.minute),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stationName,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun DashedDivider() {
    val density = LocalDensity.current
    val pathEffect = remember(density) {
        val strokeDash = with(density) { 4.dp.toPx() }
        PathEffect.dashPathEffect(floatArrayOf(strokeDash, strokeDash), 0f)
    }
    val outlineColor = MaterialTheme.colorScheme.outline
    Canvas(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
    ) {
        drawLine(
            color = outlineColor,
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            pathEffect = pathEffect,
            cap = StrokeCap.Round,
            strokeWidth = 1.dp.toPx()
        )
    }
}

@Composable
private fun TravelTimeLabel(timeInMinutes: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Filled.Timelapse,
            modifier = Modifier.padding(end = 12.dp),
            contentDescription = "Travel time",
        )
        Text(
            text = "Travel Time $timeInMinutes min",
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Preview
@Composable
private fun PreviewTrainSolutionDetails() {
    GodotTrainsTheme(darkTheme = false) {
        Surface {
            val now = Clock.System.now()
            val timeZone = TimeZone.currentSystemDefault()
            val train = Train(
                name = "MET 21300",
                departureStation = "Torre del Greco",
                departureDateTime = now.toLocalDateTime(timeZone),
                arrivalStation = "Napoli Piazza Garibaldi",
                arrivalDateTime = now.plus(21, DateTimeUnit.MINUTE).toLocalDateTime(timeZone)
            )
            val solution = OneWaySolution(
                id = "",
                trains = listOf(train),
                durationInMinutes = 21
            )
            Column {
                OneWaySolutionCard(
                    modifier = Modifier.padding(8.dp),
                    oneWaySolution = solution
                )
                GodotTrainsTheme(darkTheme = true) {
                    OneWaySolutionCard(
                        modifier = Modifier.padding(8.dp),
                        oneWaySolution = solution
                    )
                }

                TrackBetweenStations(
                    Modifier.height(160.dp),
                    stationPointCircleRadius = 8.dp,
                    trackWidth = 3.dp
                )
            }
        }
    }
}
