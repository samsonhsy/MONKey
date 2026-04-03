package com.monkey.focus_app.ui.home

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.monkey.focus_app.data.AppRepository
import com.monkey.focus_app.data.db.DatabaseBuilder
import com.monkey.focus_app.ui.navigation.MainRoute
import com.monkey.focus_app.ui.navigation.navigateToTopLevel
import com.monkey.focus_app.ui.settings.PermissionChecklistStatus
import com.monkey.focus_app.ui.theme.MONKeyTheme
import com.monkey.focus_app.service.focus.FocusActions
import com.monkey.focus_app.ui.warning.WarningActivity
import androidx.compose.foundation.background

@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current

    val database = remember {
        DatabaseBuilder.getInstance(context)
    }

    val repository = remember(database) {
        AppRepository(
            focusLogDao = database.focusLogDao(),
            rewardDao = database.rewardItemDao(),
            sessionDao = database.sessionDao(),
            tagDao = database.tagDao(),
            userStatsDao = database.userStatsDao()
        )
    }

    val appContext = context.applicationContext
    val factory = remember(repository, appContext) { HomeViewModelFactory(repository, appContext) }
    val homeViewModel: HomeViewModel = viewModel(factory = factory)
    val uiState by homeViewModel.uiState.collectAsState()

    LaunchedEffect(navController.currentBackStackEntry) {
        homeViewModel.refreshPermissionStatus()
    }

    LaunchedEffect(Unit) {
        homeViewModel.effect.collect { effect ->
            when (effect) {
                HomeEffect.NavigateToCreateSession -> {
                    navController.navigate(MainRoute.SessionEdit.create("new"))
                }

                HomeEffect.NavigateToSessionList -> {
                    navController.navigateToTopLevel(MainRoute.SessionList.route)
                }

                HomeEffect.NavigateToSettings -> {
                    navController.navigateToTopLevel(MainRoute.Settings.route)
                }
            }
        }
    }

    HomeScreenContent(
        sessions = uiState.todaySessions,
        weeklyFocusText = uiState.weeklyFocusText,
        permissionStatus = uiState.permissionStatus,
        showFirstSetupDialog = uiState.showFirstSetupDialog,
        onStartFocusClick = homeViewModel::onStartFocusClicked,
        onViewAllClick = homeViewModel::onViewAllClicked,
        onPermissionWarningClick = homeViewModel::onPermissionWarningClicked,
        onDismissSetupDialog = homeViewModel::dismissFirstSetupDialog,
        onOpenSetupFromDialog = homeViewModel::openSetupFromDialog,
    )
}
@Composable
fun HomeScreenContent(
    sessions: List<HomeSessionItemUi>,
    weeklyFocusText: String,
    permissionStatus: PermissionChecklistStatus = PermissionChecklistStatus(false, false, false),
    showFirstSetupDialog: Boolean = false,
    modifier: Modifier = Modifier,
    onStartFocusClick: () -> Unit = {},
    onViewAllClick: () -> Unit = {},
    onPermissionWarningClick: () -> Unit = {},
    onDismissSetupDialog: () -> Unit = {},
    onOpenSetupFromDialog: () -> Unit = {},
) {
    val context = LocalContext.current
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            TopBarSection(
                showPermissionWarning = !permissionStatus.allReady,
                onPermissionWarningClick = onPermissionWarningClick
            )
        }
        item{
            Spacer(modifier = Modifier.height(8.dp))
        }
        item {
            StartFocusButton(onClick = onStartFocusClick)
        }
        item{
            Spacer(modifier = Modifier.height(8.dp))
        }
        item {
            StatsSection(weeklyFocusText = weeklyFocusText)
        }
        item{
            Spacer(modifier = Modifier.height(8.dp))
        }
        item {
            HeaderWithViewAll(title = "Today's Sessions", onViewAllClick = onViewAllClick)
        }
        items(sessions) { session ->
            SessionCard(
                session = session,
                onClick = {
                    if (session.isActive) {
                        val intent = Intent(context, WarningActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            putExtra(com.monkey.focus_app.service.focus.FocusActions.EXTRA_SESSION_ID, session.id)
                            putExtra(com.monkey.focus_app.service.focus.FocusActions.EXTRA_BLOCKED_PACKAGE, "")
                            putExtra(com.monkey.focus_app.service.focus.FocusActions.EXTRA_UNLOCK_LEVEL, "") // Note: ideally we'd pass real values if needed
                        }
                        context.startActivity(intent)
                    }
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }

    if (showFirstSetupDialog) {
        AlertDialog(
            onDismissRequest = onDismissSetupDialog,
            title = { Text("Complete Setup") },
            text = {
                Text("To make app functionable, please enable the required permissions in Settings.")
            },
            confirmButton = {
                TextButton(onClick = onOpenSetupFromDialog) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissSetupDialog) {
                    Text("Later")
                }
            }
        )
    }
}

@Composable
private fun TopBarSection(
    showPermissionWarning: Boolean,
    onPermissionWarningClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Welcome to MONKey 🙈",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (showPermissionWarning) {
            IconButton(onClick = onPermissionWarningClick) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Permissions setup required",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun StartFocusButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(250.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "PLAN TO FOCUS",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
                )
            }
        }
    }
}
@Composable
private fun StatsSection(weeklyFocusText: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            label = "Total Focus This Week",
            value = weeklyFocusText,
        )
//        StatCard(
//            modifier = Modifier.weight(1f),
//            label = "MONKney Balance",
//            value = "1,250",
//        )
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant) // Automatically handles Dark/Light mode!
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f) // Good contrast
        )
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant // Good contrast
        )

    }
}

@Composable
private fun HeaderWithViewAll(title: String, onViewAllClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "View All",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable(onClick = onViewAllClick)
        )
    }
}

@Composable
private fun SessionCard(
    session: HomeSessionItemUi,
    onClick: () -> Unit = {}
) {
    val backgroundColor = if (session.isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (session.isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable(enabled = session.isActive, onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column() {
            Text(
                text = session.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                color = textColor
            )
            Text(
                text = session.timeslot,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor.copy(alpha = 0.8f)
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = session.duration,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor.copy(alpha = 0.8f)
            )
            // Recurrence Chip
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(textColor.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (session.isActive) "ACTIVE NOW" else session.recurrence,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = textColor
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun HomeScreenPreviewDark() {
    MONKeyTheme(darkTheme = true) {
        HomeScreenContent(
            sessions = listOf(
                HomeSessionItemUi(1, "CSCI lecture", "09:00 - 10:00", "120 min", "Once", true),
                HomeSessionItemUi(2, "Math Study", "10:30 - 11:30", "60 min", "Weekly", false)
            ),
            weeklyFocusText = "4h 20m"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreviewLight() {
    MONKeyTheme(darkTheme = false) {
        HomeScreenContent(
            sessions = listOf(
                HomeSessionItemUi(1, "CSCI lecture", "09:00 - 10:00", "120 min", "ONCE", true),
                HomeSessionItemUi(2, "Math Study", "10:30 - 11:30", "60 min", "WEEKLY", false)
            ),
            weeklyFocusText = "4h 20m"
        )
    }
}
