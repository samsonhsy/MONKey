package com.monkey.focus_app.ui.focus_tag

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.monkey.focus_app.data.AppRepository
import com.monkey.focus_app.data.db.DatabaseBuilder
import com.monkey.focus_app.ui.navigation.MainRoute
import com.monkey.focus_app.ui.theme.MONKeyTheme
import kotlinx.coroutines.launch

@Composable
fun FocusTagScreen(navController: NavController) {
    val context = androidx.compose.ui.platform.LocalContext.current
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

    val factory = remember(repository) { TagListViewModelFactory(repository) }
    val tagListViewModel: TagListViewModel = viewModel(factory = factory)
    val uiState by tagListViewModel.uiState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        tagListViewModel.effect.collect { effect ->
            when (effect) {
                TagListEffect.NavigateToCreate -> {
                    navController.navigate(MainRoute.FocusTagEdit.create("new"))
                }

                is TagListEffect.NavigateToEdit -> {
                    navController.navigate(MainRoute.FocusTagEdit.create(effect.id.toString()))
                }

                is TagListEffect.ShowMessage -> {
                    scope.launch { snackbarHostState.showSnackbar(effect.text) }
                }
            }
        }
    }

    FocusTagContent(
        tags = uiState.tags,
        isDeleteMode = uiState.isDeleteMode,
        pendingDeleteTagId = uiState.pendingDeleteTagId,
        onToggleDeleteMode = tagListViewModel::onToggleDeleteMode,
        onRequestDelete = tagListViewModel::onRequestDelete,
        onDismissDeleteDialog = tagListViewModel::onDismissDeleteDialog,
        onConfirmDelete = tagListViewModel::onConfirmDelete,
        onEditClick = tagListViewModel::onEditClicked,
        onAddClick = tagListViewModel::onAddClicked,
        snackbarHostState = snackbarHostState,
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
private fun FocusTagContent(
    tags: List<TagListItemUi>,
    isDeleteMode: Boolean,
    pendingDeleteTagId: Int?,
    onToggleDeleteMode: () -> Unit,
    onRequestDelete: (Int) -> Unit,
    onDismissDeleteDialog: () -> Unit,
    onConfirmDelete: () -> Unit,
    onEditClick: (Int) -> Unit,
    onAddClick: () -> Unit,
    snackbarHostState: SnackbarHostState,
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
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create Focus Tag",
                    modifier = Modifier.size(32.dp),
                )
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Focus Tags",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    IconButton(
                        onClick = onToggleDeleteMode,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isDeleteMode) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.errorContainer
                            )
                            .size(40.dp)
                    ) {
                        Icon(
                            imageVector = if (isDeleteMode) Icons.Default.Edit else Icons.Default.Delete,
                            contentDescription = if (isDeleteMode) "Switch to edit mode" else "Switch to delete mode",
                            tint = if (isDeleteMode) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            items(tags) { tag ->
                FocusTagCard(
                    item = tag,
                    isDeleteMode = isDeleteMode,
                    onEditClick = { onEditClick(tag.id) },
                    onRequestDelete = { onRequestDelete(tag.id) },
                )
            }
        }
    }

    if (pendingDeleteTagId != null) {
        AlertDialog(
            onDismissRequest = onDismissDeleteDialog,
            title = { Text("Delete Tag") },
            text = { Text("Confirm to delete this tag?") },
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
private fun FocusTagCard(
    item: TagListItemUi,
    isDeleteMode: Boolean,
    onEditClick: () -> Unit,
    onRequestDelete: () -> Unit,
) {
    val stripColor = try {
        Color(android.graphics.Color.parseColor(item.colorHex))
    } catch (_: IllegalArgumentException) {
        MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .size(width = 8.dp, height = 100.dp)
                    .background(stripColor),
            )

            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = item.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    IconButton(
                        onClick = if (isDeleteMode) onRequestDelete else onEditClick,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (isDeleteMode) MaterialTheme.colorScheme.errorContainer
                                else MaterialTheme.colorScheme.primary
                            )
                            .size(40.dp)
                    ) {
                        Icon(
                            imageVector = if (isDeleteMode) Icons.Default.Delete else Icons.Default.Edit,
                            contentDescription = if (isDeleteMode) "Delete ${item.title}" else "Edit ${item.title}",
                            tint = if (isDeleteMode) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)

                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(999.dp),
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = "${item.appCount} apps",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FocusTagPreviewDark() {
    MONKeyTheme(darkTheme = true) {
        FocusTagContent(
            tags = listOf(
                TagListItemUi(1, "Work", "Work apps", "#FE9F4C", 7),
                TagListItemUi(2, "Study", "Study apps", "#4FB2F8", 5),
            ),
            isDeleteMode = false,
            pendingDeleteTagId = null,
            onToggleDeleteMode = {},
            onRequestDelete = {},
            onDismissDeleteDialog = {},
            onConfirmDelete = {},
            onEditClick = { },
            onAddClick = { },
            snackbarHostState = SnackbarHostState(),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FocusTagPreviewLight() {
    MONKeyTheme(darkTheme = false) {
        FocusTagContent(
            tags = listOf(
                TagListItemUi(1, "Work", "Work apps", "#FE9F4C", 7),
                TagListItemUi(2, "Study", "Study apps", "#4FB2F8", 5),
            ),
            isDeleteMode = true,
            pendingDeleteTagId = null,
            onToggleDeleteMode = {},
            onRequestDelete = {},
            onDismissDeleteDialog = {},
            onConfirmDelete = {},
            onEditClick = { },
            onAddClick = { },
            snackbarHostState = SnackbarHostState(),
        )
    }
}
