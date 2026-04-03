package com.monkey.focus_app.ui.settings

import android.Manifest
import android.os.Build
import android.widget.Space
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.monkey.focus_app.ui.theme.MONKeyTheme

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val appContext = context.applicationContext
    val lifecycleOwner = LocalLifecycleOwner.current
    val factory = remember(appContext) { SettingsViewModelFactory(appContext) }
    val settingsViewModel: SettingsViewModel = viewModel(factory = factory)
    val uiState by settingsViewModel.uiState.collectAsState()

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        settingsViewModel.refreshStatus()
    }

    LaunchedEffect(Unit) {
        settingsViewModel.effect.collect { effect ->
            when (effect) {
                SettingsEffect.OpenAccessibilitySettings -> {
                    context.startActivity(PermissionSetup.createAccessibilitySettingsIntent())
                }

                SettingsEffect.OpenExactAlarmSettings -> {
                    context.startActivity(PermissionSetup.createExactAlarmSettingsIntent(context))
                }

                SettingsEffect.RequestNotificationPermission -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                settingsViewModel.refreshStatus()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    SettingsScreenContent(
        uiState = uiState,
        onAccessibilityClick = settingsViewModel::onAccessibilityClicked,
        onExactAlarmClick = settingsViewModel::onExactAlarmClicked,
        onNotificationsClick = settingsViewModel::onNotificationsClicked,
    )
}

@Composable
fun SettingsScreenContent(
    uiState: SettingsUiState,
    onAccessibilityClick: () -> Unit,
    onExactAlarmClick: () -> Unit,
    onNotificationsClick: () -> Unit,
) {
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column(modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)) {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (uiState.permissionStatus.allReady) "All required permissions are ready"
                        else "Please complete all required permissions",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                PermissionItemCard(
                    title = "Accessibility Service",
                    description = "Required to detect blocked app openings",
                    enabled = uiState.permissionStatus.accessibilityEnabled,
                    onActionClick = onAccessibilityClick
                )
            }

            item {
                PermissionItemCard(
                    title = "Exact Alarms",
                    description = "Required for precise session start/end timing",
                    enabled = uiState.permissionStatus.exactAlarmAllowed,
                    onActionClick = onExactAlarmClick
                )
            }

            item {
                PermissionItemCard(
                    title = "Notifications",
                    description = "Required for reminders and ongoing focus notification",
                    enabled = uiState.permissionStatus.notificationsAllowed,
                    onActionClick = onNotificationsClick
                )
            }
        }
    }
}

@Composable
private fun PermissionItemCard(
    title: String,
    description: String,
    enabled: Boolean,
    onActionClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        onClick = onActionClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.width(300.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )

                Text(
                    text = if (enabled) "Tap to review setting" else "Tap to enable",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Icon(
                modifier = Modifier.size(40.dp),
                imageVector = if (enabled) Icons.Default.CheckCircle else Icons.Default.Error,
                contentDescription = if (enabled) "Enabled" else "Not enabled",
                tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun SettingsScreenContentPreviewLight() {
    MONKeyTheme(darkTheme = true) {
        SettingsScreenContent(
            uiState = SettingsUiState(
                permissionStatus = PermissionChecklistStatus(
                    accessibilityEnabled = false,
                    exactAlarmAllowed = true,
                    notificationsAllowed = false
                )
            ),
            onAccessibilityClick = {},
            onExactAlarmClick = {},
            onNotificationsClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenContentPreviewDark() {
    MONKeyTheme(darkTheme = false) {
        SettingsScreenContent(
            uiState = SettingsUiState(
                permissionStatus = PermissionChecklistStatus(
                    accessibilityEnabled = true,
                    exactAlarmAllowed = true,
                    notificationsAllowed = true
                )
            ),
            onAccessibilityClick = {},
            onExactAlarmClick = {},
            onNotificationsClick = {}
        )
    }
}


