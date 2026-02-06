package com.retro99.reader.ui.media.smil

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import org.koin.core.annotation.Single

/**
 * Represents a single SMIL <par> entry with raw references and clock values.
 * The references are kept as raw strings so platforms can resolve them.
 */
@Serializable
data class SmilClip(
    val textSrc: String,
    val audioSrc: String,
    val clipBegin: Double,
    val clipEnd: Double,
)

/**
 * Parses SMIL XML and extracts the raw clip list.
 * Uses Kotlin serialization with xmlutil for multiplatform parsing.
 *
 * This class is managed as a singleton by Koin to reuse the XML parser instance
 * across multiple parsing operations.
 *
 * @param xml The XML parser instance for deserializing SMIL content
 * @param clockParser Parser for SMIL clock values
 */
@Single
class SmilParser(
    private val xml: XML,
    private val clockParser: SmilClockParser,
) {

    fun parseClips(content: String): List<SmilClip> {
        if (content.isBlank()) return emptyList()

        val document = runCatching {
            xml.decodeFromString(SmilDocument.serializer(), content)
        }.getOrNull() ?: return emptyList()

        val pars = document.body?.collectPars().orEmpty()
        return pars.mapNotNull { par ->
            val textSrc = par.text?.src?.trim().orEmpty()
            val audioSrc = par.audio?.src?.trim().orEmpty()
            if (textSrc.isEmpty() || audioSrc.isEmpty()) {
                return@mapNotNull null
            }

            val clipBegin = clockParser.parse(par.audio?.clipBegin) ?: 0.0
            val clipEnd = clockParser.parse(par.audio?.clipEnd) ?: 0.0

            SmilClip(
                textSrc = textSrc,
                audioSrc = audioSrc,
                clipBegin = clipBegin,
                clipEnd = clipEnd,
            )
        }
    }
}

@Serializable
@XmlSerialName("smil", "", "")
private data class SmilDocument(
    val body: SmilBody? = null,
)

@Serializable
@XmlSerialName("body", "", "")
private data class SmilBody(
    val seq: List<SmilSeq> = emptyList(),
    val par: List<SmilPar> = emptyList(),
) {
    fun collectPars(): List<SmilPar> {
        val fromSeq = seq.flatMap { it.collectPars() }
        return par + fromSeq
    }
}

@Serializable
@XmlSerialName("seq", "", "")
private data class SmilSeq(
    val seq: List<SmilSeq> = emptyList(),
    val par: List<SmilPar> = emptyList(),
) {
    fun collectPars(): List<SmilPar> {
        val fromSeq = seq.flatMap { it.collectPars() }
        return par + fromSeq
    }
}

@Serializable
@XmlSerialName("par", "", "")
private data class SmilPar(
    val text: SmilText? = null,
    val audio: SmilAudio? = null,
)

@Serializable
@XmlSerialName("text", "", "")
private data class SmilText(
    val src: String? = null,
)

@Serializable
@XmlSerialName("audio", "", "")
private data class SmilAudio(
    val src: String? = null,
    val clipBegin: String? = null,
    val clipEnd: String? = null,
)