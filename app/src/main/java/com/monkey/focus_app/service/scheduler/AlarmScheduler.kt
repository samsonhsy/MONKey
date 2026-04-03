package com.monkey.focus_app.service.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.monkey.focus_app.data.db.entity.Session

class AlarmScheduler(
    private val context: Context
) {
    private val alarmManager: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    fun scheduleSession(session: Session) {
        // clear old alarms
        cancelSession(session.sessionID)
        val now = System.currentTimeMillis()
        val startAt = session.startDateTime
        val endAt = session.endDateTime
        val reminderOffsetMin = session.reminderOffsetMinutes.toIntOrNull() ?: 0
        val reminderAt = startAt - (reminderOffsetMin * 60000L)
        // Ignore fully expired sessions
        if (endAt <= now) {
            Log.d("AlarmScheduler", "Skip scheduling expired session ${session.sessionID}")
            return
        }
        // Still future checks
        if (reminderAt > now) {
            val pi = reminderPendingIntent(session.sessionID)
            setExact(reminderAt, pi)
        }
        if (startAt > now) {
            val pi = startPendingIntent(session.sessionID)
            setExact(startAt, pi)
        }
        if (endAt > now) {
            val pi = endPendingIntent(session.sessionID)
            setExact(endAt, pi)
        }
    }
    fun cancelSession(sessionId: Int) {
        alarmManager.cancel(reminderPendingIntent(sessionId))
        alarmManager.cancel(startPendingIntent(sessionId))
        alarmManager.cancel(endPendingIntent(sessionId))
    }
    private fun setExact(triggerAtMillis: Long, pendingIntent: PendingIntent) {
        val canExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
        if (canExact) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } else {
            // Fallback when exact-alarm permission is not granted
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
            Log.w("AlarmScheduler", "Exact alarm not permitted; used inexact fallback")
        }
    }
    private fun reminderPendingIntent(sessionId: Int): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = AlarmIds.ACTION_REMINDER
            putExtra(AlarmIds.EXTRA_SESSION_ID, sessionId)
        }
        return PendingIntent.getBroadcast(
            context,
            AlarmIds.reminderRequestCode(sessionId),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    private fun startPendingIntent(sessionId: Int): PendingIntent {
        val intent = Intent(context, SessionStartReceiver::class.java).apply {
            action = AlarmIds.ACTION_SESSION_START
            putExtra(AlarmIds.EXTRA_SESSION_ID, sessionId)
        }
        return PendingIntent.getBroadcast(
            context,
            AlarmIds.startRequestCode(sessionId),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    private fun endPendingIntent(sessionId: Int): PendingIntent {
        val intent = Intent(context, SessionEndReceiver::class.java).apply {
            action = AlarmIds.ACTION_SESSION_END
            putExtra(AlarmIds.EXTRA_SESSION_ID, sessionId)
        }
        return PendingIntent.getBroadcast(
            context,
            AlarmIds.endRequestCode(sessionId),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}