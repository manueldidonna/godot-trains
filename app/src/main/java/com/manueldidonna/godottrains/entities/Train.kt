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
package com.manueldidonna.godottrains.entities

import androidx.compose.runtime.Immutable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
@Immutable
data class Train(
    @SerialName("trainidentifier")
    @Serializable(with = TrainNameSerializer::class)
    val name: String
)

private object TrainNameSerializer : KSerializer<String> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("trainidentifier", PrimitiveKind.STRING)

    @OptIn(ExperimentalStdlibApi::class)
    override fun deserialize(decoder: Decoder): String {
        return decoder.decodeString()
            .uppercase()
            .replace("REGIONALE", "REG", ignoreCase = false)
            .replace("METROPOLITANO", "MET", ignoreCase = false)
    }

    override fun serialize(encoder: Encoder, value: String) {
        TODO("Not yet implemented")
    }
}
