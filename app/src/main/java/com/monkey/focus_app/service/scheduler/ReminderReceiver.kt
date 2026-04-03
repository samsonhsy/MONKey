package com.monkey.focus_app.service.scheduler

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.monkey.focus_app.data.AppRepository
import com.monkey.focus_app.data.db.DatabaseBuilder
import com.monkey.focus_app.service.focus.FocusNotificationFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != AlarmIds.ACTION_REMINDER) return
        val sessionId = intent.getIntExtra(AlarmIds.EXTRA_SESSION_ID, -1)
        if (sessionId <= 0) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        FocusNotificationFactory.ensureChannel(context)

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
            val session = repository.getAllSessionById(sessionId) ?: return@launch
            if (session.reminderOffsetMinutes.toInt() == 0) return@launch
            manager.notify(
                10_000 + sessionId,
                FocusNotificationFactory.buildReminderAlert(
                    context = appContext,
                    sessionName = session.sessionName,
                    minutesBeforeStart = session.reminderOffsetMinutes
                )
            )
        }
    }
}
