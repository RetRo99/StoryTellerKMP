package com.retro99.reader.ui.media.smil

import co.touchlab.kermit.Logger
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
    private val logger = Logger.withTag("čič")

    fun parseClips(content: String): List<SmilClip> {
        logger.d { "parseClips called, content length: ${content.length}" }

        if (content.isBlank()) {
            logger.w { "Content is blank, returning empty list" }
            return emptyList()
        }

        val document = runCatching {
            xml.decodeFromString(SmilDocument.serializer(), content)
        }.onFailure { e ->
            logger.e(e) { "Failed to decode SMIL XML" }
        }.getOrNull()

        if (document == null) {
            logger.e { "Document is null after parsing, returning empty list" }
            return emptyList()
        }

        logger.d { "Document parsed, body: ${document.body != null}" }

        val pars = document.body?.collectPars().orEmpty()
        logger.d { "Found ${pars.size} <par> elements" }

        val clips = pars.mapNotNull { par ->
            val textSrc = par.text?.src?.trim().orEmpty()
            val audioSrc = par.audio?.src?.trim().orEmpty()
            if (textSrc.isEmpty() || audioSrc.isEmpty()) {
                logger.v { "Skipping par: textSrc='$textSrc', audioSrc='$audioSrc'" }
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

        logger.d { "Parsed ${clips.size} clips from ${pars.size} pars" }
        return clips
    }
}

private const val SMIL_NAMESPACE = "http://www.w3.org/ns/SMIL"

@Serializable
@XmlSerialName("smil", SMIL_NAMESPACE, "")
private data class SmilDocument(
    val body: SmilBody? = null,
)

@Serializable
@XmlSerialName("body", SMIL_NAMESPACE, "")
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
@XmlSerialName("seq", SMIL_NAMESPACE, "")
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
@XmlSerialName("par", SMIL_NAMESPACE, "")
private data class SmilPar(
    val text: SmilText? = null,
    val audio: SmilAudio? = null,
)

@Serializable
@XmlSerialName("text", SMIL_NAMESPACE, "")
private data class SmilText(
    val src: String? = null,
)

@Serializable
@XmlSerialName("audio", SMIL_NAMESPACE, "")
private data class SmilAudio(
    val src: String? = null,
    val clipBegin: String? = null,
    val clipEnd: String? = null,
)