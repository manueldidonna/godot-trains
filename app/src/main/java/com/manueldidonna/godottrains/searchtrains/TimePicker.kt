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

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Timelapse
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.manueldidonna.godottrains.GodotTrainsTheme
import kotlin.math.abs

@Stable
private val AllowedTimesInMinutes = List(33) { (it + 12) * 30 }

@Composable
fun DepartureTimeCard(
    modifier: Modifier = Modifier,
    cardElevation: Dp = 1.dp,
    cardShape: Shape = MaterialTheme.shapes.medium,
    selectedTimeInMinutes: Int = 0,
    onTimeChange: (Int) -> Unit = {}
) {
    Card(elevation = cardElevation, modifier = modifier, shape = cardShape) {
        Column {
            CardHeader(
                modifier = Modifier.padding(top = 24.dp, start = 24.dp, end = 24.dp),
                icon = Icons.Rounded.Timelapse,
                text = "Departure Time"
            )

            val adjustedTimeInMinutes = remember(selectedTimeInMinutes) {
                AllowedTimesInMinutes.minByOrNull { abs(selectedTimeInMinutes - it) }
                    ?: AllowedTimesInMinutes.first()
            }

            val lazyListState = rememberLazyListState(
                initialFirstVisibleItemIndex = AllowedTimesInMinutes.indexOf(adjustedTimeInMinutes)
            )

            LazyRow(
                contentPadding = remember { PaddingValues(horizontal = 24.dp, vertical = 32.dp) },
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                state = lazyListState
            ) {
                items(AllowedTimesInMinutes) { minutes ->
                    TimeCarouselEntry(
                        timeValue = String.format("%02d : %02d", minutes / 60, minutes % 60),
                        selected = adjustedTimeInMinutes == minutes,
                        onClick = { onTimeChange(minutes) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CardHeader(modifier: Modifier, icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Icon(icon, contentDescription = text)
        Spacer(Modifier.width(12.dp))
        Text(text = text, style = MaterialTheme.typography.subtitle2)
    }
}

@Composable
private fun TimeCarouselEntry(timeValue: String, selected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colors
    val shape = MaterialTheme.shapes.small
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) colors.primary else Color.Transparent,
        animationSpec = tween(durationMillis = 450)
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) colors.onPrimary else colors.onSurface.copy(alpha = ContentAlpha.medium),
        animationSpec = tween(durationMillis = 450)
    )
    val borderStrokeWidth by animateDpAsState(
        targetValue = if (selected) 4.dp else 2.dp,
        animationSpec = tween(durationMillis = 450)
    )
    Text(
        text = timeValue,
        style = MaterialTheme.typography.button,
        // fontWeight = FontWeight.Bold,
        color = textColor,
        modifier = Modifier
            .clip(shape)
            .selectable(
                selected = selected,
                onClick = onClick,
                indication = rememberRipple(
                    color = if (selected) colors.onPrimary else colors.primary
                ),
                interactionSource = remember { MutableInteractionSource() }
            )
            .background(color = backgroundColor, shape = shape)
            .border(
                border = BorderStroke(
                    width = borderStrokeWidth,
                    color = textColor.copy(alpha = ContentAlpha.medium)
                ),
                shape = shape
            )
            .padding(horizontal = 20.dp, vertical = 16.dp)
    )
}

@Preview
@Composable
private fun PreviewTimePicker() {
    GodotTrainsTheme {
        Surface {
            Column {
                DepartureTimeCard(modifier = Modifier.padding(24.dp))
            }
        }
    }
}
