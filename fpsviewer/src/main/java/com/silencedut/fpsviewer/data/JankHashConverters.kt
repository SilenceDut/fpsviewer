package com.silencedut.fpsviewer.data


import android.arch.persistence.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


/**
 * @author SilenceDut
 * @date 2019-05-08
 */
class JankHashConverters {

    private val gson = Gson()

    @TypeConverter
    fun fromString(value: String): List<Pair<String,Int>> {
        val listType = object : TypeToken<List<Pair<String,Int>>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromListPair(list: List<Pair<String,Int>>): String {
        return gson.toJson(list)
    }

    @TypeConverter
    fun fromListString(sectionStr: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(sectionStr, listType)
    }

    @TypeConverter
    fun fromListSection(list: List<String>): String {
        return gson.toJson(list)
    }

}