package com.monkey.focus_app.service.focus

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class FocusRuntimeState(
    val isRunning: Boolean = false,
    val sessionId: Int? = null,
    val blockedPackages: Set<String> = emptySet(),
    val unlockLevel: String? = null,
    val warningVisible: Boolean = false,
    val lastWarningAtMs: Long = 0L
)

object FocusRuntimeStore {
    private val _state = MutableStateFlow(FocusRuntimeState())
    val state: StateFlow<FocusRuntimeState> = _state.asStateFlow()

    fun setRunning(isRunning: Boolean, sessionId: Int? = null) {
        _state.update { it.copy(isRunning = isRunning, sessionId = sessionId) }
    }

    fun setBlockedPackages(packages: Set<String>) {
        _state.update { it.copy(blockedPackages = packages) }
    }

    fun setUnlockLevel(level: String) {
        _state.update { it.copy(unlockLevel = level) }
    }

    fun setWarningVisible(visible: Boolean) {
        _state.update { it.copy(warningVisible = visible) }
    }

    fun markWarningShown(timestamp: Long) {
        _state.update { it.copy(lastWarningAtMs = timestamp) }
    }

    fun reset() {
        _state.value = FocusRuntimeState()
    }
}
