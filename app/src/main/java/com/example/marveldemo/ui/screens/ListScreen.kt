package com.example.marveldemo.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Downloading
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.marveldemo.R
import com.example.marveldemo.data.database.Comic
import com.example.marveldemo.data.database.ComicImage
import com.example.marveldemo.ui.theme.MarvelDemoTheme
import kotlinx.coroutines.flow.flowOf

@Composable
fun ListScreen(
    comicLazyPagingItems: LazyPagingItems<Comic>,
    navigateToComicDetails: (comicId: Int) -> Unit,
    snackbarData: (messageText: String, actionText: String, action: () -> Unit) -> Unit,
    modifier: Modifier = Modifier,
) {
    val refreshState = comicLazyPagingItems.loadState.mediator?.refresh

    // Per this issue tracker, this is necessary
    // https://issuetracker.google.com/issues/224855902
    var initialLoadComplete by rememberSaveable { mutableStateOf(false) }

    if (comicLazyPagingItems.itemCount > 0) {
        initialLoadComplete = true
        // We have items
        PullToRefreshComics(
            comicLazyPagingItems = comicLazyPagingItems,
            isRefreshing = refreshState is LoadState.Loading,
            onRefresh = { comicLazyPagingItems.refresh() },
            onComicClicked = { comicId -> navigateToComicDetails(comicId) },
            refreshErrorSnackbarData = { messageText, actionText, action ->
                snackbarData(
                    messageText,
                    actionText,
                    action
                )
            },
            modifier = modifier
        )
    } else {
        // We do not have items
        when (refreshState) {
            is LoadState.Error, null -> {
                ErrorScreen(modifier = modifier) {
                    comicLazyPagingItems.retry()
                }
            }

            LoadState.Loading -> {
                LoadingItem(modifier = modifier)
            }

            is LoadState.NotLoading -> {
                // There were no items returned
                if (initialLoadComplete) {
                    NoResultsScreen(
                        text = stringResource(id = R.string.no_results_found),
                        modifier = modifier
                    )
                }
            }

        }
    }
}

@Composable
fun PullToRefreshComics(
    comicLazyPagingItems: LazyPagingItems<Comic>,
    isRefreshing: Boolean,
    onComicClicked: (comicId: Int) -> Unit,
    onRefresh: () -> Unit,
    refreshErrorSnackbarData: (messageText: String, actionText: String, action: () -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    val loadState = comicLazyPagingItems.loadState.mediator

    // On scroll, close the keyboard
    val keyboardController = LocalSoftwareKeyboardController.current
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                keyboardController?.hide()
                return Offset.Zero
            }
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            contentPadding = PaddingValues(8.dp),
            modifier = Modifier.nestedScroll(nestedScrollConnection)
        ) {
            // We do not have any cases for pre-pending data in this UI, so this is commented
            // out to prevent showing any of the prepending UI which would look odd
            when (comicLazyPagingItems.loadState.prepend) {
                is LoadState.Error -> {
                    item(
                        span = { GridItemSpan(maxLineSpan) }
                    ) {
                        ErrorItem(
                            modifier = modifier
                        ) {
                            comicLazyPagingItems.retry()
                        }
                    }
                }

                is LoadState.Loading -> {
                    if (loadState?.append !is LoadState.Loading) {
                        // Check to make sure append is not also loading to avoid double loading UIs
                        item(
                            span = { GridItemSpan(maxLineSpan) }
                        ) {
                            LoadingItem(modifier = modifier)
                        }
                    }
                }

                is LoadState.NotLoading -> { /* No-op */
                }
            }

            items(
                count = comicLazyPagingItems.itemCount,
                key = comicLazyPagingItems.itemKey { it.incrementedId },
                contentType = comicLazyPagingItems.itemContentType { it.javaClass.toString() },
            ) { index ->
                val comic = comicLazyPagingItems[index]
                ComicCard(
                    comic,
                    onComicClicked = onComicClicked,
                    modifier = modifier
                        .padding(4.dp)
                        .fillMaxWidth()
                        .aspectRatio(0.8f),
                )
            }

            // Add an item to the end of the list (if necessary) for loading/error/end states
            when (val appendState = comicLazyPagingItems.loadState.append) {
                is LoadState.Loading -> {
                    // We are loading more items at the end of the list
                    // Only show this when we have items - to avoid displaying it when the
                    // initial data is being loaded in
                    if (comicLazyPagingItems.itemCount > 0) {
                        item(
                            span = { GridItemSpan(maxLineSpan) }
                        ) {
                            LoadingItem(modifier = modifier)
                        }
                    }
                }

                is LoadState.NotLoading -> {
                    if (appendState.endOfPaginationReached) {
                        item(
                            span = { GridItemSpan(maxLineSpan) }
                        ) {
                            NoResultsScreen(
                                text = stringResource(id = R.string.end_of_results),
                                modifier = modifier
                            )
                        }
                    }
                }

                is LoadState.Error -> {
                    // There was an error loading more items at the end of the list
                    item(
                        span = { GridItemSpan(maxLineSpan) }
                    ) {
                        ErrorItem(
                            modifier = modifier
                        ) {
                            comicLazyPagingItems.retry()
                        }
                    }
                }
            }
        }

        if (loadState?.refresh is LoadState.Error) {
            // We ran into an error while refreshing
            // Keep displaying the data we have stored locally, but provide an error message
            refreshErrorSnackbarData(
                stringResource(id = R.string.error_refreshing),
                stringResource(id = R.string.retry)
            ) { comicLazyPagingItems.retry() }
        }
    }
}

