package com.manueldidonna.godottrains.ui.searchtrains.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.manueldidonna.godottrains.R
import com.manueldidonna.godottrains.ThemeShapes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*
import kotlin.math.ceil


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DateTimeInlinePicker(
    selection: LocalDateTime,
    onSelectionChange: (LocalDateTime) -> Unit
) {
    var yearMonth by remember {
        mutableStateOf(YearMonth.of(selection.year, selection.monthNumber))
    }

    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TimesRow(selection, onSelectionChange)
        YearMonthStepper(
            yearMonth = yearMonth,
            onPreviousButtonClick = { yearMonth = yearMonth.minusMonths(1) },
            onNextButtonClick = { yearMonth = yearMonth.plusMonths(1) }
        )
        DayLabelsRow()
        AnimatedContent(
            targetState = yearMonth,
            transitionSpec = {
                if (targetState > initialState) {
                    slideInHorizontally { width -> width } + fadeIn() with
                            slideOutHorizontally { width -> -width } + fadeOut()
                } else {
                    slideInHorizontally { width -> -width } + fadeIn() with
                            slideOutHorizontally { width -> width } + fadeOut()
                }
            }
        ) { yearMonth ->
            CalendarGrid(
                selection = selection,
                onSelectionChange = onSelectionChange,
                yearMonth = yearMonth
            )
        }

    }
}

private fun LocalDateTime.copy(
    year: Int = this.year,
    monthNumber: Int = this.monthNumber,
    dayOfMonth: Int = this.dayOfMonth,
    hour: Int = this.hour,
    minute: Int = this.minute
) = LocalDateTime(year, monthNumber, dayOfMonth, hour, minute, second, nanosecond)

@Immutable
private data class LocalTime(val hour: Int, val minute: Int)

@Composable
private fun TimesRow(selection: LocalDateTime, onSelectionChange: (LocalDateTime) -> Unit) {
    val selectedLocalTime = remember(selection) {
        if (selection.hour < 6) {
            LocalTime(6, 0)
        } else if (selection.hour > 21) {
            LocalTime(21, 30)
        } else {
            LocalTime(selection.hour, selection.minute)
        }
    }

    val listState = rememberLazyListState()
    LaunchedEffect(selectedLocalTime) {
        val itemIndex = selectedLocalTime.hour - 6
        val visibleItemsInfo = listState.layoutInfo.visibleItemsInfo
        if (itemIndex !in visibleItemsInfo.first().index..visibleItemsInfo.last().index) {
            listState.scrollToItem(itemIndex)
        }
    }

    LazyRow(
        modifier = Modifier.padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        state = listState
    ) {
        items(16) {
            TimeChip(
                hour = it + 6,
                selectedLocalTime = selectedLocalTime,
                onClick = { hour, minute ->
                    onSelectionChange(selection.copy(hour = hour, minute = minute))
                }
            )
        }
    }
}

@Composable
private fun TimeChip(
    hour: Int,
    selectedLocalTime: LocalTime,
    onClick: (hour: Int, minute: Int) -> Unit
) {
    val expanded = selectedLocalTime.hour == hour
    Row(
        modifier = Modifier
            .height(32.dp)
            .border(1.dp, MaterialTheme.colorScheme.outline, ThemeShapes.Chip)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TimeChipTextContent(
            text = "%02d : %02d".format(hour, 0),
            selected = expanded && selectedLocalTime.minute < 30,
            onClick = { onClick(hour, 0) }
        )

        AnimatedVisibility(visible = expanded) {
            TimeChipDivider()
        }

        AnimatedVisibility(visible = expanded) {
            TimeChipTextContent(
                text = "%02d : %02d".format(hour, 30),
                selected = selectedLocalTime.minute >= 30,
                onClick = { onClick(hour, 30) }
            )
        }

    }
}

@Composable
private fun TimeChipTextContent(text: String, selected: Boolean, onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = if (selected) colorScheme.onSecondaryContainer else colorScheme.onSurface,
        modifier = Modifier
            .clip(InnerTimeChipShape)
            .background(color = if (selected) colorScheme.secondaryContainer else colorScheme.surface)
            .clickable(onClick = onClick)
            .fillMaxHeight()
            .wrapContentHeight()
            .padding(horizontal = 12.dp)
    )
}

@Composable
private fun TimeChipDivider() {
    Box(
        modifier = Modifier
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .background(MaterialTheme.colorScheme.outline)
            .fillMaxHeight()
            .width(1.dp)
    )
}

