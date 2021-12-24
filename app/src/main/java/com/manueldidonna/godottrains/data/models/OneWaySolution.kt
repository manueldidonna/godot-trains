package com.manueldidonna.godottrains.data.models

import androidx.compose.runtime.Immutable
import kotlinx.datetime.LocalDateTime

@Immutable
data class OneWaySolution(
    val id: String,
    val trains: List<Train>,
    val durationInMinutes: Int
)

val OneWaySolution.firstDepartureDateTime: LocalDateTime
    get() = trains.first().departureDateTime
