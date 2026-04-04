package com.monkey.focus_app.ui.session

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.monkey.focus_app.data.AppRepository
import com.monkey.focus_app.data.db.DatabaseBuilder
import com.monkey.focus_app.ui.theme.MONKeyTheme
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import com.monkey.focus_app.ui.home.HomeEffect
import com.monkey.focus_app.ui.navigation.MainRoute
import com.monkey.focus_app.ui.navigation.navigateToTopLevel
import kotlinx.coroutines.launch
import kotlin.text.isDigit

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SessionEditScreen(
    navController: NavController,
    sessionId: String,
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
    val appContext = context.applicationContext
    val factory = remember(repository, sessionId) {
        SessionEditViewModelFactory(repository, sessionId, appContext)
    }
    val sessionEditViewModel: SessionEditViewModel = viewModel(factory = factory)
    val uiState by sessionEditViewModel.uiState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        sessionEditViewModel.effect.collect { effect ->
            when (effect) {
                SessionEditEffect.SaveSuccess -> navController.popBackStack()
                SessionEditEffect.NavigateToTagEdit -> {
                    navController.navigate(MainRoute.FocusTagEdit.route)
                }
                is SessionEditEffect.ShowMessage -> {
                    scope.launch { snackbarHostState.showSnackbar(effect.text) }
                }
                is SessionEditEffect.NavigateToCalendar -> {
                    navController.navigate(MainRoute.SessionCalendar.create(sessionId))
                }
            }
        }
    }

    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val calendarTitle = savedStateHandle?.get<String>("calendar_title")
    val calendarStartMillis = savedStateHandle?.get<Long>("calendar_start_millis")
    val calendarEndMillis = savedStateHandle?.get<Long>("calendar_end_millis")

    LaunchedEffect(calendarTitle, calendarStartMillis, calendarEndMillis) {
        if (calendarTitle != null && calendarStartMillis != null && calendarEndMillis != null) {
            savedStateHandle.remove<String>("calendar_title")
            savedStateHandle.remove<Long>("calendar_start_millis")
            savedStateHandle.remove<Long>("calendar_end_millis")
            sessionEditViewModel.onCalendarEventImported(
                title = calendarTitle,
                startTimeMillis = calendarStartMillis,
                endTimeMillis = calendarEndMillis
            )
        }
    }

    SessionEditContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onBackClick = { navController.popBackStack() },
        onSaveClick = sessionEditViewModel::onSaveClicked,
        onTitleChanged = sessionEditViewModel::onTitleChanged,
        onToggleTag = sessionEditViewModel::onToggleTag,
        onDateChanged = sessionEditViewModel::onDateChanged,
        onTimeChanged = sessionEditViewModel::onTimeChanged,
        onDurationChanged = sessionEditViewModel::onDurationChanged,
        onRecurrenceChanged = sessionEditViewModel::onRecurrenceChanged,
        onUnlockLevelChanged = sessionEditViewModel::onUnlockLevelChanged,
        onReminderIndexChanged = sessionEditViewModel::onReminderIndexChanged,
        onEmptyTagClicked = sessionEditViewModel::onEmptyTagClicked,
        onImportFromCalendarClicked = sessionEditViewModel::onImportFromCalendarClicked,
    )
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SessionEditContent(
    uiState: SessionEditUiState,
    snackbarHostState: SnackbarHostState,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
    onTitleChanged: (String) -> Unit,
    onToggleTag: (Int) -> Unit,
    onDateChanged: (Long?) -> Unit,
    onTimeChanged: (Int, Int) -> Unit,
    onDurationChanged: (Int) -> Unit,
    onRecurrenceChanged: (String) -> Unit,
    onUnlockLevelChanged: (String) -> Unit,
    onReminderIndexChanged: (Float) -> Unit,
    onEmptyTagClicked: () -> Unit,
    onImportFromCalendarClicked:() -> Unit,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH) }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH) }
    val zoneId = remember { ZoneId.systemDefault() }

    val dateText = Instant.ofEpochMilli(uiState.selectedDateMillis)
        .atZone(zoneId)
        .toLocalDate()
        .format(dateFormatter)

    val startTime = LocalTime.of(uiState.selectedHour, uiState.selectedMinute)
    val endTime = startTime.plusMinutes(uiState.durationMinutes.toLong())
    val timeText = "${startTime.format(timeFormatter)} - ${endTime.format(timeFormatter)}"

    val recurrenceOptions = listOf("ONCE", "DAILY", "WEEKLY")
    val levelOptions = listOf("NOVICE", "BHIKKHU")
