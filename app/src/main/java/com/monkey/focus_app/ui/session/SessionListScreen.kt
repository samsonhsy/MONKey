package com.monkey.focus_app.ui.session
import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.monkey.focus_app.data.AppRepository
import com.monkey.focus_app.data.db.DatabaseBuilder
import com.monkey.focus_app.ui.navigation.MainRoute
import com.monkey.focus_app.ui.theme.MONKeyTheme
import androidx.core.graphics.toColorInt


@Composable
fun SessionListScreen(navController: NavController) {
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
    val factory = remember(repository, appContext) { SessionListViewModelFactory(repository, appContext) }
    val sessionListViewModel: SessionListViewModel = viewModel(factory = factory)
    val uiState by sessionListViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        sessionListViewModel.effect.collect { effect ->
            when (effect) {
                SessionListEffect.NavigateToCreate -> {
                    navController.navigate(MainRoute.SessionEdit.create("new"))
                }

                is SessionListEffect.NavigateToEdit -> {
                    navController.navigate(MainRoute.SessionEdit.create(effect.id.toString()))
                }

                is SessionListEffect.ShowMessage -> {
                    snackbarHostState.showSnackbar(effect.text)
                }
            }
        }
    }

    SessionListContent(
        selectedTabIndex = if (uiState.selectedTab == SessionTab.UPCOMING) 0 else 1 ,
        onTabSelected = sessionListViewModel :: onTabSelected,
        upcomingSessions = uiState.upcoming,
        historySessions = uiState.history,
        isDeleteMode = uiState.isDeleteMode,
        pendingDeleteSessionId = uiState.pendingDeleteSessionId,
        onToggleDeleteMode = sessionListViewModel :: onToggleDeleteMode,
        onRequestDelete = sessionListViewModel :: onRequestDelete,
        onDismissDeleteDialog = sessionListViewModel :: onDismissDeleteDialog,
        onConfirmDelete = sessionListViewModel :: onConfirmDelete,
        onEditClick = sessionListViewModel :: onEditClicked,
        onFabClick = sessionListViewModel :: onAddClicked,
        snackbarHostState = snackbarHostState
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SessionListContent(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    upcomingSessions: List<UpcomingSessionUi>,
    historySessions: List<HistorySessionUi>,
    isDeleteMode: Boolean,
    pendingDeleteSessionId: Int?,
    onToggleDeleteMode: () -> Unit,
    onRequestDelete: (Int) -> Unit,
    onDismissDeleteDialog: () -> Unit,
    onConfirmDelete: () -> Unit,
    onEditClick: (Int) -> Unit,
    onFabClick: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = 88.dp)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onFabClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp) // Squircle shape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Session")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // --- Header ---
            TopBarSection(
                isDeleteMode = isDeleteMode,
                onToggleDeleteMode = onToggleDeleteMode
            )
            // --- Custom Tab Row ---
            SessionTabRow(
                selectedTabIndex = selectedTabIndex,
                onTabSelected = onTabSelected
            )
            // --- Content List ---
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp), // Space for FAB/BottomNav
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (selectedTabIndex == 0) {
                    items(upcomingSessions) { session ->
                        UpcomingSessionCard(
                            session = session,
                            isDeleteMode = isDeleteMode,
                            onEditClick = { onEditClick(session.id) },
                            onRequestDelete = {onRequestDelete(session.id)}
                        )
                    }
                } else {
                    items(historySessions) { session ->
                        HistorySessionCard(session = session)
                    }
                }
            }
        }
    }
    if (pendingDeleteSessionId != null) {
        AlertDialog(
            onDismissRequest = onDismissDeleteDialog,
            title = { Text("Delete Session") },
            text = { Text("Confirm to delete this session?") },
            confirmButton = {
                TextButton(onClick = onConfirmDelete) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDeleteDialog) {
                    Text("Cancel")
                }
            }
        )
    }
}


