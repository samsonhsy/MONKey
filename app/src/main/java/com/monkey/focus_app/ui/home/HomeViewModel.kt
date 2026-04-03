package com.monkey.focus_app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import com.monkey.focus_app.data.AppRepository
import com.monkey.focus_app.data.db.entity.Session
import com.monkey.focus_app.ui.settings.PermissionChecklistStatus
import com.monkey.focus_app.ui.settings.PermissionSetup
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.Instant

// UI friendly data for session item in Home screen
data class HomeSessionItemUi(
    val id: Int,
    val title: String,
    val timeslot: String,
    val duration: String,
    val recurrence: String,
    val isActive: Boolean
)

data class HomeUiState(
    val isLoading: Boolean = false,
    val weeklyFocusText: String = "0h 0m",
    val monkneyBalanceText: String = "0",
    val todaySessions: List<HomeSessionItemUi> = emptyList(),
    val permissionStatus: PermissionChecklistStatus = PermissionChecklistStatus(
        accessibilityEnabled = false,
        exactAlarmAllowed = false,
        notificationsAllowed = false
    ),
    val showFirstSetupDialog: Boolean = false,
    val errorMessage: String? = null
)

sealed interface HomeEffect {
    data object NavigateToCreateSession : HomeEffect
    data object NavigateToSessionList : HomeEffect
    data object NavigateToSettings : HomeEffect
}

class HomeViewModel(
    private val repository: AppRepository,
    private val appContext: Context
) : ViewModel(){
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow() // Read only state flow

    private val _effect = MutableSharedFlow<HomeEffect>()
    val effect: SharedFlow<HomeEffect> = _effect.asSharedFlow()

    init {
        refreshPermissionStatus()
        observeHomeData()
    }

    private fun observeHomeData(){
        viewModelScope.launch {
            combine(
                repository.getAllSession(),
                repository.observeAllFocusLog(),
                repository.getAllUserStats()
            ){ sessions, focusLogs, stats ->
                val todaySessions = sessions
                    .filter{isToday(it.startDateTime)}
                    .sortedBy{it.startDateTime}
                    .map{it.toHomeUi()}

                val weeklyMinutesFromLogs = calculateWeeklyFocusMinutes(focusLogs)
                val weeklyMinutes = maxOf(weeklyMinutesFromLogs, stats?.totalFocusMinutes ?: 0)

                HomeUiState(
                    isLoading = false,
                    weeklyFocusText = formatMinutesToHourMin(weeklyMinutes),
                    monkneyBalanceText = (stats?.totalMonkney?:0).toString(),
                    todaySessions = todaySessions,
                    permissionStatus = _uiState.value.permissionStatus,
                    showFirstSetupDialog = _uiState.value.showFirstSetupDialog,
                    errorMessage = null
                )
            }.catch { throwable ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = throwable.message?: "Failed to load home data"
                )
            }
                .collect { newState ->
                    _uiState.value = newState
                }
        }
    }
    fun onStartFocusClicked() {
        viewModelScope.launch {
            _effect.emit(HomeEffect.NavigateToCreateSession)
        }
    }

    fun onViewAllClicked(){
        viewModelScope.launch {
            _effect.emit(HomeEffect.NavigateToSessionList)
        }
    }

    fun onPermissionWarningClicked() {
        viewModelScope.launch {
            _effect.emit(HomeEffect.NavigateToSettings)
        }
    }

    fun dismissFirstSetupDialog() {
        PermissionSetup.markFirstPromptSeen(appContext)
        _uiState.value = _uiState.value.copy(showFirstSetupDialog = false)
    }

    fun openSetupFromDialog() {
        PermissionSetup.markFirstPromptSeen(appContext)
        _uiState.value = _uiState.value.copy(showFirstSetupDialog = false)
        viewModelScope.launch {
            _effect.emit(HomeEffect.NavigateToSettings)
        }
    }

    fun refreshPermissionStatus() {
        val status = PermissionSetup.getStatus(appContext)
        val shouldShowFirstDialog = !status.allReady && PermissionSetup.shouldShowFirstPrompt(appContext)
        _uiState.value = _uiState.value.copy(
            permissionStatus = status,
            showFirstSetupDialog = shouldShowFirstDialog
        )
    }

    private fun Session.toHomeUi(): HomeSessionItemUi{
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val zone = ZoneId.systemDefault()

        val start = Instant.ofEpochMilli(startDateTime).atZone(zone).toLocalTime().format(timeFormatter)
        val end = Instant.ofEpochMilli(endDateTime).atZone(zone).toLocalTime().format(timeFormatter)

        val recurrenceLabel = recurrence
            .uppercase()

        return HomeSessionItemUi(
            id = sessionID,
            title = sessionName,
            timeslot = "$start - $end",
            duration = "$durationMin min",
            recurrence = recurrenceLabel,
            isActive = isActive
        )
    }

    private fun isToday(epochMillis: Long): Boolean{
        val zone = ZoneId.systemDefault()
        val date = Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate()
        val today = java.time.LocalDate.now(zone)
        return date == today
    }

    private fun formatMinutesToHourMin(totalMinutes: Int): String{
        val hours = totalMinutes / 60
        val mins = totalMinutes % 60
        return "${hours}h ${mins}m"
    }

    private fun calculateWeeklyFocusMinutes(logs: List<com.monkey.focus_app.data.db.entity.FocusLog>): Int {
        val now = System.currentTimeMillis()
        val weekStart = now - 7L * 24 * 60 * 60 * 1000
        val totalMillis = logs
            .asSequence()
            .filter { !it.wasHardUnlocked }
            .filter { it.actualEndTime > weekStart }
            .map { log ->
                val start = maxOf(log.actualStartTime, weekStart)
                val end = minOf(log.actualEndTime, now)
                (end - start).coerceAtLeast(0L)
            }
            .sum()

        return (totalMillis / 60_000L).toInt()
    }
}
