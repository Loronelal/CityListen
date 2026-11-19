package com.citylisten.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.citylisten.presentation.R
import com.citylisten.presentation.components.ScreenHeader
import com.citylisten.presentation.ui.theme.CityListenTheme

@Composable
fun ProfileRoute(
    onBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ProfileScreen(
        uiState = uiState,
        onBack = onBack,
        onNameChange = viewModel::onNameChange,
        onSave = viewModel::save
    )
}

@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    onBack: () -> Unit,
    onNameChange: (String) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ScreenHeader(
            title = stringResource(R.string.profile_title),
            onBack = onBack
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(42.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.profile_guest_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.profile_guest_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            OutlinedTextField(
                value = uiState.displayName,
                onValueChange = onNameChange,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading && !uiState.isSaving,
                singleLine = true,
                label = { Text(text = stringResource(R.string.profile_name_label)) },
                isError = uiState.error != null,
                supportingText = {
                    Text(
                        text = uiState.error
                            ?: stringResource(R.string.profile_name_hint)
                    )
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onSave() }),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onSave,
                enabled = !uiState.isLoading &&
                    uiState.displayName.isNotBlank() &&
                    !uiState.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = stringResource(R.string.profile_save),
                    style = MaterialTheme.typography.labelLarge
                )
            }

            if (uiState.isSaved) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.profile_saved),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Preview(name = "Profile", showBackground = true, heightDp = 874)
@Composable
private fun ProfilePreview() {
    CityListenTheme {
        ProfileScreen(
            uiState = ProfileUiState(
                isLoading = false,
                displayName = "Анна",
                savedName = "Анна",
                isSaved = true
            ),
            onBack = {},
            onNameChange = {},
            onSave = {}
        )
    }
}

@Preview(name = "Profile · Error", showBackground = true, heightDp = 874)
@Composable
private fun ProfileErrorPreview() {
    CityListenTheme {
        ProfileScreen(
            uiState = ProfileUiState(
                isLoading = false,
                displayName = "А",
                error = "Имя должно содержать не меньше 2 символов"
            ),
            onBack = {},
            onNameChange = {},
            onSave = {}
        )
    }
}
