package com.example.marveldemo.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.marveldemo.MarvelDemoApplication
import com.example.marveldemo.data.ComicsService.ComicSortOrder
import com.example.marveldemo.data.database.AppDatabase
import com.example.marveldemo.data.database.Comic
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ComicsRepository @Inject constructor(
    private val appDatabase: AppDatabase,
    private val comicsService: ComicsService
) {
    fun getComicsStream(
        query: String?,
        comicSortOrder: ComicSortOrder
    ): Flow<PagingData<Comic>> {
        @OptIn(ExperimentalPagingApi::class)
        return Pager(
            // Configure how data is loaded by passing additional properties to
            // PagingConfig, such as prefetchDistance.
            config = PagingConfig(
                pageSize = MarvelDemoApplication.COMIC_ITEMS_PER_PAGE,
                enablePlaceholders = true
            ),
            pagingSourceFactory = {
                appDatabase.comicDao().pagingSource()
            },
            remoteMediator = ComicsRemoteMediator(
                database = appDatabase,
                comicsService = comicsService,
                query = query,
                comicSortOrder = comicSortOrder
            )
        ).flow
    }

    // Attempt to load the requested comic from the cache
    // If it is not found, then attempt to find the comic from the endpoint
    suspend fun getComic(id: Int): Comic? {
        return appDatabase.comicDao().getComicById(id)
            ?: comicsService.getComic(id).comicDataContainer.results.firstOrNull()
    }
}