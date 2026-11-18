package com.citylisten.presentation.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.citylisten.presentation.components.ErrorBanner
import com.citylisten.presentation.components.ListCard
import com.citylisten.presentation.components.LoadingState
import com.citylisten.presentation.components.ScreenHeader
import com.citylisten.presentation.ui.UiError
import com.citylisten.presentation.ui.sourceLabel
import com.citylisten.presentation.ui.timeLabel
import com.citylisten.presentation.ui.theme.CityListenTheme
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HistoryRoute(
    onBack: () -> Unit,
    onOpenStory: (String) -> Unit,
    onStartWalk: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HistoryScreen(
        uiState = uiState,
        onBack = onBack,
        onRetry = viewModel::retry,
        onOpenStory = onOpenStory,
        onStartWalk = onStartWalk
    )
}

@Composable
fun HistoryScreen(
    uiState: HistoryUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onOpenStory: (String) -> Unit,
    onStartWalk: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ScreenHeader(
            title = stringResource(R.string.history_title),
            onBack = onBack
        )

        when (uiState) {
            HistoryUiState.Loading -> LoadingState(
                message = stringResource(R.string.history_loading),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            HistoryUiState.Empty -> EmptyState(
                emoji = "📜",
                title = stringResource(R.string.history_empty_title),
                message = stringResource(R.string.history_empty_message),
                actionLabel = stringResource(R.string.walk_start),
                onAction = onStartWalk,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            is HistoryUiState.Content -> HistoryList(
                days = uiState.days,
                onOpenStory = onOpenStory,
                modifier = Modifier.weight(1f)
            )

            is HistoryUiState.Error -> ErrorContent(
                state = uiState,
                onRetry = onRetry,
                onOpenStory = onOpenStory,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ErrorContent(
    state: HistoryUiState.Error,
    onRetry: () -> Unit,
    onOpenStory: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        ErrorBanner(
            message = stringResource(R.string.history_error_banner),
            onRetry = onRetry,
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 16.dp)
        )
        if (state.cachedDays.isNotEmpty()) {
            HistoryList(
                days = state.cachedDays,
                onOpenStory = onOpenStory,
                modifier = Modifier.weight(1f),
                header = stringResource(R.string.history_from_cache),
                showDayLabels = false
            )
        }
    }
}

@Composable
private fun HistoryList(
    days: List<HistoryDay>,
    onOpenStory: (String) -> Unit,
    modifier: Modifier = Modifier,
    header: String? = null,
    showDayLabels: Boolean = true
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (header != null) {
            item(key = "header") {
                GroupLabel(text = header)
            }
        }
        days.forEach { day ->
            if (showDayLabels) {
                item(key = "day-${day.date}") {
                    GroupLabel(text = dayLabel(day.date))
                }
            }
            items(items = day.stories, key = { it.id }) { story ->
                ListCard(
                    title = story.title,
                    subtitle = "${story.timeLabel()} · ${story.sourceLabel()}",
                    onClick = { onOpenStory(story.id) }
                )
            }
        }
    }
}

@Composable
private fun GroupLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 2.dp)
    )
}

@Composable
private fun dayLabel(date: LocalDate): String {
    val today = LocalDate.now()
    return when (date) {
        today -> stringResource(R.string.history_today)
        today.minusDays(1) -> stringResource(R.string.history_yesterday)
        else -> date.format(historyDateFormatter)
    }
}

private val historyDateFormatter = DateTimeFormatter.ofPattern("d MMMM", Locale.getDefault())

private val previewToday = Story(
    id = "today-1",
    placeId = "osm:101",
    title = "Административная, 8",
    text = "Типичная городская застройка начала XX века.",
    language = "ru",
    createdAt = Instant.parse("2026-11-18T11:32:00Z"),
    cached = false
)

private val previewTodaySecond = Story(
    id = "today-2",
    placeId = "wd:202",
    title = "Памятник солдату",
    text = "Мемориал в честь защитников города.",
    language = "ru",
    createdAt = Instant.parse("2026-11-18T11:18:00Z"),
    cached = false
)

private val previewYesterday = Story(
    id = "yesterday",
    placeId = "osm:303",
    title = "Городской парк",
    text = "Старейший парк города с вековыми деревьями.",
    language = "ru",
    createdAt = Instant.parse("2026-11-17T16:05:00Z"),
    cached = true
)

@Preview(name = "History · Content", showBackground = true)
@Composable
private fun HistoryContentPreview() {
    CityListenTheme {
        HistoryScreen(
            uiState = HistoryUiState.Content(
                days = listOf(
                    HistoryDay(LocalDate.now(), listOf(previewToday, previewTodaySecond)),
                    HistoryDay(LocalDate.now().minusDays(1), listOf(previewYesterday))
                )
            ),
            onBack = {},
            onRetry = {},
            onOpenStory = {},
            onStartWalk = {}
        )
    }
}

@Preview(name = "History · Empty", showBackground = true)
@Composable
private fun HistoryEmptyPreview() {
    CityListenTheme {
        HistoryScreen(
            uiState = HistoryUiState.Empty,
            onBack = {},
            onRetry = {},
            onOpenStory = {},
            onStartWalk = {}
        )
    }
}

@Preview(name = "History · Error", showBackground = true)
@Composable
private fun HistoryErrorPreview() {
    CityListenTheme {
        HistoryScreen(
            uiState = HistoryUiState.Error(
                error = UiError.NETWORK,
                cachedDays = listOf(HistoryDay(LocalDate.now(), listOf(previewToday)))
            ),
            onBack = {},
            onRetry = {},
            onOpenStory = {},
            onStartWalk = {}
        )
    }
}
