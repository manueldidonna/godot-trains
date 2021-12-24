package com.manueldidonna.godottrains.data.models

import androidx.compose.runtime.Immutable
import kotlinx.datetime.LocalDateTime

@Immutable
data class Train(
    val name: String,
    val departureStation: String,
    val departureDateTime: LocalDateTime,
    val arrivalStation: String,
    val arrivalDateTime: LocalDateTime
)
