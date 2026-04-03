package com.monkey.focus_app.service.focus

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.monkey.focus_app.data.AppRepository
import com.monkey.focus_app.data.db.DatabaseBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class FocusEnforcementService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            FocusActions.ACTION_STOP_SESSION -> handleStopSession(intent)
            FocusActions.ACTION_START_SESSION -> handleStartSession(intent)
        }
        return START_NOT_STICKY
    }

    private fun handleStartSession(intent: Intent) {
        val sessionId = intent.getIntExtra(FocusActions.EXTRA_SESSION_ID, -1)
        if (sessionId > 0) {
            FocusRuntimeStore.setRunning(true, sessionId)
        }
    }

    private fun handleStopSession(intent: Intent) {
        val sessionId = intent.getIntExtra(FocusActions.EXTRA_SESSION_ID, -1)
        serviceScope.launch {
            val db = DatabaseBuilder.getInstance(applicationContext)
            val repository = AppRepository(
                focusLogDao = db.focusLogDao(),
                rewardDao = db.rewardItemDao(),
                sessionDao = db.sessionDao(),
                tagDao = db.tagDao(),
                userStatsDao = db.userStatsDao()
            )

            if (sessionId > 0) {
                val session = repository.getAllSessionById(sessionId)
                if (session != null) {
                    repository.updateAllSession(session.copy(isActive = false))
                }
            }

            FocusRuntimeStore.reset()
            stopSelf()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
