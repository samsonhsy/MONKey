package com.monkey.focus_app.service.scheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.monkey.focus_app.data.AppRepository
import com.monkey.focus_app.data.db.DatabaseBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return
        val pendingResult = goAsync()
        val appContext = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = DatabaseBuilder.getInstance(appContext)
                val repository = AppRepository(
                    focusLogDao = db.focusLogDao(),
                    rewardDao = db.rewardItemDao(),
                    sessionDao = db.sessionDao(),
                    tagDao = db.tagDao(),
                    userStatsDao = db.userStatsDao()
                )
                val scheduler = AlarmScheduler(appContext)
                val now = System.currentTimeMillis()
                val sessions = repository.getUpcomingOrOngoingSessions(now)
                sessions.forEach { session ->
                    scheduler.scheduleSession(session)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}