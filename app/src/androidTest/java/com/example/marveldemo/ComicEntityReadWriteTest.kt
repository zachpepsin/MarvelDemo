package com.example.marveldemo

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.marveldemo.data.database.AppDatabase
import com.example.marveldemo.data.database.Comic
import com.example.marveldemo.data.database.ComicDao
import com.example.marveldemo.util.TestUtil
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class ComicEntityReadWriteTest {
    private lateinit var comicDao: ComicDao
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java
        ).build()
        comicDao = db.comicDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun writeComicAndReadInList() = runTest {
        val comic: Comic = TestUtil.createComics(1).first()

        comicDao.insertAll(listOf(comic))
        val byId = comicDao.getComicById(0)
        assertEquals(byId, comic)
    }
}