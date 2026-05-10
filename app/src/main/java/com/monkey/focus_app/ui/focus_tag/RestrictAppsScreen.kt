package com.monkey.focus_app.ui.focus_tag

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.monkey.focus_app.data.AppRepository
import com.monkey.focus_app.data.db.DatabaseBuilder
import com.monkey.focus_app.ui.theme.MONKeyTheme
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun RestrictAppsScreen(
    navController: NavController,
    tagId: String,
) {
    val context = LocalContext.current
    val database = remember { DatabaseBuilder.getInstance(context) }
    val repository = remember(database) {
        AppRepository(
            focusLogDao = database.focusLogDao(),
            rewardDao = database.rewardItemDao(),
            sessionDao = database.sessionDao(),
            tagDao = database.tagDao(),
            userStatsDao = database.userStatsDao()
        )
    }
    val factory = remember(repository, tagId) { RestrictAppsViewModelFactory(repository, tagId) }
    val restrictAppsViewModel: RestrictAppsViewModel = viewModel(factory = factory)
    val uiState by restrictAppsViewModel.uiState.collectAsState()
    val packageManager = context.packageManager

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        restrictAppsViewModel.loadInstalledApps(packageManager)
    }

    LaunchedEffect(Unit) {
        restrictAppsViewModel.effect.collect { effect ->
            when (effect) {
                RestrictAppsEffect.SaveSuccess -> navController.popBackStack()
                is RestrictAppsEffect.ShowMessage -> {
                    scope.launch { snackbarHostState.showSnackbar(effect.text) }
                }
            }
        }
    }

    RestrictAppsContent(
        query = uiState.query,
        apps = uiState.apps,
        selectedPackages = uiState.selectedPackages,
        snackbarHostState = snackbarHostState,
        onBackClick = { navController.popBackStack() },
        onQueryChanged = restrictAppsViewModel::onQueryChanged,
        onTogglePackage = restrictAppsViewModel::onTogglePackage,
        onConfirmSelection = restrictAppsViewModel::onConfirmSelection,
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
private fun RestrictAppsContent(
    query: String,
    apps: List<RestrictAppUi>,
    selectedPackages: Set<String>,
    snackbarHostState: SnackbarHostState,
    onBackClick: () -> Unit,
    onQueryChanged: (String) -> Unit,
    onTogglePackage: (String, Boolean) -> Unit,
    onConfirmSelection: () -> Unit,
) {
    val showSelectedOnly = remember { mutableStateOf(false) }
    val visibleApps = if (showSelectedOnly.value) {
        apps.filter { selectedPackages.contains(it.packageName) }
    } else {
        apps
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Button(
                    onClick = onConfirmSelection,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Confirm selection")
                    Text(
                        text = "Confirm Selection (${selectedPackages.size})",
                        modifier = Modifier.padding(start = 8.dp),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ){
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "Restrict Apps",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }

                FilledTonalButton(
                    onClick = { showSelectedOnly.value = !showSelectedOnly.value },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (showSelectedOnly.value) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onPrimary
                        },
                        contentColor = if (showSelectedOnly.value) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    ),
                ) {
                    Text(
                        text = if (showSelectedOnly.value) "All" else "Selected",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    )
                }
            }

            OutlinedTextField(
                value = query,
                onValueChange = onQueryChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Search apps") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                shape = MaterialTheme.shapes.medium,
                singleLine = true,
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(visibleApps, key = { it.packageName }) { app ->
                    val isSelected = selectedPackages.contains(app.packageName)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(14.dp),
                            )
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                val appIconBitmap = remember(app.packageName, app.icon) {
                                    app.icon?.toBitmap(width = 96, height = 96)?.asImageBitmap()
                                }

                                if (appIconBitmap != null) {
                                    Image(
                                        bitmap = appIconBitmap,
                                        contentDescription = app.appName,
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(CircleShape)
                                    )
                                } else {
                                    Text(
                                        text = app.appName.take(1).uppercase(),
                                        color = MaterialTheme.colorScheme.primary,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    )
                                }
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = app.appName,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = app.packageName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                                )
                            }
                        }
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { checked -> onTogglePackage(app.packageName, checked) }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RestrictAppsPreviewDark() {
    MONKeyTheme(darkTheme = true) {
        RestrictAppsContent(
            query = "",
            apps = listOf(
                RestrictAppUi("com.instagram.android", "Instagram", null),
                RestrictAppUi("com.tiktok.video", "TikTok", null),
            ),
            selectedPackages = setOf("com.instagram.android"),
            snackbarHostState = SnackbarHostState(),
            onBackClick = {},
            onQueryChanged = {},
            onTogglePackage = { _, _ -> },
            onConfirmSelection = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RestrictAppsPreviewLight() {
    MONKeyTheme(darkTheme = false) {
        RestrictAppsContent(
            query = "",
            apps = listOf(
                RestrictAppUi("com.instagram.android", "Instagram", null),
                RestrictAppUi("com.zhiliaoapp.musically", "TikTok", null),
            ),
            selectedPackages = setOf("com.instagram.android"),
            snackbarHostState = SnackbarHostState(),
            onBackClick = {},
            onQueryChanged = {},
            onTogglePackage = { _, _ -> },
            onConfirmSelection = {},
        )
    }
}
