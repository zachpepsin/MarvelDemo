package com.example.marveldemo.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.example.marveldemo.MarvelDemoApplication
import com.example.marveldemo.data.ComicsService.ComicSortOrder
import com.example.marveldemo.data.database.AppDatabase
import com.example.marveldemo.data.database.Comic
import com.example.marveldemo.data.database.RemoteKey
import retrofit2.HttpException
import java.io.IOException
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalPagingApi::class)
class ComicsRemoteMediator(
    private val database: AppDatabase,
    private val comicsService: ComicsService,
    private val query: String?,
    private val comicSortOrder: ComicSortOrder,
) : RemoteMediator<Int, Comic>() {
    private val comicDao = database.comicDao()
    private val remoteKeyDao = database.remoteKeyDao()

    // The cache can be invalidated and cleared after a set amount of time
    // This is unnecessary when using an in-memory-only db, as there will not be any items
    // in the DB when the app is launched, however is still here for demonstration.
    override suspend fun initialize(): InitializeAction {
        val cacheTimeout =
            TimeUnit.MINUTES.convert(CACHE_EXPIRATION_PERIOD_MINUTES, TimeUnit.MILLISECONDS)
        return if (System.currentTimeMillis() - (database.remoteKeyDao().getOldestCreatedTimestamp()
                ?: 0L) < cacheTimeout
        ) {
            // Cached data is up-to-date, so there is no need to re-fetch from network.
            InitializeAction.SKIP_INITIAL_REFRESH
        } else {
            // Need to refresh cached data from network; returning LAUNCH_INITIAL_REFRESH here
            // will also block RemoteMediator's APPEND and PREPEND from running until REFRESH
            // succeeds.
            InitializeAction.LAUNCH_INITIAL_REFRESH
        }
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, Comic>
    ): MediatorResult {
        return try {
            val page = when (loadType) {
                LoadType.REFRESH -> {
                    val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                    remoteKeys?.nextKey?.minus(1) ?: COMIC_API_STARTING_INDEX
                }

                LoadType.PREPEND -> {
                    val remoteKey = getRemoteKeyForFirstItem(state)
                    // If remoteKey is null, that means the refresh result is not in the database yet
                    val prevKey = remoteKey?.prevKey
                        ?: return MediatorResult.Success(endOfPaginationReached = remoteKey != null)
                    prevKey
                }

                LoadType.APPEND -> {
                    val remoteKey = getRemoteKeyForLastItem(state)

                    // You must explicitly check if the page key is null when
                    // appending, since null is only valid for initial load.
                    // If you receive null for APPEND, that means you have
                    // reached the end of pagination and there are no more
                    // items to load
                    if (remoteKey?.nextKey == null) {
                        return MediatorResult.Success(
                            endOfPaginationReached = remoteKey != null
                        )
                    }
                    remoteKey.nextKey
                }
            }

            // Suspending network load via Retrofit. This doesn't need to be
            // wrapped in a withContext(Dispatcher.IO) { ... } block since
            // Retrofit's Coroutine CallAdapter dispatches on a worker
            // thread.
            val response = comicsService.getComics(
                limit = MarvelDemoApplication.COMIC_ITEMS_PER_PAGE,
                offset = getOffset(page),
                orderBy = comicSortOrder.toString(),
                titleStartsWith = query?.ifBlank { null }
            )

            val comics = response.comicDataContainer.results
            val endOfPaginationReached = comics.isEmpty()

            database.withTransaction {
                // clear all tables in the database
                if (loadType == LoadType.REFRESH) {
                    remoteKeyDao.clearAll()
                    comicDao.clearAll()
                }

                // Insert new comics into database, which invalidates the current
                // PagingData, allowing Paging to present the updates in the DB
                val prevKey = if (page == COMIC_API_STARTING_INDEX) null else page - 1
                val nextKey = if (endOfPaginationReached) null else page + 1
                val keys = comics.map {
                    RemoteKey(comicId = it.id, prevKey = prevKey, nextKey = nextKey)
                }
                remoteKeyDao.insertAll(keys)
                comicDao.insertAll(comics)
            }

            MediatorResult.Success(
                endOfPaginationReached = endOfPaginationReached
            )
        } catch (e: IOException) {
            MediatorResult.Error(e)
        } catch (e: HttpException) {
            MediatorResult.Error(e)
        }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, Comic>): RemoteKey? {
        // Get the first page that was retrieved, that contained items.
        // From that first page, get the first item
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()
            ?.let { comic ->
                // Get the remote keys of the first item retrieved
                remoteKeyDao.remoteKeyComicId(comic.id)
            }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, Comic>): RemoteKey? {
        // Get the last page that was retrieved, that contained items.
        // From that last page, get the last item
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()
            ?.let { comic ->
                // Get the remote keys of the last item retrieved
                remoteKeyDao.remoteKeyComicId(comic.id)
            }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(
        state: PagingState<Int, Comic>
    ): RemoteKey? {
        // The paging library is trying to load data after the anchor position
        // Get the item closest to the anchor position
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { comicId ->
                remoteKeyDao.remoteKeyComicId(comicId)
            }
        }
    }

    private fun getOffset(page: Int) = MarvelDemoApplication.COMIC_ITEMS_PER_PAGE * (page - 1)

    companion object {
        private const val COMIC_API_STARTING_INDEX = 1
        private const val CACHE_EXPIRATION_PERIOD_MINUTES = 30L
    }

}