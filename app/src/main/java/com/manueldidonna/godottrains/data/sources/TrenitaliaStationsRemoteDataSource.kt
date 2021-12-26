package com.manueldidonna.godottrains.data.sources

import android.util.Log
import com.manueldidonna.godottrains.data.models.Station
import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject


class TrenitaliaStationsRemoteDataSource @Inject constructor(private val client: HttpClient) {
    suspend fun searchStations(partialName: String): List<Station> {
        if (partialName.isBlank()) return emptyList()
        return try {
            client
                .get<List<StationApiModel>>("http://www.viaggiatreno.it/viaggiatrenonew/resteasy/viaggiatreno/cercaStazione/$partialName")
                .map { response ->
                    Station(
                        id = response.id.substringAfter("S").toInt(),
                        name = response.longName,
                        shortName = response.longName
                    )
                }
        } catch (e: Exception) {
            Log.e("Get stations", e.toString())
            return emptyList()
        }
    }

    @Serializable
    private data class StationApiModel(
        @SerialName("nomeLungo")
        val longName: String,
        @SerialName("nomeBreve")
        val shortName: String,
        @SerialName("id")
        val id: String
    )
}
