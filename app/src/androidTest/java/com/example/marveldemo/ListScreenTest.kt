package com.example.marveldemo

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.testing.asSnapshot
import com.example.marveldemo.data.ComicsRepository
import com.example.marveldemo.data.ComicsService
import com.example.marveldemo.data.database.Comic
import com.example.marveldemo.ui.screens.ComicsViewModel
import com.example.marveldemo.ui.screens.ListScreen
import com.example.marveldemo.util.TestUtil
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class ListScreenTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var viewModel: ComicsViewModel

    @Inject
    lateinit var comicsService: ComicsService

    @Inject
    lateinit var comicsRepository: ComicsRepository

    @Before
    fun init() {
        hiltRule.inject()
        viewModel = ComicsViewModel(comicsRepository)
    }

    @Test
    fun comicsHaveCorrectIncrementedId() = runTest {
        // Get the Flow of PagingData from the ViewModel under test
        val items: Flow<PagingData<Comic>> = viewModel.comicsPagingData

        val itemsSnapshot: List<Comic> = items.asSnapshot {
            // Scroll down 10 items in the list. This will also suspend
            // till the prefetch requirement is met if there's one.
            // It also suspends until all loading is complete.
            scrollTo(index = 10)
        }

        // With the asSnapshot complete, you can now verify that the snapshot
        // has the expected values
        assertEquals(
            (1..60).toList(),
            itemsSnapshot.map { it.incrementedId }
        )
    }

    @Test
    fun comicsHaveTitle() = runTest {
        // Get the Flow of PagingData from the ViewModel under test
        val items: Flow<PagingData<Comic>> = viewModel.comicsPagingData

        val itemsSnapshot: List<Comic> = items.asSnapshot {
            // Scroll to the 50th item in the list. This will also suspend till
            // the prefetch requirement is met if there's one.
            // It also suspends until all loading is complete.
            scrollTo(index = 50)
        }

        // With the asSnapshot complete, you can now verify that the snapshot
        // has the expected values

        // Having a title is not a requirement from the API, but this is
        // just an example of how we could verify data
        assert(
            itemsSnapshot.map { it.title }.all {
                !it.isNullOrEmpty()
            }
        )
    }

    @Test
    fun comicTitleDisplayed() {
        val flowData: Flow<PagingData<Comic>> =
            MutableStateFlow(PagingData.from(TestUtil.createComics(10)))

        composeTestRule.setContent {
            val items = flowData.collectAsLazyPagingItems()
            ListScreen(
                comicLazyPagingItems = items,
                navigateToComicDetails = {},
                snackbarData = { _, _, _ -> }
            )
        }

        composeTestRule.onNodeWithText("Title 0").assertExists()
    }
}