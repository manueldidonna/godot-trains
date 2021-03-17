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
package com.manueldidonna.godottrains.searchtrains

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.manueldidonna.godottrains.GodotTrainsTheme

@Composable
fun StationsDisplayCard(
    modifier: Modifier = Modifier,
    cardElevation: Dp = 1.dp,
    cardShape: Shape = MaterialTheme.shapes.medium,
    departureStationName: String? = null,
    arrivalStationName: String? = null,
    onDepartureStationNameClick: () -> Unit = {},
    onArrivalStationNameClick: () -> Unit = {},
    onSwapStationsButtonClick: () -> Unit = {}
) {
    Card(shape = cardShape, elevation = cardElevation, modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                StationName(
                    modifier = Modifier
                        .padding(start = 24.dp, top = 16.dp, bottom = 8.dp)
                        .fillMaxWidth(),
                    text = departureStationName,
                    placeHolderText = "Departure Station",
                    onClick = onDepartureStationNameClick
                )
                Divider(startIndent = 24.dp)
                StationName(
                    modifier = Modifier
                        .padding(start = 24.dp, top = 8.dp, bottom = 16.dp)
                        .fillMaxWidth(),
                    text = arrivalStationName,
                    placeHolderText = "Arrival Station",
                    onClick = onArrivalStationNameClick
                )
            }
            IconButton(
                onClick = onSwapStationsButtonClick,
                modifier = Modifier.padding(8.dp),
                enabled = departureStationName?.isNotEmpty() == true && arrivalStationName?.isNotEmpty() == true
            ) {
                Icon(
                    imageVector = Icons.Rounded.SwapVert,
                    contentDescription = "swap stations button",
                    tint = MaterialTheme.colors.primary.copy(alpha = LocalContentAlpha.current)
                )
            }
        }
    }
}

@Composable
private fun StationName(
    modifier: Modifier,
    text: String?,
    placeHolderText: String,
    onClick: () -> Unit
) {
    val textToShow = if (text.isNullOrEmpty()) placeHolderText else text
    val iconTintAlpha = if (text.isNullOrEmpty()) ContentAlpha.disabled else ContentAlpha.medium
    val textColorAlpha = if (text.isNullOrEmpty()) ContentAlpha.disabled else ContentAlpha.high
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .clickable(
                onClick = onClick,
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(color = MaterialTheme.colors.primary)
            )
            .padding(vertical = 16.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.Place,
            contentDescription = "station location icon",
            tint = LocalContentColor.current.copy(alpha = iconTintAlpha)
        )
        Spacer(Modifier.width(20.dp))
        Text(
            text = textToShow,
            style = MaterialTheme.typography.body1,
            color = LocalContentColor.current.copy(alpha = textColorAlpha)
        )
    }
}

@Preview
@Composable
private fun PreviewStationsDisplay() {
    GodotTrainsTheme {
        Surface {
            StationsDisplayCard(
                modifier = Modifier.padding(24.dp),
                cardElevation = 4.dp,
                departureStationName = "Torre del Greco"
            )
        }
    }
}
