package com.monkey.focus_app.ui.warning

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monkey.focus_app.data.AppRepository
import com.monkey.focus_app.data.db.DatabaseBuilder
import com.monkey.focus_app.service.focus.FocusActions
import com.monkey.focus_app.service.focus.FocusEnforcementService
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WarningUiState(
    val sessionId: Int = -1,
    val blockedPackage: String = "",
    val unlockLevel: String = "NOVICE",
    val currentStep: Int = 0,
    val typedText: String = "",
    val shakeCount: Int = 0,
    val shakeProgress: Float = 0f
)

sealed interface WarningEffect {
    data object NavigateToDeviceHome : WarningEffect
    data object CloseWarning : WarningEffect
    data object NavigateToUnlock : WarningEffect
}

class WarningViewModel(
    private val appContext: Context,
    sessionId: Int,
    blockedPackage: String,
    unlockLevel: String
) : ViewModel() {

    private val repository: AppRepository by lazy {
        val db = DatabaseBuilder.getInstance(appContext)
        AppRepository(
            focusLogDao = db.focusLogDao(),
            rewardDao = db.rewardItemDao(),
            sessionDao = db.sessionDao(),
            tagDao = db.tagDao(),
            userStatsDao = db.userStatsDao()
        )
    }

    private val _state = MutableStateFlow(
        WarningUiState(
            sessionId = sessionId,
            blockedPackage = blockedPackage,
            unlockLevel = unlockLevel
        )
    )
    val state: StateFlow<WarningUiState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<WarningEffect>()
    val effect: SharedFlow<WarningEffect> = _effect.asSharedFlow()

    val unlockPhrase = "I have decided not to focus and be addicted to my cell phone again."

    companion object {
        const val SHAKE_TARGET = 100
    }

    fun onNewBlockedApp(sessionId: Int, blockedPackage: String, unlockLevel: String) {
        _state.update {
            it.copy(
                sessionId = sessionId,
                blockedPackage = blockedPackage,
                unlockLevel = unlockLevel
            )
        }
    }

    fun onBackToFocusClicked() {
        viewModelScope.launch {
            _effect.emit(WarningEffect.NavigateToDeviceHome)
        }
    }

    fun onUnlockClicked() {
        _state.update { it.copy(currentStep = 0, typedText = "", shakeCount = 0, shakeProgress = 0f) }
        viewModelScope.launch {
            _effect.emit(WarningEffect.NavigateToUnlock)
        }
    }

    fun onTypedTextChanged(text: String) {
        _state.update { it.copy(typedText = text) }
    }

    fun onSubmitUnlock() {
        val currentState = _state.value
        if (currentState.typedText == unlockPhrase) {
            if (currentState.unlockLevel.uppercase() == "BHIKKHU" && currentState.currentStep == 0) {
                _state.update { it.copy(currentStep = 1) }
            } else {
                onUnlockSuccess()
            }
        }
    }

    fun onShakeStepCompleted() {
        val newCount = _state.value.shakeCount + 1
        _state.update {
            it.copy(shakeCount = newCount, shakeProgress = newCount / SHAKE_TARGET.toFloat())
        }
        if (newCount >= SHAKE_TARGET) {
            onUnlockSuccess()
        }
    }

    fun onCancelUnlock() {
        _state.update { it.copy(currentStep = 0, typedText = "", shakeCount = 0, shakeProgress = 0f) }
    }

    private fun onUnlockSuccess() {
        viewModelScope.launch {
            val stopIntent = Intent(appContext, FocusEnforcementService::class.java).apply {
                action = FocusActions.ACTION_STOP_SESSION
                putExtra(FocusActions.EXTRA_SESSION_ID, _state.value.sessionId)
            }
            appContext.startService(stopIntent)

            val id = _state.value.sessionId
            if (id > 0) {
                val session = repository.getAllSessionById(id)
                if (session != null) {
                    repository.updateAllSession(session.copy(isActive = false))
                }
            }

            _effect.emit(WarningEffect.NavigateToDeviceHome)
        }
    }
}
