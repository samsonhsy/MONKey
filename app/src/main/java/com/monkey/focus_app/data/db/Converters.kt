package com.monkey.focus_app.data.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromJsonToListOfString(value: String?): List<String> {
        if (value == null) return emptyList()
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromListOfStringToJson(list: List<String>): String {
        return gson.toJson(list)
    }

    @TypeConverter
    fun fromJsonToListOfInt(value: String?): List<Int> {
        if (value == null) return emptyList()
        val listType = object : TypeToken<List<Int>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromListOfIntToJson(list: List<Int>): String {
        return gson.toJson(list)
    }

}
