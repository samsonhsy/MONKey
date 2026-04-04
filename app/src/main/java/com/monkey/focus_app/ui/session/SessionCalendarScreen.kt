package com.monkey.focus_app.ui.session

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionCalendarScreen(
    navController: NavController,
    sessionId: String
) {
    val context = LocalContext.current
    val appContext = context.applicationContext
    val factory = remember(appContext) {
        SessionCalendarViewModelFactory(appContext)
    }
    val viewModel: SessionCalendarViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.onPermissionGranted()
        } else {
            viewModel.onPermissionDenied()
        }
    }

    LaunchedEffect(Unit) {
        if (!uiState.hasPermission) {
            permissionLauncher.launch(Manifest.permission.READ_CALENDAR)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is CalendarEffect.ReturnToSessionEdit -> {
                    navController.previousBackStackEntry?.savedStateHandle?.apply {
                        set("calendar_title", effect.title)
                        set("calendar_start_millis", effect.startTimeMillis)
                        set("calendar_end_millis", effect.endTimeMillis)
                    }
                    navController.popBackStack()
                }
                is CalendarEffect.ShowMessage -> {
                    scope.launch { snackbarHostState.showSnackbar(effect.text) }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Select Calendar Event",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::onRefresh) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            FilledTonalButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp, 0.dp, 10.dp, 10.dp),
                onClick = viewModel::onConfirmSelection,
                enabled = uiState.selectedEventId != null,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Import")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        SessionCalendarContent(
            modifier = Modifier.padding(innerPadding),
            uiState = uiState,
            onEventToggled = viewModel::onEventToggled,
            onRequestPermission = { permissionLauncher.launch(Manifest.permission.READ_CALENDAR) }
        )
    }
}

@Composable
fun SessionCalendarContent(
    uiState: SessionCalendarUiState,
    onEventToggled: (Long) -> Unit,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            !uiState.hasPermission -> {
                PermissionRequiredContent(
                    onRequestPermission = onRequestPermission,
                    errorMessage = uiState.errorMessage
                )
            }

            uiState.events.isEmpty() -> {
                EmptyEventsContent()
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.events, key = { it.id }) { event ->
                        val isSelected = uiState.selectedEventId == event.id
                        val eventDate = Instant.ofEpochMilli(event.startTimeMillis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()

                        EventCard(
                            event = event,
                            isSelected = isSelected,
                            onToggle = { onEventToggled(event.id) },
                            dateFormatter = dateFormatter,
                            eventDate = eventDate
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EventCard(
    event: CalendarEventUi,
    isSelected: Boolean,
    onToggle: () -> Unit,
    dateFormatter: DateTimeFormatter,
    eventDate: LocalDate
) {
    val borderStroke: BorderStroke = if (isSelected) {
        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    } else {
        BorderStroke(0.dp, MaterialTheme.colorScheme.primary)
    }

    OutlinedCard(
        onClick = onToggle,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = borderStroke,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        tint = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = eventDate.format(dateFormatter),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                        )
                        Text(
                            text = event.formattedTime,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                            }
                        )
                    }
                }
            }

            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() }
            )
        }
    }
}

@Composable
private fun PermissionRequiredContent(
    onRequestPermission: () -> Unit,
    errorMessage: String?
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Calendar Permission Required",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = errorMessage ?: "To import calendar events, please grant calendar access permission.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            FilledTonalButton(
                onClick = onRequestPermission,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Grant Permission")
            }
        }
    }
}

@Composable
private fun EmptyEventsContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Event,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )

            Text(
                text = "No Upcoming Events",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "You don't have any calendar events in the next 3 months.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview
@Composable
fun SessionCalendarScreenPreview(){
    SessionCalendarScreen(
        navController = rememberNavController(),
        sessionId = "1"
    )
}
