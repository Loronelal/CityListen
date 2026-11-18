package com.citylisten.presentation.walk

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.citylisten.domain.model.Story
import com.citylisten.presentation.R
import com.citylisten.presentation.components.OfflineBadge
import com.citylisten.presentation.ui.theme.CityListenTheme
import java.time.Instant

@Composable
fun WalkRoute(
    onStartWalk: () -> Unit,
    viewModel: WalkViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    WalkScreen(
        uiState = uiState,
        onRetry = viewModel::retry,
        onStartWalk = onStartWalk
    )
}

@Composable
fun WalkScreen(
    uiState: WalkUiState,
    onRetry: () -> Unit,
    onStartWalk: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val contentModifier = Modifier
            .align(Alignment.Center)
            .widthIn(max = CONTENT_MAX_WIDTH)
            .fillMaxSize()
            .padding(16.dp)

        when (uiState) {
            WalkUiState.Loading -> LoadingContent(contentModifier)
            is WalkUiState.Empty -> EmptyContent(
                locationConsent = uiState.locationConsent,
                onStartWalk = onStartWalk,
                modifier = contentModifier
            )
            is WalkUiState.Content -> StoryContent(
                story = uiState.story,
                locationConsent = uiState.locationConsent,
                onStartWalk = onStartWalk,
                modifier = contentModifier
            )
            is WalkUiState.Error -> ErrorContent(
                error = uiState.error,
                onRetry = onRetry,
                modifier = contentModifier
            )
        }
    }
}

@Composable
private fun LoadingContent(modifier: Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator()
            Text(
                text = stringResource(R.string.walk_loading),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyContent(
    locationConsent: Boolean,
    onStartWalk: () -> Unit,
    modifier: Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!locationConsent) {
            ConsentBanner()
            Spacer(modifier = Modifier.height(16.dp))
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "🎧  🗺️",
            fontSize = 64.sp,
            lineHeight = 72.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.walk_empty_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.walk_empty_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.weight(1f))
        StartWalkButton(onClick = onStartWalk)
    }
}

@Composable
private fun StoryContent(
    story: Story,
    locationConsent: Boolean,
    onStartWalk: () -> Unit,
    modifier: Modifier
) {
    Column(modifier = modifier) {
        if (!locationConsent) {
            ConsentBanner()
            Spacer(modifier = Modifier.height(12.dp))
        }
        MapPlaceholder(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.walk_last_story),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        LastStoryCard(story = story)
        Spacer(modifier = Modifier.weight(1f))
        StartWalkButton(onClick = onStartWalk)
    }
}

@Composable
private fun ErrorContent(
    error: WalkError,
    onRetry: () -> Unit,
    modifier: Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.walk_error_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(error.messageRes()),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Text(text = stringResource(R.string.walk_retry))
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun ConsentBanner() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = stringResource(R.string.walk_consent_disabled),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
private fun MapPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "📍 " + stringResource(R.string.walk_map_placeholder),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LastStoryCard(story: Story) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = story.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f)
                )
                OfflineBadge()
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = story.text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun StartWalkButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Text(
            text = stringResource(R.string.walk_start),
            style = MaterialTheme.typography.labelLarge
        )
    }
}

private fun WalkError.messageRes(): Int = when (this) {
    WalkError.NETWORK -> R.string.walk_error_network
    WalkError.TIMEOUT -> R.string.walk_error_timeout
    WalkError.SERVER -> R.string.walk_error_server
    WalkError.UNKNOWN -> R.string.walk_error_unknown
}

private val CONTENT_MAX_WIDTH = 640.dp

private val previewStory = Story(
    id = "preview",
    placeId = "osm:1",
    title = "Административная, 8",
    text = "Вы находитесь у здания по адресу Административная улица, 8. " +
        "Типичная городская застройка начала XX века.",
    language = "ru",
    createdAt = Instant.parse("2026-11-17T10:00:00Z"),
    cached = true
)

@Preview(name = "Walk · Empty", showBackground = true)
@Composable
private fun WalkEmptyPreview() {
    CityListenTheme {
        WalkScreen(
            uiState = WalkUiState.Empty(locationConsent = true),
            onRetry = {},
            onStartWalk = {}
        )
    }
}

@Preview(name = "Walk · Content", showBackground = true)
@Composable
private fun WalkContentPreview() {
    CityListenTheme {
        WalkScreen(
            uiState = WalkUiState.Content(previewStory, locationConsent = true),
            onRetry = {},
            onStartWalk = {}
        )
    }
}

@Preview(name = "Walk · Error", showBackground = true)
@Composable
private fun WalkErrorPreview() {
    CityListenTheme {
        WalkScreen(
            uiState = WalkUiState.Error(WalkError.NETWORK),
            onRetry = {},
            onStartWalk = {}
        )
    }
}
