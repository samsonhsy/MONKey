package com.monkey.focus_app.service.scheduler

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.monkey.focus_app.data.AppRepository
import com.monkey.focus_app.data.db.DatabaseBuilder
import com.monkey.focus_app.data.db.entity.FocusLog
import com.monkey.focus_app.service.focus.FocusActions
import com.monkey.focus_app.service.focus.FocusEnforcementService
import com.monkey.focus_app.service.focus.FocusNotificationFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.Instant

class SessionEndReceiver : BroadcastReceiver(){
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != AlarmIds.ACTION_SESSION_END) return
        val sessionId = intent.getIntExtra(AlarmIds.EXTRA_SESSION_ID, -1)
        if (sessionId <= 0) return
        val appContext = context.applicationContext
        FocusNotificationFactory.ensureChannel(appContext)
        val manager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val stopIntent = Intent(appContext, FocusEnforcementService::class.java).apply {
            action = FocusActions.ACTION_STOP_SESSION
        }
        appContext.startService(stopIntent)

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

            if (session.isActive) {
                val now = System.currentTimeMillis()
                val actualEndTime = maxOf(now, session.startDateTime)
                repository.insertAllFocusLog(
                    FocusLog(
                        focusLogID = 0,
                        sessionID = session.sessionID,
                        actualStartTime = session.startDateTime,
                        actualEndTime = actualEndTime,
                        wasHardUnlocked = false,
                        pointsEarned = 0
                    )
                )
                manager.notify(
                    30_000 + session.sessionID,
                    FocusNotificationFactory.buildEndAlert(
                        context = appContext,
                        sessionName = session.sessionName,
                        focusedDurationText = formatFocusedDuration(
                            startMs = session.startDateTime,
                            endMs = actualEndTime
                        )
                    )
                )
            }

            repository.updateAllSession(session.copy(isActive = false))

            // Recurrence handling
            val recurrence = session.recurrence.uppercase()
            if (recurrence == "DAILY" || recurrence == "WEEKLY"){
                val daysOffset = if (recurrence == "DAILY") 1L else 7L
                val nextSession = session.copy(
                    sessionID = 0,
                    startDateTime = shiftByDays(session.startDateTime, daysOffset),
                    endDateTime = shiftByDays(session.endDateTime, daysOffset),
                    isActive = false
                )
                val insertedIds = repository.insertAllSession(nextSession)
                val nextSessionId = insertedIds.firstOrNull()?.toInt()?:-1

                if(nextSessionId > 0){
                    val scheduler = AlarmScheduler(appContext)
                    scheduler.scheduleSession(nextSession.copy(sessionID = nextSessionId))
                }
            }
        }
    }
    private fun shiftByDays(epochMillis: Long, days: Long): Long{
        val zone = ZoneId.systemDefault()
        return Instant.ofEpochMilli(epochMillis)
            .atZone(zone)
            .plusDays(days)
            .toInstant()
            .toEpochMilli()
    }

    private fun formatFocusedDuration(startMs: Long, endMs: Long): String {
        val totalMinutes = ((endMs - startMs).coerceAtLeast(0L) / 60_000L).toInt()
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return when {
            hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
            hours > 0 -> "${hours}h"
            else -> "${minutes}m"
        }
    }
}
