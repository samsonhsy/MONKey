package com.monkey.focus_app.service.focus

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.monkey.focus_app.data.AppRepository
import com.monkey.focus_app.data.db.DatabaseBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FocusEnforcementService : LifecycleService() {
    private lateinit var repository: AppRepository
    override fun onCreate() {
        super.onCreate()
        val database = DatabaseBuilder.getInstance(applicationContext)
        repository = AppRepository(
            focusLogDao = database.focusLogDao(),
            rewardDao = database.rewardItemDao(),
            sessionDao = database.sessionDao(),
            tagDao = database.tagDao(),
            userStatsDao = database.userStatsDao()
        )
        FocusNotificationFactory.ensureChannel(this)
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            FocusActions.ACTION_START_SESSION -> {
                val sessionId = intent.getIntExtra(FocusActions.EXTRA_SESSION_ID, -1)
                if (sessionId <= 0) {
                    stopSelf()
                    return START_NOT_STICKY
                }
                startFocus(sessionId)
            }
            FocusActions.ACTION_STOP_SESSION -> {
                stopFocus()
            }
        }
        return START_STICKY
    }
    private fun startFocus(sessionId: Int) {
        val notification = FocusNotificationFactory.buildPlaceholderOngoing(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                FocusNotificationFactory.NOTIFICATION_ID_ONGOING,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(
                FocusNotificationFactory.NOTIFICATION_ID_ONGOING,
                notification
            )
        }
        lifecycleScope.launch {
            try {
                val session = withContext(Dispatchers.IO) {
                    repository.getAllSessionById(sessionId)
                } ?: run {
                    stopFocus()
                    return@launch
                }
                val blockedPackages = withContext(Dispatchers.IO) {
                    val allTags = repository.getAllTagSnapshot()
                    allTags
                        .filter { session.tagIds.contains(it.tagID) }
                        .flatMap { it.packageNames }
                        .toSet()
                }
                val wasRunningBefore = FocusRuntimeStore.state.value.isRunning
                val previousSessionId = FocusRuntimeStore.state.value.sessionId
                FocusRuntimeStore.setActive(
                    sessionId = session.sessionID,
                    sessionName = session.sessionName,
                    startMs = session.startDateTime,
                    endMs = session.endDateTime,
                    unlockLevel = session.unlockKeyLevel,
                    blockedPackages = blockedPackages
                )
                val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val ongoing = FocusNotificationFactory.buildOngoing(
                    context = this@FocusEnforcementService,
                    sessionName = session.sessionName,
                    startMs = session.startDateTime,
                    endMs = session.endDateTime
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                    startForeground(FocusNotificationFactory.NOTIFICATION_ID_ONGOING, ongoing,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
                }else {
                    startForeground(FocusNotificationFactory.NOTIFICATION_ID_ONGOING, ongoing)
                }

                val shouldPlayStartAlert = !wasRunningBefore || previousSessionId != session.sessionID
                if (shouldPlayStartAlert) {
                    val alertId = 20_000 + session.sessionID
                    manager.notify(
                        alertId,
                        FocusNotificationFactory.buildStartAlert(
                            context = this@FocusEnforcementService,
                            sessionName = session.sessionName,
                            startMs = session.startDateTime,
                            endMs = session.endDateTime
                        )
                    )
                    lifecycleScope.launch {
                        delay(5500L)
                        manager.cancel(alertId)
                    }
                }
            } catch (_: Throwable) {
                stopFocus()
            }
        }
    }
    private fun stopFocus() {
        FocusRuntimeStore.setStopped()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }
    override fun onDestroy() {
        FocusRuntimeStore.setStopped()
        super.onDestroy()
    }
    override fun onBind(intent: Intent): IBinder? {
        return super.onBind(intent)
    }
}
