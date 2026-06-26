package com.retro99.server.storyteller.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class BooleanOrIntSerializerTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `deserializes boolean true as 1`() {
        val result = json.decodeFromString<FeaturedHolder>("""{"featured": true}""")
        assertEquals(1, result.featured)
    }

    @Test
    fun `deserializes boolean false as 0`() {
        val result = json.decodeFromString<FeaturedHolder>("""{"featured": false}""")
        assertEquals(0, result.featured)
    }

    @Test
    fun `deserializes integer 1 as 1`() {
        val result = json.decodeFromString<FeaturedHolder>("""{"featured": 1}""")
        assertEquals(1, result.featured)
    }

    @Test
    fun `deserializes integer 0 as 0`() {
        val result = json.decodeFromString<FeaturedHolder>("""{"featured": 0}""")
        assertEquals(0, result.featured)
    }

    @Test
    fun `deserializes null as null`() {
        val result = json.decodeFromString<FeaturedHolder>("""{"featured": null}""")
        assertNull(result.featured)
    }

    @Test
    fun `deserializes missing field as null`() {
        val result = json.decodeFromString<FeaturedHolder>("""{}""")
        assertNull(result.featured)
    }

    @Test
    fun `serializes 1 as integer`() {
        val result = json.encodeToString(FeaturedHolder.serializer(), FeaturedHolder(1))
        assertEquals("""{"featured":1}""", result)
    }

    @Test
    fun `serializes null as omitted`() {
        val result = json.encodeToString(FeaturedHolder.serializer(), FeaturedHolder(null))
        assertEquals("""{}""", result)
    }

    @Test
    fun `deserializes book with boolean featured in series`() {
        val jsonStr = """
            {"uuid":"123","title":"Test","series":[
                {"id":1,"name":"S1","position":1.0,"featured":true}
            ]}
        """.trimIndent()
        val book = json.decodeFromString<StorytellerBookApiModel>(jsonStr)
        assertEquals(1, book.series.first().featured)
    }

    @Test
    fun `deserializes book with integer featured in series`() {
        val jsonStr = """
            {"uuid":"123","title":"Test","series":[
                {"id":1,"name":"S1","position":1.0,"featured":1}
            ]}
        """.trimIndent()
        val book = json.decodeFromString<StorytellerBookApiModel>(jsonStr)
        assertEquals(1, book.series.first().featured)
    }

    @Test
    fun `deserializes book list with mixed featured types across series`() {
        val jsonStr = """
            [
                {"uuid":"1","title":"A","series":[{"id":1,"name":"S1","featured":true}]},
                {"uuid":"2","title":"B","series":[{"id":2,"name":"S2","featured":1}]},
                {"uuid":"3","title":"C","series":[{"id":3,"name":"S3","featured":false}]},
                {"uuid":"4","title":"D","series":[{"id":4,"name":"S4","featured":0}]}
            ]
        """.trimIndent()
        val books = json.decodeFromString<List<StorytellerBookApiModel>>(jsonStr)
        assertEquals(1, books[0].series.first().featured)
        assertEquals(1, books[1].series.first().featured)
        assertEquals(0, books[2].series.first().featured)
        assertEquals(0, books[3].series.first().featured)
    }

    @Serializable
    private data class FeaturedHolder(
        @kotlinx.serialization.SerialName("featured")
        @kotlinx.serialization.Serializable(with = BooleanOrIntSerializer::class)
        val featured: Int? = null,
    )
}
