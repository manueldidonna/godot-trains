package com.manueldidonna.godottrains.data.sources

import android.util.Log
import com.manueldidonna.godottrains.data.models.OneWaySolution
import com.manueldidonna.godottrains.data.models.Train
import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import javax.inject.Inject

class TrenitaliaSolutionsRemoteDataSource @Inject constructor(private val client: HttpClient) {

    companion object {
        private const val Endpoint =
            "http://www.viaggiatreno.it/viaggiatrenonew/resteasy/viaggiatreno/soluzioniViaggioNew"
    }

    suspend fun getOneWaySolutions(
        departureStationId: Int,
        arrivalStationId: Int,
        departureDateTime: LocalDateTime
    ): List<OneWaySolution> {
        try {
            return client
                .get<OneWaySolutionsApiModel>("$Endpoint/$departureStationId/$arrivalStationId/${departureDateTime.coerce()}:00")
                .solutions
                .map { apiSolution ->
                    OneWaySolution(
                        id = "",
                        durationInMinutes = apiSolution.durationInMinutes,
                        trains = apiSolution.vehicles.map { apiVehicle ->
                            Train(
                                name = "${prettifyCategoryName(apiVehicle.category)} ${apiVehicle.identifier}",
                                departureStation = apiVehicle.departureStation,
                                arrivalStation = apiVehicle.arrivalStation,
                                departureDateTime = apiVehicle.departAt,
                                arrivalDateTime = apiVehicle.arriveAt
                            )
                        }
                    )
                }
        } catch (e: Exception) {
            Log.d("Get solutions", e.toString())
            return emptyList()
        }
    }

    private fun LocalDateTime.coerce(): LocalDateTime {
        if (hour !in 6..22) {
            return LocalDateTime(
                year = year,
                monthNumber = monthNumber,
                dayOfMonth = dayOfMonth,
                hour = hour.coerceIn(6, 22),
                minute = minute
            )
        }
        return this
    }

    private fun prettifyCategoryName(category: String): String {
        return when (category) {
            "SFM" -> "TI MET"
            "Autobus" -> "TI BUS"
            "Regionale" -> "TI REG"
            "" -> "Tratto A Piedi -"
            else -> category
        }
    }

    @Serializable
    private data class OneWaySolutionsApiModel(
        @SerialName("soluzioni")
        val solutions: List<Solution>
    ) {

        @Serializable
        data class Solution(
            @SerialName("durata")
            @Serializable(with = DurationInMinutesSerializer::class)
            val durationInMinutes: Int,
            @SerialName("vehicles")
            val vehicles: List<Vehicle>
        )

        @Serializable
        data class Vehicle(
            @SerialName("origine")
            val departureStation: String,
            @SerialName("destinazione")
            val arrivalStation: String,
            @SerialName("orarioPartenza")
            val departAt: LocalDateTime,
            @SerialName("orarioArrivo")
            val arriveAt: LocalDateTime,
            @SerialName("numeroTreno")
            val identifier: String,
            @SerialName("categoriaDescrizione")
            val category: String
        )

        private object DurationInMinutesSerializer : KSerializer<Int> {
            override val descriptor: SerialDescriptor =
                PrimitiveSerialDescriptor("DurationInMinutes", PrimitiveKind.STRING)

            override fun deserialize(decoder: Decoder): Int {
                val format = decoder.decodeString()
                val hour = format.substring(0, 2).toInt()
                val minutes = format.substring(3, 5).toInt()
                return minutes + (hour * 60)
            }

            override fun serialize(encoder: Encoder, value: Int) {
                throw IllegalStateException("Can't serialize")
            }
        }
    }
}