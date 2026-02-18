package com.retro99.server.api

import com.retro99.base.server.ServerType
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Custom serializer for ServerType enum.
 * Serializes using the identifier string for compatibility.
 */
object ServerTypeSerializer : KSerializer<ServerType> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("ServerType", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: ServerType) {
        encoder.encodeString(value.identifier)
    }

    override fun deserialize(decoder: Decoder): ServerType {
        val identifier = decoder.decodeString()
        return ServerType.fromIdentifier(identifier)
            ?: throw IllegalArgumentException("Unknown server type: $identifier")
    }
}