@Composable
private fun YearMonthStepper(
    yearMonth: YearMonth,
    onPreviousButtonClick: () -> Unit,
    onNextButtonClick: () -> Unit,
) {
    val text = remember(yearMonth) {
        val month = yearMonth.month
            .getDisplayName(TextStyle.FULL, Locale.getDefault())
            .replaceFirstChar { it.titlecase(Locale.getDefault()) }
        "$month ${yearMonth.year}"
    }

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxSize()
    ) {
        IconButton(onClick = onPreviousButtonClick) {
            Icon(
                imageVector = Icons.Filled.ChevronLeft,
                contentDescription = stringResource(id = R.string.select_prev_month_action)
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(180.dp)
        )
        IconButton(onClick = onNextButtonClick) {
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = stringResource(id = R.string.select_next_month_action)
            )
        }
    }
}

@Composable
private fun DayLabelsRow() {
    val weekLabels = remember {
        DayOfWeek.values().map { dayOfWeek ->
            dayOfWeek
                .getDisplayName(TextStyle.NARROW, Locale.getDefault())
                .replaceFirstChar { it.titlecase(Locale.getDefault()) }
        }
    }
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
        weekLabels.forEach { dayLabel ->
            Text(
                text = dayLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.66f),
                modifier = Modifier
                    .width(width = CellWidth)
                    .wrapContentWidth()
            )
        }
    }
}

private val DaysGroupedByWeekPlaceholder = List(5) { List(7) { "" } }

@Composable
private fun CalendarGrid(
    yearMonth: YearMonth,
    selection: LocalDateTime,
    onSelectionChange: (LocalDateTime) -> Unit,
) {
    var daysGroupedByWeek: List<List<String>> by remember {
        mutableStateOf(DaysGroupedByWeekPlaceholder)
    }

    LaunchedEffect(yearMonth) {
        withContext(Dispatchers.Default) {
            val daysInMonth = yearMonth.lengthOfMonth()
            val daysInFirstWeek = 7 - yearMonth.atDay(1).dayOfWeek.value + 1
            val startPadding = 7 - daysInFirstWeek
            val numberOfRows = ceil((daysInMonth + startPadding).toDouble() / 7).toInt()
            val endPadding = (numberOfRows * 7) - daysInMonth - startPadding
            if (isActive) {
                daysGroupedByWeek = buildList(startPadding + daysInMonth + endPadding) {
                    repeat(startPadding) { add("") }
                    repeat(daysInMonth) { add((it + 1).toString()) }
                    repeat(endPadding) { add("") }
                }.chunked(7)
            }
        }
    }

    val selectedDay: String? = remember(selection, yearMonth) {
        val date = selection.date
        if (date.year == yearMonth.year && date.monthNumber == yearMonth.monthValue) {
            return@remember date.dayOfMonth.toString()
        }
        return@remember null
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        daysGroupedByWeek.forEach { week ->
            CalendarRow(
                cells = week,
                onSelectionChange = { dayString ->
                    val newSelection = selection.copy(
                        year = yearMonth.year,
                        monthNumber = yearMonth.monthValue,
                        dayOfMonth = dayString.toInt()
                    )
                    onSelectionChange(newSelection)
                },
                selectedDay = selectedDay
            )
        }
    }
}

@Composable
private fun CalendarRow(
    cells: List<String>,
    selectedDay: String?,
    onSelectionChange: (String) -> Unit
) {
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
        cells.forEach { day ->
            CalendarCell(
                day = day,
                selected = day == selectedDay,
                onSelectionChange = { onSelectionChange(day) }
            )
        }
    }
}

@Composable
private fun CalendarCell(day: String, selected: Boolean, onSelectionChange: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    val conditionalCellModifier =
        if (day.isNotEmpty()) Modifier
            .run { if (selected) background(colorScheme.secondaryContainer) else this }
            .clickable(onClick = onSelectionChange)
            .wrapContentSize()
        else Modifier
    Text(
        text = day,
        style = MaterialTheme.typography.labelLarge,
        color = if (selected) colorScheme.onSecondaryContainer else colorScheme.onSurface,
        modifier = Modifier
            .size(width = CellWidth, height = CellHeight)
            .clip(ThemeShapes.Chip)
            .then(conditionalCellModifier)
    )
}

private val CellWidth = 40.dp
private val CellHeight = 36.dp
private val InnerTimeChipShape = RoundedCornerShape(4.dp)
