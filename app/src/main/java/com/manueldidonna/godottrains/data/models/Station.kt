package com.manueldidonna.godottrains.data.models

import androidx.compose.runtime.Immutable

@Immutable
data class Station(
    val id: Int,
    val name: String,
    val shortName: String
)
