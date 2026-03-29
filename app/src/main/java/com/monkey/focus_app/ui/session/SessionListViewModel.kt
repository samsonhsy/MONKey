package com.monkey.focus_app.ui.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monkey.focus_app.data.AppRepository
import com.monkey.focus_app.data.db.entity.Session
import com.monkey.focus_app.data.db.entity.Tag
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.Boolean

enum class SessionTab { UPCOMING, HISTORY }
data class UpcomingSessionUi(
    val id: Int,
    val title: String,
    val time: String,
    val tags: List<String>,
    val recurrence: String
)
data class HistorySessionUi(
    val id: Int,
    val title: String,
    val date: String,
    val duration: String
)

data class SessionListUiState(
    val isLoading: Boolean = true,
    val selectedTab: SessionTab = SessionTab.UPCOMING,
    val upcoming: List<UpcomingSessionUi> = emptyList(),
    val history: List<HistorySessionUi> = emptyList(),
    val isDeleteMode: Boolean = false,
    val pendingDeleteSessionId: Int? = null,
    val errorMessage: String? = null
)

sealed interface SessionListEffect {
    data object NavigateToCreate : SessionListEffect
    data class NavigateToEdit(val id: Int) : SessionListEffect
    data class ShowMessage(val text: String) : SessionListEffect
}

class SessionListViewModel (
    private val repository: AppRepository
) : ViewModel(){
    private val _uiState = MutableStateFlow(SessionListUiState())
    val uiState: StateFlow<SessionListUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<SessionListEffect>()
    val effect: SharedFlow<SessionListEffect> = _effect.asSharedFlow()

    init {
        observeSessionListData()
    }

    private fun observeSessionListData(){
        viewModelScope.launch {
            combine(
                repository.getAllSession(),
                repository.getAllTag()
            ) { sessions, tags ->
                val tagMap = tags.associateBy { it.tagID }
                sessions to tagMap
            }
                .catch { throwable ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = throwable.message?: "Failed to load session list data"
                    )
                }
                .collect { (sessions, tagMap) ->
                    val now = System.currentTimeMillis()

                    val upcoming = sessions
                        .filter { it.startDateTime >= now }
                        .sortedBy { it.startDateTime }
                        .map { it.toUpcomingUi(tagMap) }

                    val history = sessions
                        .filter { it.startDateTime < now }
                        .sortedByDescending { it.startDateTime }
                        .map { it.toHistoryUi()}

                    _uiState.value =  _uiState.value.copy(
                        isLoading = false,
                        upcoming = upcoming,
                        history = history,
                        errorMessage = null
                    )

                }
        }
    }

    fun onTabSelected(index: Int) {
        val tab =
            if (index == 0) {
                SessionTab.UPCOMING
            }
            else {SessionTab.HISTORY}
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun onToggleDeleteMode(){
        _uiState.value = _uiState.value.copy(
            isDeleteMode = !_uiState.value.isDeleteMode,
            pendingDeleteSessionId = null
        )
    }
    fun onRequestDelete(sessionId: Int) {
        _uiState.value = _uiState.value.copy(pendingDeleteSessionId = sessionId)
    }
    fun onDismissDeleteDialog() {
        _uiState.value = _uiState.value.copy(pendingDeleteSessionId = null)
    }
    fun onConfirmDelete() {
        viewModelScope.launch {
            val id = _uiState.value.pendingDeleteSessionId ?: return@launch
            val entity = repository.getAllSessionById(id)
            if (entity != null) {
                repository.deleteSession(entity)
                _effect.emit(SessionListEffect.ShowMessage("Session deleted"))
            }
            _uiState.value = _uiState.value.copy(pendingDeleteSessionId = null)
        }
    }

    fun onAddClicked() {
        viewModelScope.launch {
            _effect.emit(SessionListEffect.NavigateToCreate)
        }
    }
    fun onEditClicked(id: Int) {
        viewModelScope.launch {
            _effect.emit(SessionListEffect.NavigateToEdit(id))
        }
    }
    private fun Session.toUpcomingUi(tagMap: Map<Int, Tag>): UpcomingSessionUi {
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        val zone = ZoneId.systemDefault()
        val start = Instant.ofEpochMilli(startDateTime).atZone(zone).toLocalTime().format(formatter)
        val end = Instant.ofEpochMilli(endDateTime).atZone(zone).toLocalTime().format(formatter)

        val tagLabels = tagIds
            .mapNotNull { id -> tagMap[id]?.tagName?.let { "#$it" } }
            .ifEmpty { listOf("#none") }

        return UpcomingSessionUi(
            id = sessionID,
            title = sessionName,
            time = "$start - $end",
            tags = tagLabels,
            recurrence = recurrence
        )
    }
    private fun Session.toHistoryUi(): HistorySessionUi {
        val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
        val zone = ZoneId.systemDefault()
        val dateText = Instant.ofEpochMilli(startDateTime).atZone(zone).toLocalDate().format(dateFormatter)
        return HistorySessionUi(
            id = sessionID,
            title = sessionName,
            date = dateText,
            duration = "$durationMin mins"
        )
    }
}