//    val levelOptions = listOf("NOVICE", "BHIKKHU", "ABBOT")

    var durationInput by rememberSaveable(uiState.sessionId, uiState.durationMinutes) {
        mutableStateOf(uiState.durationMinutes.toString())
    }

    fun commitDurationInput() {
        val parsed = durationInput.toIntOrNull()
        if (parsed == null) {
            durationInput = uiState.durationMinutes.toString()
            return
        }
        val clamped = parsed.coerceIn(5, 240)
        onDurationChanged(clamped)
        durationInput = clamped.toString()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = if (uiState.isCreateMode) "Create Session" else "Edit Session",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                FilledTonalButton(
                    onClick = {
                        commitDurationInput()
                        onSaveClick()
                    },
                    enabled = !uiState.isSaving,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    )
                ) {
                    Text(if (uiState.isSaving) "Saving..." else "Save")
                }
            }

            OutlinedButton(
                onClick = { onImportFromCalendarClicked() },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                contentPadding = PaddingValues(vertical = 12.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Date",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Import from Calendar")
                }
            }

            OutlinedTextField(
                value = uiState.title,
                onValueChange = onTitleChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Session title") },
                shape = MaterialTheme.shapes.medium,
                singleLine = true,
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Tags",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                )

                if (uiState.availableTags.isEmpty()) {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        onClick = onEmptyTagClicked
                    ) {
                        Text(
                            text = "No tags yet. Create tags first in Tags screen.",
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        uiState.availableTags.forEach { tag ->
                            val selected = uiState.selectedTagIds.contains(tag.id)
                            val tagColor = try {
                                Color(tag.colorHex.toColorInt())
                            } catch (_: IllegalArgumentException) {
                                MaterialTheme.colorScheme.primary
                            }
                            FilterChip(
                                selected = selected,
                                onClick = { onToggleTag(tag.id) },
                                label = { Text("#${tag.name}") },
                                border = if (selected) BorderStroke(2.dp, tagColor) else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = tagColor.copy(alpha = 0.18f),
                                    selectedLabelColor = tagColor,
                                    containerColor = tagColor.copy(alpha = 0.08f),
                                    labelColor = tagColor
                                )
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedCard(
                    onClick = { showDatePicker = true },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 76.dp),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Date", style = MaterialTheme.typography.labelMedium)
                        Text(
                            text = dateText,
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                OutlinedCard(
                    onClick = { showTimePicker = true },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 76.dp),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Time", style = MaterialTheme.typography.labelMedium)
                        Text(
                            text = timeText,
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Duration (minutes)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,

                ) {
                    IconButton(
                        onClick = {
                            val base = durationInput.toIntOrNull() ?: uiState.durationMinutes
                            val updated = (base - 5).coerceIn(5, 240)
                            onDurationChanged(updated)
                            durationInput = updated.toString()
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.primary,
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Decrease duration",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                    OutlinedTextField(
                        value = durationInput,
                        onValueChange = { input: String ->
                            if (input.isEmpty()) {
                                durationInput = ""
                                return@OutlinedTextField
                            }
                            val digitsOnly = input.filter { it.isDigit() }
                            if (digitsOnly.length <= 3) {
                                durationInput = digitsOnly
                            }
                        },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium,
                        interactionSource = remember { MutableInteractionSource() }.also { interactionSource ->
                            val isFocused by interactionSource.collectIsFocusedAsState()
                            LaunchedEffect(isFocused) {
                                if (!isFocused) {
                                    commitDurationInput()
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { commitDurationInput() }),
                        textStyle = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        ),
                    )
                    IconButton(
                        onClick = {
                            val base = durationInput.toIntOrNull() ?: uiState.durationMinutes
                            val updated = (base + 5).coerceIn(5, 240)
                            onDurationChanged(updated)
                            durationInput = updated.toString()
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.primary,
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Increase duration",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Recurrence",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    recurrenceOptions.forEach { option ->
                        val selected = uiState.selectedRecurrence == option
                        val border = if (selected) {
                            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                        } else {
                            BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
                        }
                        OutlinedCard(
                            onClick = { onRecurrenceChanged(option) },
                            modifier = Modifier.weight(1f),
                            shape = MaterialTheme.shapes.medium,
                            border = border,
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = option,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Unlock Level",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    levelOptions.forEach { level ->
                        val selected = uiState.selectedUnlockLevel == level
                        val border = if (selected) {
                            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                        } else {
                            BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
                        }
                        OutlinedCard(
                            onClick = { onUnlockLevelChanged(level) },
                            modifier = Modifier.weight(1f),
                            shape = MaterialTheme.shapes.medium,
                            border = border,
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = level,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Reminder",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Slider(
                    value = uiState.reminderIndex,
                    onValueChange = onReminderIndexChanged,
                    valueRange = 0f..3f,
                    steps = 2,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                        activeTickColor = MaterialTheme.colorScheme.onPrimary,
                        inactiveTickColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("NONE", style = MaterialTheme.typography.labelSmall)
                    Text("15M", style = MaterialTheme.typography.labelSmall)
                    Text("30M", style = MaterialTheme.typography.labelSmall)
                    Text("1H", style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .padding(bottom = 88.dp)
        )
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = uiState.selectedDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDateChanged(datePickerState.selectedDateMillis)
                        showDatePicker = false
                    }
                ) {
                    Text("OK", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = uiState.selectedHour,
            initialMinute = uiState.selectedMinute,
            is24Hour = true,
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onTimeChanged(timePickerState.hour, timePickerState.minute)
                        showTimePicker = false
                    }
                ) {
                    Text("OK", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Preview(showBackground = true)
@Composable
private fun SessionEditContentPreviewDark() {
    MONKeyTheme(darkTheme = true) {
        SessionEditContent(
            uiState = SessionEditUiState(
                isCreateMode = true,
                title = "Deep Work",
                availableTags = listOf(),
                selectedTagIds = setOf(1, 2),
                durationMinutes = 45,
                reminderIndex = 2f
            ),
            snackbarHostState = SnackbarHostState(),
            onBackClick = {},
            onSaveClick = {},
            onTitleChanged = {},
            onToggleTag = {},
            onDateChanged = {},
            onTimeChanged = { _, _ -> },
            onDurationChanged = {},
            onRecurrenceChanged = {},
            onUnlockLevelChanged = {},
            onReminderIndexChanged = {},
            onEmptyTagClicked = {},
            onImportFromCalendarClicked = {}
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Preview(showBackground = true)
@Composable
private fun SessionEditContentPreviewLight() {
    MONKeyTheme(darkTheme = false) {
        SessionEditContent(
            uiState = SessionEditUiState(
                isCreateMode = false,
                sessionId = 1,
                title = "Morning Routine",
                availableTags = listOf(
                    SessionTagOptionUi(1, "Work", "#FE9F4C"),
                    SessionTagOptionUi(2, "Study", "#4FB2F8")
                ),
                selectedTagIds = setOf(2),
                durationMinutes = 30,
                reminderIndex = 1f
            ),
            snackbarHostState = SnackbarHostState(),
            onBackClick = {},
            onSaveClick = {},
            onTitleChanged = {},
            onToggleTag = {},
            onDateChanged = {},
            onTimeChanged = { _, _ -> },
            onDurationChanged = {},
            onRecurrenceChanged = {},
            onUnlockLevelChanged = {},
            onReminderIndexChanged = {},
            onEmptyTagClicked = {},
            onImportFromCalendarClicked = {}
        )
    }
}
