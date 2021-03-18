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
package com.manueldidonna.godottrains.network

import com.manueldidonna.godottrains.entities.OneWaySolution
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.*
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

interface LeFrecceApi {
    suspend fun getStationsByPartialName(partialName: String): List<String>

    suspend fun getOneWaySolutions(
        departureStationName: String,
        arrivalStationName: String,
        firstDepartureDateTime: LocalDateTime
    ): List<OneWaySolution>

    companion object {
        val Ktor: LeFrecceApi by lazy { KtorLeFrecceApi() }
    }
}

private class KtorLeFrecceApi : LeFrecceApi {
    private val client = HttpClient(OkHttp)
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun getStationsByPartialName(partialName: String): List<String> {
        if (partialName.isBlank() || partialName.length < 3) return emptyList()
        val response: String = client.get("$ApiPath/geolocations/locations") {
            parameter("name", partialName)
        }
        return json
            .parseToJsonElement(response)
            .jsonArray
            .mapNotNull { it.jsonObject["name"]?.jsonPrimitive?.content }
    }

    @OptIn(ExperimentalStdlibApi::class)
    override suspend fun getOneWaySolutions(
        departureStationName: String,
        arrivalStationName: String,
        firstDepartureDateTime: LocalDateTime
    ): List<OneWaySolution> {
        var solutions: List<OneWaySolution> = emptyList()
        var offset = 0
        while (solutions.isEmpty()) {
            // avoid too many requests, if the fifth request doesn't return a result
            // there should be an error in the api endpoint
            if (offset >= 20) return emptyList()
            val response = executeGetOneWaySolutionsRequest(
                departureStationName = departureStationName,
                arrivalStationName = arrivalStationName,
                firstDepartureDateTime = firstDepartureDateTime,
                requestOffset = offset
            )
            solutions = json
                .decodeFromString(ListSerializer(OneWaySolution.serializer()), response)
                .filter { solution ->
                    solution.departureDateTime >= firstDepartureDateTime
                }
            offset += 5 // each request returns 5 elements
        }
        return solutions
    }

    @OptIn(ExperimentalStdlibApi::class)
    private suspend fun executeGetOneWaySolutionsRequest(
        departureStationName: String,
        arrivalStationName: String,
        firstDepartureDateTime: LocalDateTime,
        requestOffset: Int
    ): String = client.get("$ApiPath/solutions") {
        parameter("origin", departureStationName.uppercase())
        parameter("destination", arrivalStationName.uppercase())
        parameter("arflag", "A")
        parameter("adultno", 1)
        parameter("childno", 0)
        parameter("direction", "A")
        parameter("frecce", "false")
        parameter("onlyRegional", "true")
        parameter("adate", firstDepartureDateTime.formatDate())
        parameter("atime", firstDepartureDateTime.formatTime())
        parameter("offset", requestOffset.toString())
    }

    private fun LocalDateTime.formatDate() =
        String.format("%02d/%02d/$year", dayOfMonth, monthNumber)

    private fun LocalDateTime.formatTime() = String.format("%02d:%02d", hour, minute)

    companion object {
        private const val ApiPath = "https://www.lefrecce.it/msite/api"
    }
}
