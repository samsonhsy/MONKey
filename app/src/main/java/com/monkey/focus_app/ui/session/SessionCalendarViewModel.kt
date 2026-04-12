package com.monkey.focus_app.ui.session

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class CalendarEventUi(
    val id: Long,
    val title: String,
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val isAllDay: Boolean = false,
    val formattedTime: String = ""
)

data class SessionCalendarUiState(
    val isLoading: Boolean = true,
    val events: List<CalendarEventUi> = emptyList(),
    val selectedEventId: Long? = null,
    val hasPermission: Boolean = false,
    val errorMessage: String? = null
)

sealed interface CalendarEffect {
    data class ReturnToSessionEdit(
        val title: String,
        val startTimeMillis: Long,
        val endTimeMillis: Long
    ) : CalendarEffect
    data class ShowMessage(val text: String) : CalendarEffect
}

class SessionCalendarViewModel(
    private val appContext: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SessionCalendarUiState())
    val uiState: StateFlow<SessionCalendarUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<CalendarEffect>()
    val effect: SharedFlow<CalendarEffect> = _effect.asSharedFlow()

    private val contentResolver: ContentResolver = appContext.contentResolver
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    init {
        checkPermissionAndLoadEvents()
    }

    fun checkPermissionAndLoadEvents() {
        val hasPermission = ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED

        _uiState.value = _uiState.value.copy(hasPermission = hasPermission)

        if (hasPermission) {
            loadCalendarEvents()
        } else {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                events = emptyList()
            )
        }
    }

    fun onPermissionGranted() {
        _uiState.value = _uiState.value.copy(hasPermission = true)
        loadCalendarEvents()
    }

    fun onPermissionDenied() {
        _uiState.value = _uiState.value.copy(
            hasPermission = false,
            isLoading = false,
            errorMessage = "Calendar permission is required to import events"
        )
    }

    private fun loadCalendarEvents() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val events = withContext(Dispatchers.IO) {
                    fetchCalendarEvents()
                }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    events = events,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load calendar events"
                )
            }
        }
    }

    private fun fetchCalendarEvents(): List<CalendarEventUi> {
        val now = System.currentTimeMillis()
        val endTime = LocalDate.now()
            .plusMonths(3)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val projection = arrayOf(
            CalendarContract.Instances._ID,
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END,
            CalendarContract.Instances.ALL_DAY
        )

        val uri = CalendarContract.Instances.CONTENT_URI
            .buildUpon()
            .appendPath(now.toString())
            .appendPath(endTime.toString())
            .build()

        val sortOrder = "${CalendarContract.Instances.BEGIN} ASC"

        val events = mutableListOf<CalendarEventUi>()

        contentResolver.query(
            uri,
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->
            val idIndex = cursor.getColumnIndex(CalendarContract.Instances._ID)
            val titleIndex = cursor.getColumnIndex(CalendarContract.Instances.TITLE)
            val startIndex = cursor.getColumnIndex(CalendarContract.Instances.BEGIN)
            val endIndex = cursor.getColumnIndex(CalendarContract.Instances.END)
            val allDayIndex = cursor.getColumnIndex(CalendarContract.Instances.ALL_DAY)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idIndex)
                val title = cursor.getString(titleIndex) ?: "Untitled Event"
                val startMillis = cursor.getLong(startIndex)
                var endMillis = cursor.getLong(endIndex)

                if (endMillis == 0L) {
                    endMillis = startMillis + 30 * 60 * 1000L
                }

                val isAllDay = cursor.getInt(allDayIndex) == 1

                val formattedTime = if (isAllDay) {
                    "All Day"
                } else {
                    val startDateTime = Instant.ofEpochMilli(startMillis)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime()
                    val endDateTime = Instant.ofEpochMilli(endMillis)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime()

                    "${startDateTime.format(timeFormatter)} - ${endDateTime.format(timeFormatter)}"
                }

                events.add(
                    CalendarEventUi(
                        id = id,
                        title = title,
                        startTimeMillis = startMillis,
                        endTimeMillis = endMillis,
                        isAllDay = isAllDay,
                        formattedTime = formattedTime
                    )
                )
            }
        }

        return events
    }

    fun onEventToggled(eventId: Long) {
        val currentSelected = _uiState.value.selectedEventId
        val updated = if (currentSelected == eventId) null else eventId
        _uiState.value = _uiState.value.copy(selectedEventId = updated)
    }

    fun onConfirmSelection() {
        val state = _uiState.value
        val selectedId = state.selectedEventId

        if (selectedId == null) {
            viewModelScope.launch {
                _effect.emit(CalendarEffect.ShowMessage("Please select an event"))
            }
            return
        }

        val selectedEvent = state.events.find { it.id == selectedId }

        if (selectedEvent == null) {
            viewModelScope.launch {
                _effect.emit(CalendarEffect.ShowMessage("Event not found"))
            }
            return
        }

        viewModelScope.launch {
            _effect.emit(
                CalendarEffect.ReturnToSessionEdit(
                    title = selectedEvent.title,
                    startTimeMillis = selectedEvent.startTimeMillis,
                    endTimeMillis = selectedEvent.endTimeMillis
                )
            )
        }
    }

    fun onRefresh() {
        if (_uiState.value.hasPermission) {
            loadCalendarEvents()
        }
    }
}
