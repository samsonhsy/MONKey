package com.monkey.focus_app.service.focus

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class FocusRuntimeState(
    val isRunning: Boolean = false,
    val sessionId: Int? = null,
    val sessionName: String? = null,
    val startMs: Long? = null,
    val endMs: Long? = null,
    val unlockLevel: String? = null,
    val blockedPackages: Set<String> = emptySet(),
    val warningVisible: Boolean = false,
    val lastWarningAtMs: Long = 0L
)

object FocusRuntimeStore {
    private val _state = MutableStateFlow(FocusRuntimeState())
    val state: StateFlow<FocusRuntimeState> = _state.asStateFlow()
    fun setActive(
        sessionId: Int,
        sessionName: String,
        startMs: Long,
        endMs: Long,
        unlockLevel: String,
        blockedPackages: Set<String>
    ) {
        _state.value = FocusRuntimeState(
            isRunning = true,
            sessionId = sessionId,
            sessionName = sessionName,
            startMs = startMs,
            endMs = endMs,
            unlockLevel = unlockLevel,
            blockedPackages = blockedPackages,
            warningVisible = false,
            lastWarningAtMs = 0L
        )
    }
    fun setStopped() {
        _state.value = FocusRuntimeState() // default value
    }
    fun setWarningVisible(visible: Boolean) {
        _state.value = _state.value.copy(warningVisible = visible)
    }
    fun markWarningShown(nowMs: Long) {
        _state.value = _state.value.copy(lastWarningAtMs = nowMs)
    }
    fun isBlocked(packageName: String): Boolean {
        val current = _state.value
        return current.isRunning && current.blockedPackages.contains(packageName)
    }
}