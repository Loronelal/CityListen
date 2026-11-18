package com.citylisten.presentation.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.citylisten.domain.model.Story
import com.citylisten.presentation.R
import com.citylisten.presentation.components.EmptyState
import com.citylisten.presentation.components.ErrorBanner
import com.citylisten.presentation.components.ListCard
import com.citylisten.presentation.components.LoadingState
import com.citylisten.presentation.components.MapSurface
import com.citylisten.presentation.components.ScreenHeader
import com.citylisten.presentation.ui.UiError
import com.citylisten.presentation.ui.messageRes
import com.citylisten.presentation.ui.sourceLabel
import com.citylisten.presentation.ui.theme.CityListenTheme
import java.time.Instant

@Composable
fun MapRoute(
    onBack: () -> Unit,
    onOpenStory: (String) -> Unit,
    viewModel: MapViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    MapScreen(
        uiState = uiState,
        onBack = onBack,
        onRetry = viewModel::retry,
        onOpenStory = onOpenStory
    )
}

@Composable
fun MapScreen(
    uiState: MapUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onOpenStory: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ScreenHeader(
            title = stringResource(R.string.map_title),
            onBack = onBack
        )

        when (uiState) {
            MapUiState.Loading -> MapLoading(modifier = Modifier.weight(1f))

            MapUiState.Empty -> MapEmpty(
                onRetry = onRetry,
                modifier = Modifier.weight(1f)
            )

            is MapUiState.Content -> MapContent(
                places = uiState.places,
                onOpenStory = onOpenStory,
                modifier = Modifier.weight(1f)
            )

            is MapUiState.Error -> MapError(
                error = uiState.error,
                onRetry = onRetry,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MapLoading(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth()) {
        MapSurface(
            modifier = Modifier.fillMaxSize(),
            shape = RectangleShape,
            label = null
        )
        LoadingState(
            message = stringResource(R.string.map_loading),
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
private fun MapContent(
    places: List<Story>,
    onOpenStory: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxWidth()) {
        MapSurface(
            modifier = Modifier.fillMaxSize(),
            shape = RectangleShape
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .widthIn(max = CONTENT_MAX_WIDTH)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            places.forEach { story ->
                ListCard(
                    title = story.title,
                    subtitle = "${story.sourceLabel()} · ${stringResource(R.string.map_saved)}",
                    onClick = { onOpenStory(story.id) }
                )
            }
        }
    }
}

@Composable
private fun MapEmpty(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        MapSurface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 8.dp, end = 16.dp)
                .height(190.dp)
        )
        EmptyState(
            emoji = "📫",
            title = stringResource(R.string.map_empty_title),
            message = stringResource(R.string.map_empty_message),
            actionLabel = stringResource(R.string.map_refresh),
            onAction = onRetry,
            outlinedAction = true,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
    }
}

@Composable
private fun MapError(
    error: UiError,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        ErrorBanner(
            message = stringResource(R.string.map_error_banner),
            onRetry = onRetry,
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 16.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "🗺️", fontSize = 56.sp)
            Text(
                text = stringResource(R.string.map_error_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 20.dp)
            )
            Text(
                text = errorMessage(error),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun errorMessage(error: UiError): String = when (error) {
    UiError.NETWORK, UiError.TIMEOUT -> stringResource(R.string.map_error_message)
    else -> stringResource(error.messageRes())
}

private val CONTENT_MAX_WIDTH = 640.dp

private val previewPark = Story(
    id = "park",
    placeId = "osm:park",
    title = "Городской парк",
    text = "Старейший парк города с вековыми деревьями.",
    language = "ru",
    createdAt = Instant.parse("2026-11-18T11:32:00Z"),
    cached = true
)

private val previewCafe = Story(
    id = "cafe",
    placeId = "osm:cafe",
    title = "Кафе «Утро»",
    text = "Небольшое кафе на углу улицы.",
    language = "ru",
    createdAt = Instant.parse("2026-11-18T10:05:00Z"),
    cached = true
)

@Preview(name = "Map · Content", showBackground = true)
@Composable
private fun MapContentPreview() {
    CityListenTheme {
        MapScreen(
            uiState = MapUiState.Content(listOf(previewPark, previewCafe)),
            onBack = {},
            onRetry = {},
            onOpenStory = {}
        )
    }
}

@Preview(name = "Map · Empty", showBackground = true)
@Composable
private fun MapEmptyPreview() {
    CityListenTheme {
        MapScreen(
            uiState = MapUiState.Empty,
            onBack = {},
            onRetry = {},
            onOpenStory = {}
        )
    }
}

@Preview(name = "Map · Error", showBackground = true)
@Composable
private fun MapErrorPreview() {
    CityListenTheme {
        MapScreen(
            uiState = MapUiState.Error(UiError.NETWORK),
            onBack = {},
            onRetry = {},
            onOpenStory = {}
        )
    }
}
