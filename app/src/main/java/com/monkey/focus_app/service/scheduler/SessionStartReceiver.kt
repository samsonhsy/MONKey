package com.monkey.focus_app.service.scheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.monkey.focus_app.data.AppRepository
import com.monkey.focus_app.data.db.DatabaseBuilder
import com.monkey.focus_app.service.focus.FocusActions
import com.monkey.focus_app.service.focus.FocusEnforcementService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SessionStartReceiver : BroadcastReceiver(){
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != AlarmIds.ACTION_SESSION_START) return
        val sessionId = intent.getIntExtra(AlarmIds.EXTRA_SESSION_ID, -1)
        if (sessionId <= 0) return

        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            val db = DatabaseBuilder.getInstance(appContext)
            val repository = AppRepository(
                focusLogDao = db.focusLogDao(),
                rewardDao = db.rewardItemDao(),
                sessionDao = db.sessionDao(),
                tagDao = db.tagDao(),
                userStatsDao = db.userStatsDao()
            )
            val session = repository.getAllSessionById(sessionId)?:return@launch
            repository.updateAllSession(session.copy(isActive = true))
        }

        val serviceIntent = Intent(appContext, FocusEnforcementService::class.java).apply{
            action = FocusActions.ACTION_START_SESSION
            putExtra(FocusActions.EXTRA_SESSION_ID, sessionId)
        }

        ContextCompat.startForegroundService(appContext, serviceIntent)

    }
}