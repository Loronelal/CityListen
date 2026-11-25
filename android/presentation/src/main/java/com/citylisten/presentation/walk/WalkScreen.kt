package com.citylisten.presentation.walk

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.citylisten.domain.model.Story
import com.citylisten.presentation.R
import com.citylisten.presentation.components.ErrorOverlayDialog
import com.citylisten.presentation.components.MapSurface
import com.citylisten.presentation.components.OfflineBadge
import com.citylisten.presentation.permission.hasLocationPermission
import com.citylisten.presentation.ui.messageRes
import com.citylisten.presentation.ui.theme.CityListenTheme
import java.time.Instant

@Composable
fun WalkRoute(
    onRequestPermission: () -> Unit,
    viewModel: WalkViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val locationState by viewModel.locationState.collectAsStateWithLifecycle()
    val addressState by viewModel.addressState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LifecycleResumeEffect(viewModel) {
        viewModel.resumeWalk()
        onPauseOrDispose { viewModel.pauseWalk() }
    }

    WalkScreen(
        uiState = uiState,
        locationState = locationState,
        addressState = addressState,
        onRetry = viewModel::retry,
        onRetryAddress = viewModel::retryAddress,
        onStartWalk = {
            val hasConsent = when (val state = uiState) {
                is WalkUiState.Empty -> state.locationConsent
                is WalkUiState.Content -> state.locationConsent
                else -> false
            }
            if (!hasConsent || !context.hasLocationPermission()) {
                onRequestPermission()
            } else {
                viewModel.startWalk()
            }
        },
        onStopWalk = viewModel::stopWalk
    )
}

@Composable
fun WalkScreen(
    uiState: WalkUiState,
    locationState: WalkLocationUiState,
    addressState: WalkAddressUiState,
    onRetry: () -> Unit,
    onRetryAddress: () -> Unit,
    onStartWalk: () -> Unit,
    onStopWalk: () -> Unit,
    modifier: Modifier = Modifier
) {
    val errorState = uiState as? WalkUiState.Error
    var showErrorOverlay by remember(uiState) {
        mutableStateOf(errorState != null)
    }

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
                locationState = locationState,
                addressState = addressState,
                onStartWalk = onStartWalk,
                onStopWalk = onStopWalk,
                onRetryAddress = onRetryAddress,
                modifier = contentModifier
            )
            is WalkUiState.Content -> StoryContent(
                story = uiState.story,
                locationConsent = uiState.locationConsent,
                locationState = locationState,
                addressState = addressState,
                onStartWalk = onStartWalk,
                onStopWalk = onStopWalk,
                onRetryAddress = onRetryAddress,
                modifier = contentModifier
            )
            is WalkUiState.Error -> ErrorContent(
                error = uiState.error,
                onRetry = onRetry,
                modifier = contentModifier
            )
        }

        if (errorState != null && showErrorOverlay) {
            ErrorOverlayDialog(
                message = stringResource(errorState.error.messageRes()),
                onRetry = {
                    showErrorOverlay = false
                    onRetry()
                },
                onDismiss = { showErrorOverlay = false }
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
    locationState: WalkLocationUiState,
    addressState: WalkAddressUiState,
    onStartWalk: () -> Unit,
    onStopWalk: () -> Unit,
    onRetryAddress: () -> Unit,
    modifier: Modifier
) {
    val compact = LocalConfiguration.current.screenHeightDp < COMPACT_HEIGHT_DP
    WalkContentLayout(
        modifier = modifier,
        bottomContent = {
            StartWalkButton(
                isWalking = locationState.isWalking,
                onClick = if (locationState.isWalking) onStopWalk else onStartWalk
            )
        }
    ) {
        if (!locationConsent) {
            ConsentBanner()
            Spacer(modifier = Modifier.height(16.dp))
        }
        if (!compact) {
            Text(
                text = "🎧  🗺️",
                fontSize = 64.sp,
                lineHeight = 72.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
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
        if (locationState.isWalking || locationState.error != null) {
            Spacer(modifier = Modifier.height(16.dp))
            LocationStatusCard(state = locationState)
        }
        if (addressState !is WalkAddressUiState.Idle) {
            Spacer(modifier = Modifier.height(12.dp))
            AddressStatusCard(state = addressState, onRetry = onRetryAddress)
        }
    }
}

@Composable
private fun StoryContent(
    story: Story,
    locationConsent: Boolean,
    locationState: WalkLocationUiState,
    addressState: WalkAddressUiState,
    onStartWalk: () -> Unit,
    onStopWalk: () -> Unit,
    onRetryAddress: () -> Unit,
    modifier: Modifier
) {
    val compact = LocalConfiguration.current.screenHeightDp < COMPACT_HEIGHT_DP
    WalkContentLayout(
        modifier = modifier,
        bottomContent = {
            StartWalkButton(
                isWalking = locationState.isWalking,
                onClick = if (locationState.isWalking) onStopWalk else onStartWalk
            )
        }
    ) {
        if (!locationConsent) {
            ConsentBanner()
            Spacer(modifier = Modifier.height(12.dp))
        }
        MapPlaceholder(
            locationState = locationState,
            modifier = Modifier
                .fillMaxWidth()
                .height(if (compact) 140.dp else 220.dp)
        )
        if (locationState.isWalking || locationState.error != null) {
            Spacer(modifier = Modifier.height(12.dp))
            LocationStatusCard(state = locationState)
        }
        if (addressState !is WalkAddressUiState.Idle) {
            Spacer(modifier = Modifier.height(12.dp))
            AddressStatusCard(state = addressState, onRetry = onRetryAddress)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.walk_last_story),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        LastStoryCard(story = story)
    }
}

@Composable
private fun ErrorContent(
    error: WalkError,
    onRetry: () -> Unit,
    modifier: Modifier
) {
    WalkContentLayout(
        modifier = modifier,
        bottomContent = {
            Button(onClick = onRetry) {
                Text(text = stringResource(R.string.walk_retry))
            }
        }
    ) {
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
    }
}

@Composable
private fun WalkContentLayout(
    modifier: Modifier,
    bottomContent: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .heightIn(min = maxHeight),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                content = content
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        bottomContent()
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
private fun MapPlaceholder(
    locationState: WalkLocationUiState,
    modifier: Modifier = Modifier
) {
    val label = locationState.point?.let { point ->
        stringResource(
            R.string.walk_location_coordinates,
            point.latitude,
            point.longitude
        )
    } ?: stringResource(R.string.walk_map_placeholder)

    MapSurface(
        modifier = modifier,
        label = label
    )
}

@Composable
private fun LocationStatusCard(
    state: WalkLocationUiState,
    modifier: Modifier = Modifier
) {
    val isError = state.error != null
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isError) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.secondaryContainer
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Place,
                contentDescription = null,
                tint = if (isError) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                },
                modifier = Modifier.size(20.dp)
            )
            Column {
                Text(
                    text = stringResource(R.string.walk_location_title),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isError) {
                        MaterialTheme.colorScheme.onErrorContainer
                    } else {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    }
                )
                Text(
                    text = when {
                        state.error == WalkLocationError.PERMISSION ->
                            stringResource(R.string.walk_location_permission_error)
                        state.error != null ->
                            stringResource(R.string.walk_location_unavailable)
                        state.point != null -> stringResource(
                            R.string.walk_location_coordinates,
                            state.point.latitude,
                            state.point.longitude
                        )
                        else -> stringResource(R.string.walk_location_searching)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isError) {
                        MaterialTheme.colorScheme.onErrorContainer
                    } else {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    }
                )
            }
        }
    }
}

