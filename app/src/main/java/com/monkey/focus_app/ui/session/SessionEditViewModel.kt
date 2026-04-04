package com.monkey.focus_app.ui.session

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monkey.focus_app.data.AppRepository
import com.monkey.focus_app.data.db.entity.Session
import com.monkey.focus_app.service.scheduler.AlarmScheduler
import com.monkey.focus_app.ui.home.HomeEffect
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

data class SessionTagOptionUi(
    val id: Int,
    val name: String,
    val colorHex: String,
)

data class SessionEditUiState(
    val isLoading: Boolean = true,
    val isCreateMode: Boolean = true,
    val sessionId: Int? = null,
    val title: String = "",
    val selectedTagIds: Set<Int> = emptySet(),
    val availableTags: List<SessionTagOptionUi> = emptyList(),
    val selectedDateMillis: Long = System.currentTimeMillis(),
    val selectedHour: Int = 9,
    val selectedMinute: Int = 0,
    val durationMinutes: Int = 30,
    val selectedRecurrence: String = "ONCE",
    val selectedUnlockLevel: String = "NOVICE",
    val reminderIndex: Float = 1f,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

sealed interface SessionEditEffect {
    data object SaveSuccess : SessionEditEffect
    data object NavigateToTagEdit : SessionEditEffect
    data class ShowMessage(val text: String) : SessionEditEffect
}

class SessionEditViewModel(
    private val repository: AppRepository,
    private val sessionIdArg: String,
    appContext: Context
) : ViewModel() {
    private  val alarmScheduler = AlarmScheduler(appContext)
    private val _uiState = MutableStateFlow(SessionEditUiState())
    val uiState: StateFlow<SessionEditUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<SessionEditEffect>()
    val effect: SharedFlow<SessionEditEffect> = _effect.asSharedFlow()

    private val isCreateMode = sessionIdArg == "new"

    init {
        _uiState.value = _uiState.value.copy(isCreateMode = isCreateMode)
        observeTags()
        if (isCreateMode) {
            _uiState.value = _uiState.value.copy(isLoading = false)
        } else {
            loadExistingSession()
        }
    }

    private fun observeTags() {
        viewModelScope.launch {
            repository.getAllTag()
                .catch { throwable ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = throwable.message ?: "Failed to load tags"
                    )
                }
                .collect { tags ->
                    val options = tags
                        .sortedBy { it.tagName.lowercase() }
                        .map { SessionTagOptionUi(id = it.tagID, name = it.tagName, colorHex = it.colorHex) }

                    val validSelected = _uiState.value.selectedTagIds.intersect(options.map { it.id }.toSet())

                    _uiState.value = _uiState.value.copy(
                        availableTags = options,
                        selectedTagIds = validSelected
                    )
                }
        }
    }

    private fun loadExistingSession() {
        val id = sessionIdArg.toIntOrNull()
        if (id == null) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "Invalid session id"
            )
            return
        }

        viewModelScope.launch {
            val session = repository.getAllSessionById(id)
            if (session == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Session not found"
                )
                return@launch
            }

            val localDateTime = Instant.ofEpochMilli(session.startDateTime)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()

            val dateMillis = localDateTime.toLocalDate()
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isCreateMode = false,
                sessionId = session.sessionID,
                title = session.sessionName,
                selectedTagIds = session.tagIds.toSet(),
                selectedDateMillis = dateMillis,
                selectedHour = localDateTime.hour,
                selectedMinute = localDateTime.minute,
                durationMinutes = session.durationMin,
                selectedRecurrence = session.recurrence.uppercase(),
                selectedUnlockLevel = session.unlockKeyLevel.uppercase(),
                reminderIndex = reminderOffsetToIndex(session.reminderOffsetMinutes),
                errorMessage = null
            )
        }
    }

    fun onTitleChanged(value: String) {
        _uiState.value = _uiState.value.copy(title = value)
    }

    fun onToggleTag(tagId: Int) {
        val current = _uiState.value.selectedTagIds
        val updated = if (current.contains(tagId)) current - tagId else current + tagId
        _uiState.value = _uiState.value.copy(selectedTagIds = updated)
    }

    fun onEmptyTagClicked(){
        viewModelScope.launch {
            _effect.emit(SessionEditEffect.NavigateToTagEdit)
        }
    }
    fun onDateChanged(millis: Long?) {
        if (millis == null) return
        val zone = ZoneId.systemDefault()
        val todayStartMillis = LocalDate.now(zone).atStartOfDay(zone).toInstant().toEpochMilli()
        if (millis < todayStartMillis) {
            viewModelScope.launch {
                _effect.emit(SessionEditEffect.ShowMessage("Date cannot be in the past"))
            }
            return
        }
        _uiState.value = _uiState.value.copy(selectedDateMillis = millis)
    }

    fun onTimeChanged(hour: Int, minute: Int) {
        val state = _uiState.value
        val candidateStartMillis = buildStartMillis(state.selectedDateMillis, hour, minute)
        if (candidateStartMillis < System.currentTimeMillis()) {
            viewModelScope.launch {
                _effect.emit(SessionEditEffect.ShowMessage("Start time cannot be earlier than now"))
            }
            return
        }
        _uiState.value = _uiState.value.copy(selectedHour = hour, selectedMinute = minute)
    }

    fun onDurationChanged(value: Int) {
        _uiState.value = _uiState.value.copy(durationMinutes = value.coerceIn(5, 240))
    }

    fun onRecurrenceChanged(value: String) {
        _uiState.value = _uiState.value.copy(selectedRecurrence = value.uppercase())
    }

    fun onUnlockLevelChanged(value: String) {
        _uiState.value = _uiState.value.copy(selectedUnlockLevel = value.uppercase())
    }

    fun onReminderIndexChanged(value: Float) {
        _uiState.value = _uiState.value.copy(reminderIndex = value.coerceIn(0f, 3f))
    }

    fun onSaveClicked() {
        val state = _uiState.value

        if (state.title.isBlank()) {
            viewModelScope.launch { _effect.emit(SessionEditEffect.ShowMessage("Session title is required")) }
            return
        }

        if (state.selectedTagIds.isEmpty()) {
            viewModelScope.launch { _effect.emit(SessionEditEffect.ShowMessage("Please select at least one tag")) }
            return
        }

        _uiState.value = state.copy(isSaving = true)

        viewModelScope.launch {
            try {
                val startDate = Instant.ofEpochMilli(state.selectedDateMillis)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()

                val startDateTime = LocalDateTime.of(
                    startDate,
                    LocalTime.of(state.selectedHour, state.selectedMinute)
                )

                val startMillis = startDateTime
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()

                if (startMillis < System.currentTimeMillis()) {
                    _uiState.value = _uiState.value.copy(isSaving = false)
                    _effect.emit(SessionEditEffect.ShowMessage("Session start must be in the future"))
                    return@launch
                }

                val endMillis = startMillis + state.durationMinutes * 60_000L

                val session = Session(
                    sessionID = state.sessionId ?: 0,
                    sessionName = state.title.trim(),
                    startDateTime = startMillis,
                    endDateTime = endMillis,
                    durationMin = state.durationMinutes,
                    recurrence = state.selectedRecurrence,
                    tagIds = state.selectedTagIds.toList().sorted(),
                    unlockKeyLevel = state.selectedUnlockLevel,
                    reminderOffsetMinutes = reminderIndexToOffset(state.reminderIndex),
                    isActive = false,
                    unlockPhrase = null
                )

                if (state.isCreateMode) {
                    val insertedIds = repository.insertAllSession(session)
                    val newId = insertedIds.firstOrNull()?.toInt()?:throw IllegalStateException("Failed to insert session")
                    val savedSession = session.copy(sessionID = newId)
                    alarmScheduler.scheduleSession(savedSession)
                } else {
                    val existingId = state.sessionId ?: throw IllegalStateException("Missing session id for update")
                    alarmScheduler.cancelSession(existingId)
                    repository.updateAllSession(session)
                    alarmScheduler.scheduleSession(session)
                }

                _uiState.value = _uiState.value.copy(isSaving = false)
                _effect.emit(SessionEditEffect.SaveSuccess)
            } catch (throwable: Throwable) {
                _uiState.value = _uiState.value.copy(isSaving = false)
                _effect.emit(SessionEditEffect.ShowMessage(throwable.message ?: "Failed to save session"))
            }
        }
    }

    private fun reminderIndexToOffset(index: Float): String {
        return when (index.toInt()) {
            0 -> "0"
            1 -> "15"
            2 -> "30"
            else -> "60"
        }
    }

    private fun reminderOffsetToIndex(offset: String): Float {
        return when (offset.toIntOrNull() ?: 30) {
            0 -> 0f
            15 -> 1f
            30 -> 2f
            else -> 3f
        }
    }

    private fun buildStartMillis(dateMillis: Long, hour: Int, minute: Int): Long {
        val zone = ZoneId.systemDefault()
        val startDate = Instant.ofEpochMilli(dateMillis)
            .atZone(zone)
            .toLocalDate()

        val startDateTime = LocalDateTime.of(startDate, LocalTime.of(hour, minute))
        return startDateTime.atZone(zone).toInstant().toEpochMilli()
    }
}
