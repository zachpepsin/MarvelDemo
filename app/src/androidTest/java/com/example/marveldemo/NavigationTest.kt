package com.example.marveldemo

import android.content.Context
import androidx.activity.compose.setContent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.platform.app.InstrumentationRegistry
import com.example.marveldemo.ui.AppNavHost
import com.example.marveldemo.ui.Start
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class NavigationTest {
    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private lateinit var navController: TestNavHostController

    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun setupAppNavHost() {
        hiltRule.inject()

        composeTestRule.activity.setContent {
            navController = TestNavHostController(LocalContext.current)
            navController.navigatorProvider.addNavigator(ComposeNavigator())
            AppNavHost(navController = navController, snackbarData = { _, _, _ -> })
        }
    }

    @Test
    fun appNavHost_verifyStartDestination() {
        composeTestRule
            .onAllNodesWithTag("nav_start")
            .onFirst()
            .assertIsDisplayed()

        val route = navController.currentBackStackEntry?.destination?.route
        assertEquals(route, Start::class.java.name)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun appNavHost_clickComic_navigateToComicDetails() {
        composeTestRule.waitUntilAtLeastOneExists(
            matcher = hasContentDescription(context.getString(R.string.content_description_comic_image)),
            timeoutMillis = 5000
        )

        composeTestRule.onAllNodesWithContentDescription(context.getString(R.string.content_description_comic_image))
            .onFirst()
            .performScrollTo()
            .performClick()

        val route = navController.currentBackStackEntry?.destination?.route
        assertEquals(route, "com.example.marveldemo.ui.ComicDetails/{id}")

        composeTestRule.onAllNodesWithTag("nav_comic_details")
            .onFirst()
            .assertExists()
    }
}