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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.manueldidonna.godottrains.GodotTrainsTheme

@Composable
fun DepartureTimeCard(
    modifier: Modifier = Modifier,
    cardElevation: Dp = 1.dp,
    cardShape: Shape = MaterialTheme.shapes.medium,
) {
    Card(elevation = cardElevation, modifier = modifier, shape = cardShape) {
        Column {
            CardHeader(
                modifier = Modifier.padding(top = 24.dp, start = 24.dp, end = 24.dp),
                icon = Icons.Rounded.Timelapse,
                text = "Departure Time"
            )
            LazyRow(
                contentPadding = remember { PaddingValues(horizontal = 24.dp, vertical = 32.dp) },
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(10) {
                    TimeCarouselEntry(
                        timeValue = "19 : 00",
                        selected = it == 0,
                        onClick = { /*TODO*/ }
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
        targetValue = if (selected) colors.primary else Color.Transparent
    )
    Text(
        text = timeValue,
        style = MaterialTheme.typography.body1,
        fontWeight = FontWeight.Medium,
        color = contentColorFor(backgroundColor),
        modifier = Modifier
            .clip(shape)
            .selectable(
                selected = selected,
                onClick = onClick,
                indication = rememberRipple(color = colors.primary),
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
) {
    Card(elevation = cardElevation, shape = cardShape, modifier = modifier) {
        Column {
            CardHeader(
                modifier = Modifier.padding(24.dp),
                icon = Icons.Rounded.Today,
                text = "Departure Day"
            )

            DepartureDateTabRow(selectedIndex = 0) {
                repeat(8) {
                    Tab(
                        selected = it == 0,
                        onClick = { /*TODO*/ },
                        text = { Text(text = "Wed 22") },
                        modifier = Modifier.height(64.dp),
                    )
                }
            }
        }
    }
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
                DepartureDateCard(modifier = Modifier.padding(24.dp))
                DepartureTimeCard(modifier = Modifier.padding(24.dp))
            }
        }
    }
}