package com.manueldidonna.godottrains.entities

import androidx.compose.runtime.Immutable
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Immutable
@Serializable
data class OneWaySolution(
    @SerialName("trainlist")
    val trains: List<Train>,
    @SerialName("departuretime")
    @Serializable(with = DepartureDateTimeSerializer::class)
    val departureDateTime: LocalDateTime,
    @SerialName("minprice")
    @Serializable(with = PrinceInEuroSerializer::class)
    val priceInEuro: Double,
    @SerialName("duration")
    @Serializable(with = DurationInMinutesSerializer::class)
    val durationInMinutes: Int,
)

private object DurationInMinutesSerializer : KSerializer<Int> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("duration", PrimitiveKind.STRING)

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

private object PrinceInEuroSerializer : KSerializer<Double> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("minprice", PrimitiveKind.FLOAT)

    override fun deserialize(decoder: Decoder): Double {
        return decoder.decodeFloat().toDouble()
    }

    override fun serialize(encoder: Encoder, value: Double) {
        throw IllegalStateException("Can't serialize")
    }
}

private object DepartureDateTimeSerializer : KSerializer<LocalDateTime> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("departuretime", PrimitiveKind.LONG)

    override fun deserialize(decoder: Decoder): LocalDateTime {
        return Instant
            .fromEpochMilliseconds(decoder.decodeLong())
            .toLocalDateTime(TimeZone.currentSystemDefault())
    }

    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        throw IllegalStateException("Can't serialize")
    }
}