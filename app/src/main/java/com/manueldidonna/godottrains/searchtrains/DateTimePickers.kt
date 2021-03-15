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
import androidx.compose.animation.core.animateIntOffsetAsState
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Timelapse
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.manueldidonna.godottrains.GodotTrainsTheme
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone
import java.time.format.TextStyle
import java.util.*
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
private fun TimeCarouselEntry(timeValue: String, selected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colors
    val shape = MaterialTheme.shapes.small
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) colors.primary else Color.Transparent,
        animationSpec = tween(durationMillis = 350)
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) colors.onPrimary else colors.onSurface,
        animationSpec = tween(durationMillis = 350)
    )
    Text(
        text = timeValue,
        style = MaterialTheme.typography.body1,
        fontWeight = FontWeight.Medium,
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
            .border(BorderStroke(2.dp, colors.primary), shape)
            .background(color = backgroundColor, shape = shape)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    )
}

@Composable
fun DepartureDateCard(
    modifier: Modifier = Modifier,
    cardElevation: Dp = 1.dp,
    cardShape: Shape = MaterialTheme.shapes.medium,
    selectedLocalDate: LocalDate,
    onLocalDateChange: (LocalDate) -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val locale = remember(configuration) { configuration.locales.get(0) }

    val availableDays = remember {
        val today = Clock.System.todayAt(TimeZone.currentSystemDefault())
        List(7) { today.plus(it, DateTimeUnit.DAY) }
    }

    Card(elevation = cardElevation, shape = cardShape, modifier = modifier) {
        Column {
            CardHeader(
                modifier = Modifier.padding(24.dp),
                icon = Icons.Rounded.Today,
                text = "Departure Day"
            )

            val selectedLocalDateIndex = remember(selectedLocalDate) {
                val index = availableDays.indexOf(selectedLocalDate)
                require(index >= 0) { "The selected date is not allowed " }
                return@remember index
            }

            DepartureDateTabRow(selectedIndex = selectedLocalDateIndex) {
                availableDays.forEachIndexed { index, localDate ->
                    Tab(
                        selected = index == selectedLocalDateIndex,
                        onClick = { onLocalDateChange(localDate) },
                        text = { Text(text = localDate.getDisplayName(locale)) },
                        modifier = Modifier.height(64.dp),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalStdlibApi::class)
@Stable
@Composable
private fun LocalDate.getDisplayName(locale: Locale): String {
    val dayName = remember(dayOfWeek) {
        dayOfWeek
            .getDisplayName(TextStyle.SHORT, locale)
            .replaceFirstChar { it.uppercaseChar() }
    }
    return "$dayName $dayOfMonth"
}

@Composable
private fun DepartureDateTabRow(selectedIndex: Int, tabs: @Composable () -> Unit) {
    ScrollableTabRow(
        selectedTabIndex = selectedIndex,
        divider = {},
        edgePadding = 24.dp,
        backgroundColor = MaterialTheme.colors.primary,
        contentColor = MaterialTheme.colors.onPrimary,
        indicator = { tapPositions ->
            Box(
                Modifier
                    .fillMaxWidth()
                    .departureDateTabIndicatorOffset(tapPositions[selectedIndex])
                    .height(4.dp)
                    .clip(departureDateTabIndicatorShape())
                    .background(color = LocalContentColor.current)
            )
        },
        tabs = tabs
    )
}

@Stable
@Composable
private fun departureDateTabIndicatorShape(): Shape {
    return remember { RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp) }
}

private fun Modifier.departureDateTabIndicatorOffset(currentTabPosition: TabPosition): Modifier {
    return composed(
        inspectorInfo = debugInspectorInfo {
            name = "departureDateTabIndicatorOffset"
            value = currentTabPosition
        }
    ) {
        val indicatorWidth by animateDpAsState(
            targetValue = currentTabPosition.width,
            animationSpec = tween(durationMillis = 250)
        )
        val offsetX = with(LocalDensity.current) { currentTabPosition.left.roundToPx() }
        val indicatorOffset by animateIntOffsetAsState(
            targetValue = IntOffset(x = offsetX, y = 0),
            animationSpec = tween(durationMillis = 250)
        )
        return@composed this
            .wrapContentSize(Alignment.BottomStart)
            .offset { indicatorOffset }
            .width(indicatorWidth)
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

@Preview
@Composable
private fun PreviewDateTimePickers() {
    GodotTrainsTheme {
        Surface {
            Column {
                DepartureDateCard(
                    modifier = Modifier.padding(24.dp),
                    selectedLocalDate = Clock.System.todayAt(TimeZone.currentSystemDefault())
                )
                DepartureTimeCard(modifier = Modifier.padding(24.dp))
            }
        }
    }
}
