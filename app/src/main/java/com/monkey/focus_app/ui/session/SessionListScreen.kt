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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.monkey.focus_app.ui.navigation.MainRoute
import com.monkey.focus_app.ui.theme.MONKeyTheme
// --- Dummy Data Models ---
data class UpcomingSession(val id: Int, val title: String, val time: String, val tags: List<String>, val recurrence: String)
data class HistorySession(val id: Int, val title: String, val date: String, val duration: String, val category: String)
val dummyUpcoming = listOf(
    UpcomingSession(1, "Morning Meditation", "07:00 - 09:00 ", listOf("#meditation", "#mindful"), "Daily"),
    UpcomingSession(2, "Deep Work", "09:00 - 10:00", listOf("#work"), "Weekly")
)
val dummyHistory = listOf(
    HistorySession(1, "Reading Session", "Oct 12, 2023", "15 mins", "RELAX"),
    HistorySession(2, "Study Group", "Oct 11, 2023", "45 mins", "STUDY")
)

@Composable
fun SessionListScreen(navController: NavController) {
    // 0 = Upcoming, 1 = History
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    // Keep track of which upcoming items are selected for deletion
    var selectedUpcomingIds by remember { mutableStateOf(setOf<Int>()) }
    SessionListContent(
        selectedTabIndex = selectedTabIndex,
        onTabSelected = { selectedTabIndex = it },
        upcomingSessions = dummyUpcoming,
        historySessions = dummyHistory,
        selectedUpcomingIds = selectedUpcomingIds,
        onToggleUpcomingSelection = { id ->
            selectedUpcomingIds = if (selectedUpcomingIds.contains(id)) {
                selectedUpcomingIds - id
            } else {
                selectedUpcomingIds + id
            }
        },
        onDeleteSelected = {
            // Handle delete logic here
            selectedUpcomingIds = emptySet()
        },
        onEditClick = { id -> navController.navigate(MainRoute.SessionEdit.create(id.toString())) },
        onFabClick = { navController.navigate(MainRoute.SessionEdit.create("new")) }
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SessionListContent(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    upcomingSessions: List<UpcomingSession>,
    historySessions: List<HistorySession>,
    selectedUpcomingIds: Set<Int>,
    onToggleUpcomingSelection: (Int) -> Unit,
    onDeleteSelected: () -> Unit,
    onEditClick: (Int) -> Unit,
    onFabClick: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
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
                showDelete = selectedTabIndex == 0 && selectedUpcomingIds.isNotEmpty(),
                onDeleteClick = onDeleteSelected
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
                            isSelected = selectedUpcomingIds.contains(session.id),
                            onCheckedChange = { onToggleUpcomingSelection(session.id) },
                            onEditClick = { onEditClick(session.id) }
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
}


@Composable
private fun TopBarSection(showDelete: Boolean, onDeleteClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Sessions",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )

        if (showDelete) {
            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.errorContainer)
                .size(40.dp)) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Selected",
                    tint = MaterialTheme.colorScheme.error
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
    session: UpcomingSession,
    isSelected: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onEditClick: () -> Unit
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
            // Checkbox
            Checkbox(
                checked = isSelected,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
            )
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
                Spacer(modifier = Modifier.height(8.dp))
                // Tags Row
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    session.tags.forEach { tag ->
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = CircleShape
                        ) {
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
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
                    contentDescription = "Edit",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun HistorySessionCard(session: HistorySession) {
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
                    // Category Badge
                    Surface(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = session.category,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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

@Preview(showBackground = true)
@Composable
fun SessionListUpcomingPreviewDark() {
    MONKeyTheme(darkTheme = true) {
        SessionListContent(
            selectedTabIndex = 0,
            onTabSelected = {},
            upcomingSessions = dummyUpcoming,
            historySessions = emptyList(),
            selectedUpcomingIds = setOf(1), // Show one item selected
            onToggleUpcomingSelection = {},
            onDeleteSelected = {},
            onEditClick = {},
            onFabClick = {}
        )
    }
}
@Preview(showBackground = true)
@Composable
fun SessionListHistoryPreviewDark() {
    MONKeyTheme(darkTheme = true) {
        SessionListContent(
            selectedTabIndex = 1,
            onTabSelected = {},
            upcomingSessions = emptyList(),
            historySessions = dummyHistory,
            selectedUpcomingIds = emptySet(),
            onToggleUpcomingSelection = {},
            onDeleteSelected = {},
            onEditClick = {},
            onFabClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SessionListUpcomingPreviewLight() {
    MONKeyTheme(darkTheme = false) {
        SessionListContent(
            selectedTabIndex = 0,
            onTabSelected = {},
            upcomingSessions = dummyUpcoming,
            historySessions = emptyList(),
            selectedUpcomingIds = setOf(1), // Show one item selected
            onToggleUpcomingSelection = {},
            onDeleteSelected = {},
            onEditClick = {},
            onFabClick = {}
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
            upcomingSessions = emptyList(),
            historySessions = dummyHistory,
            selectedUpcomingIds = emptySet(),
            onToggleUpcomingSelection = {},
            onDeleteSelected = {},
            onEditClick = {},
            onFabClick = {}
        )
    }
}
