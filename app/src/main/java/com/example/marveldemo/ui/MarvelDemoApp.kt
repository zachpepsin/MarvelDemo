package com.example.marveldemo.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.marveldemo.R
import com.example.marveldemo.data.ComicsService
import com.example.marveldemo.data.ComicsService.ComicSortOrder
import com.example.marveldemo.ui.screens.ComicDetailsScreen
import com.example.marveldemo.ui.screens.ComicsViewModel
import com.example.marveldemo.ui.screens.ListScreen
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

// Start destination that doesn't take any arguments
@Serializable
object Start

// The comic details screen takes an id
@Serializable
data class ComicDetails(val id: Int)

@Composable
fun MarvelDemoApp() {
    val navController: NavHostController = rememberNavController()

    val snackbarHostState = remember { SnackbarHostState() }
    val hostCoroutineScope = rememberCoroutineScope()

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())

    // Subscribe to navBackStackEntry, required to get current route
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    // State of topBar, set state to false, if current page route is "Start"
    val homeState = rememberSaveable { (mutableStateOf(true)) }
    homeState.value =
        navBackStackEntry?.destination?.route.equals("com.example.marveldemo.ui.Start")

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MarvelDemoAppBar(
                title = when (getCurrentNavDestination(navController.currentDestination?.route)) {
                    Screen.START -> stringResource(id = R.string.comics)
                    Screen.COMIC_DETAILS -> stringResource(id = R.string.comic_details)
                    null -> ""
                },
                homeState = homeState,
                canNavigateBack = navController.previousBackStackEntryAsState().value != null,
                navigateUp = { navController.navigateUp() },
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            snackbarData = { messageText, actionText, action ->
                hostCoroutineScope.launch {
                    val result = snackbarHostState
                        .showSnackbar(
                            message = messageText,
                            actionLabel = actionText,
                            duration = SnackbarDuration.Indefinite // Per guidelines, with action
                        )
                    when (result) {
                        SnackbarResult.ActionPerformed -> {
                            /* Handle snackbar action performed */
                            action()
                        }

                        SnackbarResult.Dismissed -> {
                            /* Handle snackbar dismissed */
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    snackbarData: (messageText: String, actionText: String, action: () -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: ComicsViewModel = hiltViewModel()
    val comicLazyPagingItems = viewModel.comicsPagingData.collectAsLazyPagingItems()

    NavHost(
        navController = navController,
        startDestination = Start,
        modifier = modifier
    ) {
        composable<Start> {
            ListScreen(
                comicLazyPagingItems = comicLazyPagingItems,
                navigateToComicDetails = { comicId ->
                    navController.navigate(ComicDetails(comicId))
                },
                snackbarData = { messageText, actionText, action ->
                    snackbarData(
                        messageText,
                        actionText,
                        action
                    )
                },
                modifier = Modifier
                    .testTag("nav_start")
            )
        }
        composable<ComicDetails> { backStackEntry ->
            val comicDetails: ComicDetails = backStackEntry.toRoute()
            ComicDetailsScreen(
                id = comicDetails.id,
                modifier = Modifier
                    .testTag("nav_comic_details")
            )
        }
    }
}

/**
 * Gets the previous navigation back stack entry as a [State]. When the given navController
 * changes the back stack due to a [NavController.navigate] or [NavController.popBackStack] this
 * will trigger a recompose and return the second top entry on the back stack.
 *
 * @return a state of the previous back stack entry
 */
@Composable
fun NavController.previousBackStackEntryAsState(): State<NavBackStackEntry?> {
    val previousNavBackStackEntry = remember { mutableStateOf(previousBackStackEntry) }
    // setup the onDestinationChangedListener responsible for detecting when the
    // previous back stack entry changes
    DisposableEffect(this) {
        val callback = NavController.OnDestinationChangedListener { controller, _, _ ->
            previousNavBackStackEntry.value = controller.previousBackStackEntry
        }
        addOnDestinationChangedListener(callback)
        // remove the navController on dispose (i.e. when the composable is destroyed)
        onDispose {
            removeOnDestinationChangedListener(callback)
        }
    }
    return previousNavBackStackEntry
}

@Composable
fun MarvelDemoAppBar(
    title: String,
    homeState: MutableState<Boolean>,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
    modifier: Modifier = Modifier
) {

    val viewModel: ComicsViewModel = hiltViewModel()
    var dropDownMenuExpanded by remember { mutableStateOf(false) }
    val currentSortOrder by remember {
        viewModel.comicsSortOrder
    }
    var text by rememberSaveable { mutableStateOf("") }

    TopAppBar(
        title = {
            if (homeState.value) {
                ComicsSearchBar(
                    initialText = text,
                    onTextChange = { searchText ->
                        text = searchText
                        viewModel.updateQuery(searchText)
                    }
                )
            } else {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                )
            }
        },
        modifier = modifier,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.action_back)
                    )
                }
            }
        },
        actions = {
            if (homeState.value) {
                IconButton(onClick = { dropDownMenuExpanded = true }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.Sort,
                        contentDescription = stringResource(R.string.change_sort_order)
                    )
                }
            }

            DropdownMenu(
                expanded = dropDownMenuExpanded,
                onDismissRequest = { dropDownMenuExpanded = false },
                modifier = Modifier.padding(8.dp)
            ) {
                Column {
                    Text(
                        text = stringResource(id = R.string.change_sort_order),
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    ComicsService.ComicSortOrder.entries.forEach { sortOrder ->
                        SortDropdownItem(
                            sortOrder = sortOrder,
                            onSortSelected = {
                                viewModel.updateSortOrder(sortOrder)
                                dropDownMenuExpanded = false
                            },
                            isCurrentSelection = currentSortOrder == sortOrder
                        )
                    }
                }
            }

        },
        scrollBehavior = scrollBehavior
    )
}

@Composable
fun SortDropdownItem(
    sortOrder: ComicSortOrder,
    onSortSelected: (ComicSortOrder) -> Unit,
    isCurrentSelection: Boolean
) {
    DropdownMenuItem(
        text = {
            Text(text = stringResource(id = sortOrder.getDescriptionRes()))
        },
        onClick = { onSortSelected(sortOrder) },
        leadingIcon = {
            Icon(
                imageVector = if (isCurrentSelection) {
                    Icons.Default.RadioButtonChecked
                } else {
                    Icons.Default.RadioButtonUnchecked
                },
                contentDescription = null
            )
        }
    )
}

@Composable
fun ComicsSearchBar(
    initialText: String,
    onTextChange: (searchText: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var text = initialText
    val focusManager = LocalFocusManager.current

    SearchBarDefaults.InputField(
        query = text,
        onQueryChange = {
            text = it
            onTextChange(text)
        },
        onSearch = { focusManager.clearFocus() },
        expanded = false,
        onExpandedChange = {},
        placeholder = { Text(stringResource(id = R.string.search_comics)) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (text.isNotEmpty()) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(id = R.string.clear_search),
                    modifier = modifier.clickable {
                        text = ""
                        onTextChange(text)
                        focusManager.clearFocus()
                    })
            }
        }
    )
}

fun getCurrentNavDestination(currentDestination: String?): Screen? =
    when (currentDestination) {
        "com.example.marveldemo.ui.Start" -> Screen.START
        "com.example.marveldemo.ui.ComicDetails/{id}" -> Screen.COMIC_DETAILS
        else -> null
    }

enum class Screen {
    START, COMIC_DETAILS
}

@Preview
@Composable
private fun ComicSearchBarPreview() {
    ComicsSearchBar("", {})
}
