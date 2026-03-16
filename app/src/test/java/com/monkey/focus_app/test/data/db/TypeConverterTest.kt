package com.monkey.focus_app.test.data.db

import com.monkey.focus_app.data.db.Converters
import org.junit.Assert
import org.junit.Assert.assertThrows
import org.junit.Test

class TypeConverterTest {

    private val typeConverters = Converters()

    @Test
    fun testFromJsonToListOfString() {
        val json = """["one","two","three"]"""
        val expected = listOf("one", "two", "three")
        Assert.assertEquals(expected, typeConverters.fromJsonToListOfString(json))
    }

    @Test
    fun testFromListOfStringToJson() {
        val list = listOf("one", "two", "three")
        val expected = """["one","two","three"]"""
        Assert.assertEquals(expected, typeConverters.fromListOfStringToJson(list))
    }

    @Test
    fun testFromJsonToListOfStringWithNullThrowsOrReturnsNullSafety() {

        val exception: NullPointerException? =
            assertThrows(NullPointerException::class.java, {
                typeConverters.fromJsonToListOfString(null)
            })
        Assert.assertEquals("fromJson(...) must not be null", exception?.message)
    }

    @Test
    fun fromJsonToListOfStringWithEmptyArrayReturnsEmptyList() {
        val json = "[]"
        val result = typeConverters.fromJsonToListOfString(json)
        Assert.assertEquals(emptyList<String>(), result)
    }

    @Test
    fun fromListOfStringToJson_withNullElement_returnsJsonWithNull() {
        val list: List<String?> = listOf("x", null, "y")
        val json = typeConverters.fromListOfStringToJson(list)
        val expected = """["x",null,"y"]"""
        Assert.assertEquals(expected, json)
    }

}