@Composable
fun LoadingItem(modifier: Modifier = Modifier) {
    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        CircularProgressIndicator(
            modifier = modifier.size(50.dp),
            color = MaterialTheme.colorScheme.secondary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
fun ErrorScreen(
    modifier: Modifier = Modifier,
    retryAction: () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_error),
            contentDescription = "",
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
        )
        Text(
            text = stringResource(R.string.error),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(16.dp)
        )
        ErrorRetryButton(retryAction = retryAction)
    }
}

@Composable
fun ErrorRetryButton(
    retryAction: () -> Unit
) {
    Button(onClick = retryAction) {
        Text(stringResource(R.string.retry))
    }
}

@Composable
fun ErrorItem(
    modifier: Modifier = Modifier,
    retryAction: () -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.error),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(16.dp)
        )
        ErrorRetryButton(retryAction = retryAction)
    }
}

@Composable
fun NoResultsScreen(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge
        )
    }
}

@Composable
fun ComicCard(
    comic: Comic?,
    onComicClicked: (comicId: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
            .clickable(onClick = {
                comic?.id?.let { id ->
                    onComicClicked(id)
                }
            })
    ) {
        Column(
            modifier = Modifier
                .width(255.dp)
        ) {
            val imageUrl = "${comic?.thumbnail?.path}.${comic?.thumbnail?.extension}"

            AsyncImage(
                model =
                ImageRequest.Builder(context = LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true).build(),
                error = rememberVectorPainter(Icons.Default.BrokenImage),
                placeholder = rememberVectorPainter(Icons.Default.Downloading),
                contentDescription = stringResource(id = R.string.content_description_comic_image),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.7f)
            )
            Text(
                text = comic?.title ?: stringResource(R.string.untitled),
                color = MaterialTheme.colorScheme.onSurfaceVariant, // Elevated cards have a surfaceVariant background
                style = MaterialTheme.typography.labelLarge,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(0.3f)
                    .wrapContentHeight(align = Alignment.CenterVertically)
                    .padding(8.dp)
            )
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun PullToRefreshComicsPreview() {
    MarvelDemoTheme {
        val fakeData = List(10) {
            previewComic(it)
        }

        PullToRefreshComics(
            comicLazyPagingItems = flowOf(PagingData.from(fakeData)).collectAsLazyPagingItems(),
            isRefreshing = false,
            onComicClicked = {},
            onRefresh = {},
            refreshErrorSnackbarData = { _, _, _ -> }
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun LoadingScreenPreview() {
    MarvelDemoTheme {
        LoadingItem(modifier = Modifier.fillMaxSize())
    }
}

@Preview(showSystemUi = true)
@Composable
private fun ErrorScreenPreview() {
    MarvelDemoTheme {
        ErrorScreen(modifier = Modifier.fillMaxSize()) {}
    }
}

@Preview(showBackground = true, heightDp = 200, widthDp = 160)
@Composable
private fun ComicCardPreview() {
    MarvelDemoTheme {
        ComicCard(
            comic = previewComic(),
            onComicClicked = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadingItemPreview() {
    LoadingItem()
}


@Preview(showBackground = true)
@Composable
private fun ErrorItemPreview() {
    ErrorItem {}
}

@Preview(showSystemUi = true)
@Composable
private fun NoResultsScreenPreview() {
    NoResultsScreen(
        text = stringResource(id = R.string.no_results_found),
        modifier = Modifier.fillMaxSize()
    )
}

private fun previewComic(key: Int = 0) = Comic(
    id = key,
    incrementedId = key,
    title = "Title",
    thumbnail = ComicImage("", ""),
    description = "Description",
    textObjects = emptyList(),
    creatorList = null,
    characterList = null,
)