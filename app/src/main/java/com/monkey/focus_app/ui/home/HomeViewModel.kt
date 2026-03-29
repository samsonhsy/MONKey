package com.monkey.focus_app.ui.home

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
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.Instant

// UI friendly data for session item in Home screen
data class HomeSessionItemUi(
    val id: Int,
    val title: String,
    val timeslot: String,
    val duration: String,
    val tag: String
)

data class HomeUiState(
    val isLoading: Boolean = false,
    val weeklyFocusText: String = "0h 0m",
    val monkneyBalanceText: String = "0",
    val todaySessions: List<HomeSessionItemUi> = emptyList(),
    val errorMessage: String? = null
)

sealed interface HomeEffect {
    data object NavigateToCreateSession : HomeEffect
    data object NavigateToSessionList : HomeEffect
}

class HomeViewModel(
    private val repository: AppRepository
) : ViewModel(){
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow() // Read only state flow

    private val _effect = MutableSharedFlow<HomeEffect>()
    val effect: SharedFlow<HomeEffect> = _effect.asSharedFlow()

    init {
        observeHomeData()
    }

    private fun observeHomeData(){
        viewModelScope.launch {
            combine(
                repository.getAllSession(),
                repository.getAllTag(),
                repository.getAllUserStats()
            ){ sessions, tags, stats ->
                val tagMap = tags.associateBy { it.tagID }

               val todaySessions = sessions
                   .filter{isToday(it.startDateTime)}
                   .sortedBy{it.startDateTime}
                   .map{it.toHomeUi(tagMap)}

                HomeUiState(
                    isLoading = false,
                    weeklyFocusText = formatMinutesToHourMin(stats?.totalFocusMinutes?: 0),
                    monkneyBalanceText = (stats?.totalMonkney?:0).toString(),
                    todaySessions = todaySessions,
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

    private fun Session.toHomeUi(tagMap: Map<Int, Tag>): HomeSessionItemUi{
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val zone = ZoneId.systemDefault()

        val start = Instant.ofEpochMilli(startDateTime).atZone(zone).toLocalTime().format(timeFormatter)
        val end = Instant.ofEpochMilli(endDateTime).atZone(zone).toLocalTime().format(timeFormatter)

        val tagLabel = tagIds
            .firstOrNull()
            ?.let { id -> tagMap[id]?.tagName }
            ?.let { "#$it" }
            ?: "#none"

        return HomeSessionItemUi(
            id = sessionID,
            title = sessionName,
            timeslot = "$start - $end",
            duration = "$durationMin min",
            tag = tagLabel
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
}
