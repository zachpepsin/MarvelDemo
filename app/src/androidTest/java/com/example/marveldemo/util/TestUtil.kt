package com.example.marveldemo.util

import com.example.marveldemo.data.database.Comic
import com.example.marveldemo.data.database.ComicImage

object TestUtil {

    fun createComics(count: Int): List<Comic> =
        List(count) {
            Comic(
                id = it,
                incrementedId = it + 1, // incrementedId starts at 1
                title = "Title $it",
                thumbnail = ComicImage("", ""),
                description = "Description $it",
                textObjects = emptyList(),
                creatorList = null,
                characterList = null
            )
        }
}