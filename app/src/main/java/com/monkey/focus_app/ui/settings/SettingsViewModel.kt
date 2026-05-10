package com.monkey.focus_app.ui.settings

import android.content.Context
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val permissionStatus: PermissionChecklistStatus = PermissionChecklistStatus(
        accessibilityEnabled = false,
        exactAlarmAllowed = false,
        notificationsAllowed = false
    )
)

sealed interface SettingsEffect {
    data object OpenAccessibilitySettings : SettingsEffect
    data object OpenExactAlarmSettings : SettingsEffect
    data object RequestNotificationPermission : SettingsEffect
}

class SettingsViewModel(
    private val appContext: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<SettingsEffect>()
    val effect: SharedFlow<SettingsEffect> = _effect.asSharedFlow()

    init {
        refreshStatus()
    }

    fun refreshStatus() {
        _uiState.value = _uiState.value.copy(
            permissionStatus = PermissionSetup.getStatus(appContext)
        )
    }

    fun onAccessibilityClicked() {
        viewModelScope.launch {
            _effect.emit(SettingsEffect.OpenAccessibilitySettings)
        }
    }

    fun onExactAlarmClicked() {
        viewModelScope.launch {
            _effect.emit(SettingsEffect.OpenExactAlarmSettings)
        }
    }

    fun onNotificationsClicked() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        viewModelScope.launch {
            _effect.emit(SettingsEffect.RequestNotificationPermission)
        }
    }
}
