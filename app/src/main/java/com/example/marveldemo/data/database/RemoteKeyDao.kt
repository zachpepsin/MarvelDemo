package com.example.marveldemo.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RemoteKeyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(remoteKey: List<RemoteKey>)

    @Query("SELECT * FROM remote_keys WHERE comicId = :comicId")
    suspend fun remoteKeyComicId(comicId: Int): RemoteKey?

    @Query("SELECT creationTimestamp FROM remote_keys ORDER BY creationTimestamp ASC LIMIT 1")
    suspend fun getOldestCreatedTimestamp(): Long?

    @Query("DELETE FROM remote_keys")
    suspend fun clearAll()
}