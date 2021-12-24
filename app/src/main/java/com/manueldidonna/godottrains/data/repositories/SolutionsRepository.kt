package com.manueldidonna.godottrains.data.repositories

import com.manueldidonna.godottrains.data.models.OneWaySolution
import com.manueldidonna.godottrains.data.sources.TrenitaliaSolutionsRemoteDataSource
import kotlinx.datetime.LocalDateTime

class SolutionsRepository(
    private val trenitaliaSolutionsRemoteDataSource: TrenitaliaSolutionsRemoteDataSource
) {
    suspend fun getOneWaySolutions(
        departureStationId: Int,
        arrivalStationId: Int,
        departureDateTime: LocalDateTime
    ): List<OneWaySolution> {
        return trenitaliaSolutionsRemoteDataSource.getOneWaySolutions(
            departureStationId = departureStationId,
            arrivalStationId = arrivalStationId,
            departureDateTime = departureDateTime
        )
    }
}