@Composable
private fun AddressStatusCard(
    state: WalkAddressUiState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isError = state is WalkAddressUiState.Error
    val contentColor = if (isError) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.colorScheme.onSecondaryContainer
    }
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isError) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.secondaryContainer
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Place,
                contentDescription = null,
                tint = if (isError) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                },
                modifier = Modifier.size(20.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.walk_address_title),
                    style = MaterialTheme.typography.labelLarge,
                    color = contentColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                when (state) {
                    WalkAddressUiState.Idle -> Unit
                    WalkAddressUiState.Loading -> Text(
                        text = stringResource(R.string.walk_address_loading),
                        style = MaterialTheme.typography.bodyMedium,
                        color = contentColor
                    )
                    is WalkAddressUiState.Content -> Text(
                        text = state.address.shortName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = contentColor
                    )
                    is WalkAddressUiState.Error -> {
                        Text(
                            text = stringResource(state.error.messageRes()),
                            style = MaterialTheme.typography.bodyMedium,
                            color = contentColor
                        )
                        TextButton(
                            onClick = onRetry,
                            contentPadding = PaddingValues(horizontal = 0.dp, vertical = 4.dp)
                        ) {
                            Text(text = stringResource(R.string.action_retry))
                        }
                    }
                }
            }
        }
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
private fun StartWalkButton(
    isWalking: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Text(
            text = stringResource(
                if (isWalking) R.string.walk_stop else R.string.walk_start
            ),
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
private const val COMPACT_HEIGHT_DP = 500

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
            locationState = WalkLocationUiState(),
            addressState = WalkAddressUiState.Idle,
            onRetry = {},
            onRetryAddress = {},
            onStartWalk = {},
            onStopWalk = {}
        )
    }
}

@Preview(name = "Walk · Content", showBackground = true)
@Composable
private fun WalkContentPreview() {
    CityListenTheme {
        WalkScreen(
            uiState = WalkUiState.Content(previewStory, locationConsent = true),
            locationState = WalkLocationUiState(),
            addressState = WalkAddressUiState.Idle,
            onRetry = {},
            onRetryAddress = {},
            onStartWalk = {},
            onStopWalk = {}
        )
    }
}

@Preview(name = "Walk · Error", showBackground = true)
@Composable
private fun WalkErrorPreview() {
    CityListenTheme {
        WalkScreen(
            uiState = WalkUiState.Error(WalkError.NETWORK),
            locationState = WalkLocationUiState(),
            addressState = WalkAddressUiState.Idle,
            onRetry = {},
            onRetryAddress = {},
            onStartWalk = {},
            onStopWalk = {}
        )
    }
}