@Composable
private fun TopBarSection(isDeleteMode: Boolean, onToggleDeleteMode: () -> Unit) {
    Spacer(modifier = Modifier.height(24.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Sessions",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )

        if (!isDeleteMode) {
            IconButton(
                onClick = onToggleDeleteMode,
                modifier = Modifier.clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.errorContainer)
                .size(40.dp)) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Selected",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }else{
            IconButton(
                onClick = onToggleDeleteMode,
                modifier = Modifier.clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .size(40.dp)) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Delete Selected",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}
@Composable
private fun SessionTabRow(selectedTabIndex: Int, onTabSelected: (Int) -> Unit) {
    val tabs = listOf("Upcoming", "History")

    SecondaryTabRow(
        selectedTabIndex = selectedTabIndex,
        containerColor = Color.Transparent,
        tabs = {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { onTabSelected(index) },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTabIndex == index) MaterialTheme.colorScheme.onBackground
                            else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                )
            }
        }
    )

}

@Composable
private fun UpcomingSessionCard(
    session: UpcomingSessionUi,
    isDeleteMode: Boolean,
    onEditClick: () -> Unit,
    onRequestDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Center Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = session.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    // Recurrence Badge
                    Surface(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = session.recurrence,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ){
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = "Time",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = session.time,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DateRange, // Calendar Icon
                            contentDescription = "Date",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = session.date,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                // Tags Row
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    session.tags.forEachIndexed { index, tag ->
                        val hex = session.tagColors.getOrNull(index)
                        val tagColor = parseHexColorOrNull(hex) ?: MaterialTheme.colorScheme.primary

                        Surface(
                            color = tagColor.copy(alpha = 0.08f),
                            shape = CircleShape
                        ) {
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.labelSmall,
                                color = tagColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
            if(!isDeleteMode){
                // Edit Button
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit ${session.title}",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }else {
                // Delete Button
                IconButton(
                    onClick = onRequestDelete,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete ${session.title}",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

        }
    }
}

@SuppressLint("UseKtx")
private fun parseHexColorOrNull(hex: String?): Color? {
    if (hex.isNullOrBlank()) return null
    return runCatching {
        Color(hex.toColorInt())
    }.getOrNull()
}

@Composable
private fun HistorySessionCard(session: HistorySessionUi) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail Image Placeholder
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Done,
                    contentDescription = "Done",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
            // Center Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = session.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DateRange, // Calendar Icon
                            contentDescription = "Date",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = session.date,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                    // Duration pill
                    Surface(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.05f),
                        shape = CircleShape
                    ) {
                        Text(
                            text = session.duration,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}



private val previewUpcoming = listOf(
    UpcomingSessionUi(
        id = 1,
        title = "Morning Meditation",
        time = "07:00 - 09:00",
        tags = listOf("#meditation", "#mindful"),
        tagColors = listOf("#5DD39E", "#38C8C2"),
        recurrence = "Daily",
        date = "Oct 12, 2023",
    ),
    UpcomingSessionUi(
        id = 2,
        title = "Deep Work",
        time = "09:00 - 10:00",
        tags = listOf("#work"),
        tagColors = listOf("#FE9F4C"),
        recurrence = "Weekly",
        date = "Oct 12, 2023",
    )
)
private val previewHistory = listOf(
    HistorySessionUi(
        id = 10,
        title = "Reading Session",
        date = "Oct 12, 2023",
        duration = "15 mins",
    ),
    HistorySessionUi(
        id = 11,
        title = "Study Group",
        date = "Oct 11, 2023",
        duration = "45 mins",
    )
)


@Preview(showBackground = true)
@Composable
fun SessionListUpcomingPreviewDark() {
    MONKeyTheme(darkTheme = true) {
        SessionListContent(
            selectedTabIndex = 0,
            onTabSelected = {},
            upcomingSessions = previewUpcoming,
            historySessions = previewHistory,
            isDeleteMode = true,
            pendingDeleteSessionId = null,
            onToggleDeleteMode = {},
            onRequestDelete = {},
            onDismissDeleteDialog = {},
            onConfirmDelete = {},
            onEditClick = {},
            onFabClick = {},
            snackbarHostState = SnackbarHostState()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SessionListHistoryPreviewLight() {
    MONKeyTheme(darkTheme = false) {
        SessionListContent(
            selectedTabIndex = 1,
            onTabSelected = {},
            upcomingSessions = previewUpcoming,
            historySessions = previewHistory,
            isDeleteMode = false,
            pendingDeleteSessionId = null,
            onToggleDeleteMode = {},
            onRequestDelete = {},
            onDismissDeleteDialog = {},
            onConfirmDelete = {},
            onEditClick = {},
            onFabClick = {},
            snackbarHostState = SnackbarHostState()
        )
    }
}
