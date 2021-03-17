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
package com.manueldidonna.godottrains.onewaysolutions

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Train
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.manueldidonna.godottrains.GodotTrainsTheme
import com.manueldidonna.godottrains.entities.OneWaySolution
import com.manueldidonna.godottrains.entities.Train
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun OneWayTrainSolutionCard(
    modifier: Modifier = Modifier,
    cardElevation: Dp = 1.dp,
    cardShape: Shape = MaterialTheme.shapes.medium,
    oneWaySolution: OneWaySolution
) {
    Card(
        shape = cardShape,
        modifier = modifier,
        elevation = cardElevation,
        backgroundColor = MaterialTheme.colors.primarySurface,
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 24.dp, start = 24.dp, end = 24.dp, bottom = 16.dp)
            ) {
                TrainsVerticalList(trains = oneWaySolution.trains)
                Spacer(Modifier.weight(1f))
                SolutionPrice(priceInEuro = oneWaySolution.priceInEuro)
            }
            Divider(color = LocalContentColor.current.copy(alpha = 0.12f))
            SolutionsTimeInfo(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .height(36.dp),
                departureHour = oneWaySolution.departureDateTime.hour,
                departureMinute = oneWaySolution.departureDateTime.minute,
                durationInMinutes = oneWaySolution.durationInMinutes
            )
        }
    }
}

@Composable
private fun SolutionsTimeInfo(
    modifier: Modifier,
    departureHour: Int,
    departureMinute: Int,
    durationInMinutes: Int
) {
    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
            Icon(imageVector = Icons.Rounded.Flag, contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Text(
                text = remember(departureHour, departureMinute) {
                    String.format("%02d : %02d", departureHour, departureMinute)
                },
                style = MaterialTheme.typography.subtitle2
            )
            Spacer(modifier = Modifier.width(24.dp))
            Icon(imageVector = Icons.Rounded.Timer, contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Text(
                text = "$durationInMinutes MIN",
                style = MaterialTheme.typography.subtitle2
            )
        }
    }
}

@Composable
private fun TrainsVerticalList(modifier: Modifier = Modifier, trains: List<Train>) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (index in trains.indices) {
            TrainName(name = trains[index].name)
        }
    }
}

@Composable
private fun TrainName(name: String, modifier: Modifier = Modifier) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier.height(48.dp)) {
        Icon(
            imageVector = Icons.Rounded.Train,
            modifier = Modifier.padding(end = 12.dp),
            contentDescription = null,
        )
        Text(text = name, style = MaterialTheme.typography.body1, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun SolutionPrice(modifier: Modifier = Modifier, priceInEuro: Double) {
    val priceString = if (priceInEuro > 0.0) "€${String.format("%.2f", priceInEuro)}" else "NaN"
    Text(
        text = priceString,
        modifier = modifier,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.body1,
    )
}

@Preview
@Composable
private fun PreviewTrainSolutionDetails() {
    GodotTrainsTheme(darkTheme = false) {
        Surface {
            val solution = OneWaySolution(
                trains = listOf(Train("MET 21300")),
                departureDateTime = Clock.System.now()
                    .toLocalDateTime(TimeZone.currentSystemDefault()),
                priceInEuro = 2.20,
                durationInMinutes = 21
            )
            Column {
                OneWayTrainSolutionCard(
                    modifier = Modifier.padding(8.dp),
                    oneWaySolution = solution
                )
                GodotTrainsTheme(darkTheme = true) {
                    OneWayTrainSolutionCard(
                        modifier = Modifier.padding(8.dp),
                        oneWaySolution = solution
                    )
                }
            }
        }
    }
}
