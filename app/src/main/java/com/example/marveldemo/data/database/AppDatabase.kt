package com.example.marveldemo.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Database(
    entities = [
        Comic::class,
        RemoteKey::class
    ],
    exportSchema = false, // Set to false since we will keep this database in memory only
    version = 1
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun comicDao(): ComicDao
    abstract fun remoteKeyDao(): RemoteKeyDao
}

class Converters {

    @TypeConverter
    fun fromTextObjectList(value: List<TextObject>): String {
        val gson = Gson()
        val type = object : TypeToken<List<TextObject>>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toTextObject(value: String): List<TextObject> {
        val gson = Gson()
        val type = object : TypeToken<List<TextObject>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromCreatorSummary(value: List<CreatorSummary>): String {
        val gson = Gson()
        val type = object : TypeToken<List<CreatorSummary>>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toCreatorSummary(value: String): List<CreatorSummary> {
        val gson = Gson()
        val type = object : TypeToken<List<CreatorSummary>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromCharacterSummary(value: List<CharacterSummary>): String {
        val gson = Gson()
        val type = object : TypeToken<List<CharacterSummary>>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toCharacterSummary(value: String): List<CharacterSummary> {
        val gson = Gson()
        val type = object : TypeToken<List<CharacterSummary>>() {}.type
        return gson.fromJson(value, type)
    }
}