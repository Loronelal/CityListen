package com.citylisten.presentation.story

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.citylisten.domain.model.Story
import com.citylisten.presentation.R
import com.citylisten.presentation.components.EmptyState
import com.citylisten.presentation.components.LoadingState
import com.citylisten.presentation.components.ScreenHeader
import com.citylisten.presentation.ui.sourceLabel
import com.citylisten.presentation.ui.timeLabel
import com.citylisten.presentation.ui.theme.CityListenTheme
import java.time.Instant

@Composable
fun StoryRoute(
    onBack: () -> Unit,
    viewModel: StoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    StoryScreen(
        uiState = uiState,
        onBack = onBack
    )
}

@Composable
fun StoryScreen(
    uiState: StoryUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ScreenHeader(
            title = stringResource(R.string.story_title),
            onBack = onBack
        )

        when (uiState) {
            StoryUiState.Loading -> LoadingState(
                message = stringResource(R.string.story_loading),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            is StoryUiState.Content -> StoryContent(
                story = uiState.story,
                modifier = Modifier.weight(1f)
            )

            StoryUiState.NotFound -> EmptyState(
                emoji = "📭",
                title = stringResource(R.string.story_not_found_title),
                message = stringResource(R.string.story_not_found_message),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }
}

@Composable
private fun StoryContent(
    story: Story,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            start = 20.dp,
            top = 8.dp,
            end = 20.dp,
            bottom = 32.dp
        )
    ) {
        item {
            Text(
                text = story.title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        item {
            Text(
                text = "${story.timeLabel()} · ${story.sourceLabel()}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        item {
            Text(
                text = story.text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 20.dp)
            )
        }
    }
}

private val previewStory = Story(
    id = "preview",
    placeId = "osm:1",
    title = "Административная, 8",
    text = "Вы находитесь у здания по адресу Административная улица, 8. " +
        "Типичная городская застройка начала XX века. Дом сохранил первоначальную " +
        "планировку и лепной декор фасада.",
    language = "ru",
    createdAt = Instant.parse("2026-11-18T11:32:00Z"),
    cached = true
)

@Preview(name = "Story · Content", showBackground = true)
@Composable
private fun StoryContentPreview() {
    CityListenTheme {
        StoryScreen(
            uiState = StoryUiState.Content(previewStory),
            onBack = {}
        )
    }
}

@Preview(name = "Story · Not found", showBackground = true)
@Composable
private fun StoryNotFoundPreview() {
    CityListenTheme {
        StoryScreen(
            uiState = StoryUiState.NotFound,
            onBack = {}
        )
    }
}
