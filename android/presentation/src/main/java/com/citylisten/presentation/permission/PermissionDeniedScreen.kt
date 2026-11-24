package com.citylisten.presentation.permission

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.citylisten.presentation.R
import com.citylisten.presentation.components.ScreenHeader
import com.citylisten.presentation.ui.theme.CityListenTheme

@Composable
fun PermissionDeniedRoute(
    canAskAgain: Boolean,
    onGranted: () -> Unit,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    viewModel: PermissionViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    LifecycleResumeEffect(Unit) {
        if (context.hasLocationPermission()) {
            viewModel.setConsent(true)
            onGranted()
        }
        onPauseOrDispose {}
    }

    PermissionDeniedScreen(
        canAskAgain = canAskAgain,
        onOpenSettings = { context.openAppSettings() },
        onRetry = onRetry,
        onBack = onBack
    )
}

@Composable
fun PermissionDeniedScreen(
    canAskAgain: Boolean,
    onOpenSettings: () -> Unit,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ScreenHeader(
            title = stringResource(R.string.settings_location),
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
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
                        text = stringResource(R.string.permission_denied_banner),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = stringResource(
                    if (canAskAgain) {
                        R.string.permission_denied_title
                    } else {
                        R.string.permission_denied_permanent_title
                    }
                ),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(
                    if (canAskAgain) {
                        R.string.permission_denied_message
                    } else {
                        R.string.permission_denied_permanent_message
                    }
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(1.2f))

            Button(
                onClick = if (canAskAgain) onRetry else onOpenSettings,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = stringResource(
                        if (canAskAgain) {
                            R.string.permission_retry
                        } else {
                            R.string.permission_open_settings
                        }
                    ),
                    style = MaterialTheme.typography.labelLarge
                )
            }
            TextButton(onClick = if (canAskAgain) onOpenSettings else onBack) {
                Text(
                    text = stringResource(
                        if (canAskAgain) {
                            R.string.permission_open_settings
                        } else {
                            R.string.action_back
                        }
                    )
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(name = "Permission — Denied", showBackground = true, heightDp = 874)
@Composable
private fun PermissionDeniedPreview() {
    CityListenTheme {
        PermissionDeniedScreen(
            canAskAgain = true,
            onOpenSettings = {},
            onRetry = {},
            onBack = {}
        )
    }
}

@Preview(name = "Permission — Blocked", showBackground = true, heightDp = 874)
@Composable
private fun PermissionBlockedPreview() {
    CityListenTheme {
        PermissionDeniedScreen(
            canAskAgain = false,
            onOpenSettings = {},
            onRetry = {},
            onBack = {}
        )
    }
}
