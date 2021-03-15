package com.manueldidonna.godottrains.network

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

interface LeFrecceApi {
    suspend fun getStationsByPartialName(partialName: String): List<String>

    companion object {
        val Ktor: LeFrecceApi by lazy { KtorLeFrecceApi() }
    }
}

private class KtorLeFrecceApi : LeFrecceApi {
    private val client = HttpClient(OkHttp)
    private val json = Json { ignoreUnknownKeys = true }
    override suspend fun getStationsByPartialName(partialName: String): List<String> {
        if (partialName.isBlank() || partialName.length < 3) return emptyList()
        return try {
            val response: String = client.get("$ApiPath/geolocations/locations") {
                parameter("name", partialName)
            }
            json
                .parseToJsonElement(response)
                .jsonArray
                .mapNotNull { it.jsonObject["name"]?.jsonPrimitive?.content }
        } catch (e: Exception) {
            emptyList()
        }
    }

    companion object {
        private const val ApiPath = "https://www.lefrecce.it/msite/api"
    }
}
