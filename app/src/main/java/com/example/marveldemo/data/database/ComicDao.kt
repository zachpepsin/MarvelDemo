package com.example.marveldemo.data.database

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ComicDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(comics: List<Comic>)

    @Query("SELECT * FROM comics ORDER BY incrementedId")
    fun pagingSource(): PagingSource<Int, Comic>

    @Query("SELECT * FROM comics WHERE id LIKE :id")
    suspend fun getComicById(id: Int): Comic?

    @Query("DELETE FROM comics")
    suspend fun clearAll()
}