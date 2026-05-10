package com.monkey.focus_app.service.focus

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.monkey.focus_app.MainActivity
import com.monkey.focus_app.R
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object FocusNotificationFactory{
    const val CHANNEL_ONGOING_ID = "monkey_focus_ongoing"
    const val CHANNEL_ONGOING_NAME = "MONKey Focus Ongoing"
    const val CHANNEL_ALERT_ID = "monkey_focus_alert"
    const val CHANNEL_ALERT_NAME = "MONKey Focus Alerts"
    const val CHANNEL_BLOCKED_ID = "monkey_focus_blocked"
    const val CHANNEL_BLOCKED_NAME = "MONKey Focus Blocked"
    const val CHANNEL_REMINDER_ID = "monkey_focus_reminder"
    const val CHANNEL_REMINDER_NAME = "MONKey Focus Reminders"
    const val NOTIFICATION_ID_ONGOING = 1001

    fun ensureChannel(context: Context){
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val ongoingChannel = NotificationChannel(
            CHANNEL_ONGOING_ID,
            CHANNEL_ONGOING_NAME,
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Ongoing MONKey focus session"
            setShowBadge(false)
        }

        val alertChannel = NotificationChannel(
            CHANNEL_ALERT_ID,
            CHANNEL_ALERT_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "One-time alert MONKey focus session"
            setShowBadge(false)
            enableVibration(true)
        }

        val reminderChannel = NotificationChannel(
            CHANNEL_REMINDER_ID,
            CHANNEL_REMINDER_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Reminder before MONKey focus session"
            setShowBadge(false)
            enableVibration(true)
        }

        val blockedChannel = NotificationChannel(
            CHANNEL_BLOCKED_ID,
            CHANNEL_BLOCKED_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alerts when an app is blocked"
            setShowBadge(true)
            enableVibration(true)
        }
        manager.createNotificationChannel(ongoingChannel)
        manager.createNotificationChannel(alertChannel)
        manager.createNotificationChannel(reminderChannel)
        manager.createNotificationChannel(blockedChannel)
    }

    fun buildPlaceholderOngoing(context: Context): Notification {
        val notification = NotificationCompat.Builder(context, CHANNEL_ONGOING_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("MONKey Focus")
            .setContentText("Preparing session...")
            .setOngoing(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setAutoCancel(false)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
        notification.flags = notification.flags or Notification.FLAG_NO_CLEAR
        return notification
    }

    fun buildOngoing(
        context: Context,
        sessionName: String,
        startMs: Long,
        endMs: Long
    ): Notification {
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            3001,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val timeText = formatTimeRange(startMs, endMs)
        val notification = NotificationCompat.Builder(context, CHANNEL_ONGOING_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("MONKey Focus: $sessionName")
            .setContentText(timeText)
            .setContentIntent(contentPendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
        notification.flags = notification.flags or Notification.FLAG_NO_CLEAR
        return notification
    }
    fun buildStartAlert(
        context: Context,
        sessionName: String,
        startMs: Long,
        endMs: Long
    ): Notification {
        val contentPendingIntent = openAppPendingIntent(context, 3002)
        val timeText = formatTimeRange(startMs, endMs)
        return NotificationCompat.Builder(context, CHANNEL_ALERT_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Focus session started")
            .setContentText("$sessionName • $timeText")
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .setTimeoutAfter(5000L) // auto-dismiss after 5s
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(Notification.DEFAULT_ALL)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .build()
    }

    fun buildEndAlert(
        context: Context,
        sessionName: String,
        focusedDurationText: String
    ): Notification {
        return NotificationCompat.Builder(context, CHANNEL_ALERT_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Focus session ended")
            .setContentText("$sessionName • Focused for $focusedDurationText")
            .setContentIntent(openAppPendingIntent(context, 3003))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(Notification.DEFAULT_ALL)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .build()
    }

    fun buildReminderAlert(
        context: Context,
        sessionName: String,
        minutesBeforeStart: String
    ): Notification {
        return NotificationCompat.Builder(context, CHANNEL_REMINDER_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("MONKey Reminder")
            .setContentText("$sessionName starts in $minutesBeforeStart min")
            .setContentIntent(openAppPendingIntent(context, 3004))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(Notification.DEFAULT_ALL)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .build()
    }

    fun buildBlockedAlert(
        context: Context,
        sessionId: Int,
        blockedPackage: String,
        unlockLevel: String
    ): Notification {
        val intent = Intent(context, com.monkey.focus_app.ui.warning.WarningActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(FocusActions.EXTRA_SESSION_ID, sessionId)
            putExtra(FocusActions.EXTRA_BLOCKED_PACKAGE, blockedPackage)
            putExtra(FocusActions.EXTRA_UNLOCK_LEVEL, unlockLevel)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            3005 + sessionId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(context, CHANNEL_BLOCKED_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("MONKey Blocked an App")
            .setContentText("Tap here to unlock or return to focus")
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .setTimeoutAfter(30000L) // auto-dismiss after 30s
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(Notification.DEFAULT_ALL)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .build()
    }

    private fun openAppPendingIntent(context: Context, requestCode: Int): PendingIntent {
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            requestCode,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun formatTimeRange(startMs: Long, endMs: Long): String {
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        val zone = ZoneId.systemDefault()
        val start = Instant.ofEpochMilli(startMs).atZone(zone).toLocalTime().format(formatter)
        val end = Instant.ofEpochMilli(endMs).atZone(zone).toLocalTime().format(formatter)
        return "$start - $end"
    }

}
