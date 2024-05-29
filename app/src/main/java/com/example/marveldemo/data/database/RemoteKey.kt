package com.example.marveldemo.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "remote_keys")
data class RemoteKey(
    @PrimaryKey val comicId: Int,
    val prevKey: Int?,
    val nextKey: Int?,
    val creationTimestamp: Long = System.currentTimeMillis()
)