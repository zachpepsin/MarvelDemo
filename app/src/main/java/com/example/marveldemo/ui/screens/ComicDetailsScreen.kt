package com.example.marveldemo.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.marveldemo.R
import com.example.marveldemo.data.database.Comic
import java.util.Locale
import kotlin.math.min

val SURFACE_ELEVATION = 10.dp

@Composable
fun ComicDetailsScreen(
    id: Int,
    modifier: Modifier = Modifier
) {
    val viewmodel: ComicsViewModel = hiltViewModel()
    val uiState by viewmodel.comicDetailsUiState.collectAsStateWithLifecycle()
    viewmodel.getComic(id)

    Surface(
        tonalElevation = SURFACE_ELEVATION,
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (val state = uiState) {
            is ComicDetailsUiState.Loading -> LoadingItem(modifier = modifier)
            is ComicDetailsUiState.Success -> ComicDetailsContentParallax(
                comic = state.comic,
                modifier
            )

            is ComicDetailsUiState.Error -> ErrorScreen(modifier = modifier.fillMaxSize()) {
                viewmodel.getComic(id)
            }
        }
    }
}

@Composable
fun ComicDetailsContentParallax(
    comic: Comic,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val backgroundColor = MaterialTheme.colorScheme.surfaceColorAtElevation(SURFACE_ELEVATION)

    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopCenter
    ) {

        // Use image height to calculate the offset from the top of the screen for the title
        val localDensity = LocalDensity.current
        var imageHeightDp by remember { mutableStateOf(0.dp) }

        val imageUrl =
            "${comic.thumbnail?.path}.${comic.thumbnail?.extension}"
        AsyncImage(
            model =
            ImageRequest.Builder(context = LocalContext.current)
                .data(imageUrl)
                .crossfade(true).build(),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp)
                .graphicsLayer {
                    alpha =
                        min(
                            1f,
                            1 - (scrollState.value / 600f)
                        )
                    translationY = -scrollState.value * 0.1f
                    compositingStrategy = CompositingStrategy.Offscreen
                }
                .drawWithContent {
                    drawContent()
                    drawRect(
                        brush = Brush.verticalGradient(
                            0f to Color.Transparent,
                            0.8f to backgroundColor
                        )
                    )
                }
                .onGloballyPositioned { coordinates ->
                    imageHeightDp = with(localDensity) { coordinates.size.height.toDp() }
                }
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState)
        ) {

            Spacer(
                modifier = Modifier.height(
                    androidx.compose.ui.unit.max(
                        imageHeightDp * 0.5f,
                        0.dp
                    )
                )
            )

            ComicTitleText(
                text = comic.title ?: stringResource(id = R.string.untitled),
                modifier = Modifier
                    .padding(8.dp)
            )

            // Description
            comic.description?.let { description ->
                ComicDetailsBodyText(text = description)
            }

            // Text Objects
            comic.textObjects?.forEach { textObject ->
                if (textObject.language.equals(
                        Locale.getDefault().toLanguageTag(),
                        ignoreCase = true
                    )
                    && !textObject.text.isNullOrBlank()
                ) {
                    ComicDetailsBodyText(text = textObject.text)
                }
            }

            // Creators
            comic.creatorList?.items?.filter {
                !it.role.isNullOrBlank() && !it.name.isNullOrBlank()
            }?.let { creatorSummaryList ->
                if (creatorSummaryList.isNotEmpty()) {
                    ComicDetailsSubheaderText(
                        text = pluralStringResource(
                            id = R.plurals.creator,
                            count = creatorSummaryList.size
                        )
                    )
                    creatorSummaryList.forEach { creatorSummary ->
                        if (!creatorSummary.role.isNullOrBlank()) {
                            ComicDetailsBodyWithSubheaderBelow(
                                body = creatorSummary.name!!,
                                subheader = creatorSummary.role
                            )
                        } else {
                            ComicDetailsListItemText(text = creatorSummary.name!!)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Characters
            comic.characterList?.items?.filter {
                !it.name.isNullOrBlank()
            }?.let { characterSummaryList ->
                if (characterSummaryList.isNotEmpty()) {
                    ComicDetailsSubheaderText(
                        text = pluralStringResource(
                            id = R.plurals.character,
                            count = characterSummaryList.size
                        )
                    )
                    characterSummaryList.forEach { characterSummary ->
                        if (!characterSummary.role.isNullOrBlank()) {
                            ComicDetailsBodyWithSubheaderBelow(
                                body = characterSummary.name!!,
                                subheader = characterSummary.role
                            )
                        } else {
                            ComicDetailsListItemText(text = characterSummary.name!!)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun ComicTitleText(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier,
        color = MaterialTheme.colorScheme.onBackground,
        style = MaterialTheme.typography.displaySmall
    )
}

@Composable
fun ComicDetailsSubheaderText(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier.padding(vertical = 16.dp),
        color = MaterialTheme.colorScheme.secondary,
        style = MaterialTheme.typography.headlineMedium
    )
}

@Composable
fun ComicDetailsBodyText(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier.padding(vertical = 16.dp),
        color = MaterialTheme.colorScheme.secondary,
        style = MaterialTheme.typography.bodyLarge
    )
}

@Composable
fun ComicDetailsBodyWithSubheaderBelow(
    body: String,
    subheader: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = body,
            modifier = modifier.padding(top = 8.dp, bottom = 2.dp),
            color = MaterialTheme.colorScheme.secondary,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = subheader,
            modifier = modifier.padding(bottom = 8.dp),
            color = MaterialTheme.colorScheme.tertiary,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun ComicDetailsListItemText(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier.padding(vertical = 4.dp),
        color = MaterialTheme.colorScheme.secondary,
        style = MaterialTheme.typography.bodySmall
    )
}

@Preview
@Composable
private fun ComicDetailsBodyWithSubheaderBelowPreview() {
    ComicDetailsBodyWithSubheaderBelow(body = "Body Text", subheader = "Subheader")
}
