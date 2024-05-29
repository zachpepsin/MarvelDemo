package com.example.marveldemo.ui.screens

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.marveldemo.data.ComicsRepository
import com.example.marveldemo.data.ComicsService.ComicSortOrder
import com.example.marveldemo.data.database.Comic
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

/**
 * UI state for the comics list screen
 */
sealed interface ComicDetailsUiState {
    data class Success(val comic: Comic) : ComicDetailsUiState
    data object Error : ComicDetailsUiState
    data object Loading : ComicDetailsUiState
}

@HiltViewModel
class ComicsViewModel @Inject constructor(
    private val comicsRepository: ComicsRepository
) : ViewModel() {

    private val _comicsPagingData: MutableStateFlow<PagingData<Comic>> =
        MutableStateFlow(PagingData.empty())
    var comicsPagingData = _comicsPagingData.asStateFlow()
        private set

    private val _comicDetailsUiState =
        MutableStateFlow<ComicDetailsUiState>(ComicDetailsUiState.Loading)
    val comicDetailsUiState: StateFlow<ComicDetailsUiState> = _comicDetailsUiState.asStateFlow()

    private val _query = mutableStateOf("")

    private val _comicsSortOrder = mutableStateOf(ComicSortOrder.TITLE_ASC)  // Default sort order
    var comicsSortOrder = _comicsSortOrder
        private set

    init {
        getComics()
    }

    private fun getComics() {
        viewModelScope.launch {
            var queryTemp: String? = _query.value
            if (queryTemp?.length!! < QUERY_LENGTH_THRESHOLD) {
                queryTemp = null
            }
            comicsRepository.getComicsStream(
                query = queryTemp,
                comicSortOrder = _comicsSortOrder.value
            )
                .cachedIn(viewModelScope)
                .collectLatest { pagingData ->
                    _comicsPagingData.value = pagingData
                }
        }
    }

    fun getComic(id: Int) {
        viewModelScope.launch {
            _comicDetailsUiState.value = ComicDetailsUiState.Loading
            _comicDetailsUiState.value =
                try {
                    val comic = comicsRepository.getComic(id)
                    if (comic == null) {
                        ComicDetailsUiState.Error
                    } else {
                        ComicDetailsUiState.Success(comic)
                    }
                } catch (e: IOException) {
                    ComicDetailsUiState.Error
                } catch (e: HttpException) {
                    ComicDetailsUiState.Error
                }
        }
    }

    fun updateQuery(query: String) {
        val prevQuery = _query.value
        _query.value = query
        if ((prevQuery.length in 0..<QUERY_LENGTH_THRESHOLD)
            && (query.length in 0..<QUERY_LENGTH_THRESHOLD)
        ) {
            return
        }
        getComics()
    }

    fun updateSortOrder(comicSortOrder: ComicSortOrder) {
        _comicsSortOrder.value = comicSortOrder
        getComics()
    }

    companion object {
        const val QUERY_LENGTH_THRESHOLD =
            3 // Minimum number of characters before a search is performed
    }
}