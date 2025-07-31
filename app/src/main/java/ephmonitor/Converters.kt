package com.example.ephmonitor

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {

    @TypeConverter
    fun fromFloatList(value: List<Float>?): String? {
        val gson = Gson()
        return gson.toJson(value)  // 将 List<Float> 转换为 JSON 字符串
    }

    @TypeConverter
    fun toFloatList(value: String?): List<Float>? {
        val gson = Gson()
        val listType = object : TypeToken<List<Float>>() {}.type
        return gson.fromJson(value, listType)  // 将 JSON 字符串转换回 List<Float>
    }
}
