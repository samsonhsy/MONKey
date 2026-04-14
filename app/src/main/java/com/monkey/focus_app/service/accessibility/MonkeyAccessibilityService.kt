package com.monkey.focus_app.service.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.monkey.focus_app.data.AppRepository
import com.monkey.focus_app.data.db.DatabaseBuilder
import com.monkey.focus_app.service.focus.FocusActions
import com.monkey.focus_app.service.focus.FocusNotificationFactory
import com.monkey.focus_app.service.focus.FocusRuntimeStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@SuppressLint("AccessibilityPolicy")
class MonkeyAccessibilityService : AccessibilityService() {
    private val tag = "MonkeyAccessibility"
    private val warningCooldownMs = 400L
    private val duplicateAttemptGuardMs = 250L
    private val periodicCheckMs = 500L
    private val runtimeRefreshMs = 2_000L
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var lastAttemptAtMs = 0L
    private var lastAttemptPackage = ""
    private var monitorJob: Job? = null
    private var runtimeRefreshJob: Job? = null

    private lateinit var repository: AppRepository
    @Volatile private var fallbackRunning = false
    @Volatile private var fallbackSessionId: Int = -1
    @Volatile private var fallbackUnlockLevel: String = "NOVICE"
    @Volatile private var fallbackBlockedPackages: Set<String> = emptySet()

    private val sessionStopReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == FocusActions.ACTION_STOP_SESSION) {
                Log.i(tag, "Session stop received via broadcast")
                onSessionStopped()
            }
        }
    }

    private fun onSessionStopped() {
        fallbackRunning = false
        fallbackSessionId = -1
        fallbackUnlockLevel = "NOVICE"
        fallbackBlockedPackages = emptySet()
        FocusRuntimeStore.setStopped()
        Log.i(tag, "Session stopped - blocking disabled")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()

        registerReceiver(
            sessionStopReceiver,
            IntentFilter(FocusActions.ACTION_STOP_SESSION),
            RECEIVER_NOT_EXPORTED
        )

        val database = DatabaseBuilder.getInstance(applicationContext)
        repository = AppRepository(
            focusLogDao = database.focusLogDao(),
            rewardDao = database.rewardItemDao(),
            sessionDao = database.sessionDao(),
            tagDao = database.tagDao(),
            userStatsDao = database.userStatsDao()
        )

        serviceInfo = serviceInfo?.apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or AccessibilityEvent.TYPE_WINDOWS_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 100
            flags = flags or
                AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        }

        monitorJob?.cancel()
        monitorJob = serviceScope.launch {
            while (isActive) {
                val currentPkg = resolveForegroundPackage()
                if (currentPkg.isNotEmpty()) {
                    triggerWarningIfNeeded(currentPkg, force = false)
                }
                delay(periodicCheckMs)
            }
        }

        runtimeRefreshJob?.cancel()
        runtimeRefreshJob = serviceScope.launch(Dispatchers.IO) {
            while (isActive) {
                runCatching {
                    val now = System.currentTimeMillis()
                    val active = repository.getActiveSessionNow()
                    if (active == null || active.endDateTime <= now) {
                        if (fallbackRunning) {
                            onSessionStopped()
                        }
                    } else {
                        val allTags = repository.getAllTagSnapshot()
                        val blocked = allTags
                            .filter { active.tagIds.contains(it.tagID) }
                            .flatMap { it.packageNames }
                            .toSet()
                        fallbackRunning = true
                        fallbackSessionId = active.sessionID
                        fallbackUnlockLevel = active.unlockKeyLevel
                        fallbackBlockedPackages = blocked
                        val current = FocusRuntimeStore.state.value
                        val shouldSyncRuntime =
                            !current.isRunning ||
                                current.sessionId != active.sessionID ||
                                current.unlockLevel != active.unlockKeyLevel ||
                                current.blockedPackages != blocked
                        if (shouldSyncRuntime) {
                            FocusRuntimeStore.setActive(
                                sessionId = active.sessionID,
                                sessionName = active.sessionName,
                                startMs = active.startDateTime,
                                endMs = active.endDateTime,
                                unlockLevel = active.unlockKeyLevel,
                                blockedPackages = blocked
                            )
                        }
                    }
                }.onFailure {
                    Log.w(tag, "Failed runtime refresh: ${it.message}")
                }
                delay(runtimeRefreshMs)
            }
        }

        serviceScope.launch {
            delay(400L)
            val currentPkg = resolveForegroundPackage()
            if (currentPkg.isNotEmpty()) {
                triggerWarningIfNeeded(currentPkg, force = true)
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (
            event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            event.eventType != AccessibilityEvent.TYPE_WINDOWS_CHANGED
        ) return

        val pkg = event.packageName?.toString()?.trim().orEmpty()
        if (pkg.isEmpty()) return
        triggerWarningIfNeeded(pkg, force = false)
    }

    private fun triggerWarningIfNeeded(pkg: String, force: Boolean) {
        if (pkg == applicationContext.packageName) return
        val runtime = FocusRuntimeStore.state.value
        val running = runtime.isRunning || fallbackRunning
        if (!running) return
        val blockedPackages = runtime.blockedPackages.ifEmpty { fallbackBlockedPackages }
        if (!blockedPackages.contains(pkg)) return
        if (runtime.warningVisible) return

        Log.d(tag, "Blocked app detected: pkg=$pkg blockedCount=${blockedPackages.size}")

        val now = System.currentTimeMillis()
        if (!force && now - runtime.lastWarningAtMs < warningCooldownMs) return
        if (!force && pkg == lastAttemptPackage && now - lastAttemptAtMs < duplicateAttemptGuardMs) return

        lastAttemptAtMs = now
        lastAttemptPackage = pkg

        val sessionId = runtime.sessionId ?: fallbackSessionId
        val unlockLevel = runtime.unlockLevel ?: fallbackUnlockLevel

        performGlobalAction(GLOBAL_ACTION_HOME)

        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(
            3005 + sessionId,
            FocusNotificationFactory.buildBlockedAlert(this, sessionId, pkg, unlockLevel)
        )

        FocusRuntimeStore.markWarningShown(now)
        Log.d(tag, "Blocked app $pkg, forced home, and sent notification.")
    }

    private fun resolveForegroundPackage(): String {
        val rootNode = rootInActiveWindow
        val fromRoot = rootNode?.packageName?.toString()?.trim().orEmpty()
        Log.v(tag, "rootInActiveWindow: pkg=$fromRoot, node=$rootNode")
        if (fromRoot.isNotEmpty()) return fromRoot

        val fromWindows = windows
            .firstOrNull { it.isActive || it.isFocused }
            ?.root
            ?.packageName
            ?.toString()
            ?.trim()
            .orEmpty()
        Log.v(tag, "windows[0] pkg: $fromWindows")

        return fromWindows
    }

    override fun onDestroy() {
        try {
            unregisterReceiver(sessionStopReceiver)
        } catch (e: Exception) {
        }
        monitorJob?.cancel()
        runtimeRefreshJob?.cancel()
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onInterrupt() = Unit
}
