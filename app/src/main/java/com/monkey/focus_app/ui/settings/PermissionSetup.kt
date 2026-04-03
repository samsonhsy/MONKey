package com.monkey.focus_app.ui.settings

import android.Manifest
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.AlarmManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.monkey.focus_app.service.accessibility.MonkeyAccessibilityService
import androidx.core.net.toUri

data class PermissionChecklistStatus(
    val accessibilityEnabled: Boolean,
    val exactAlarmAllowed: Boolean,
    val notificationsAllowed: Boolean,
) {
    val allReady: Boolean
        get() = accessibilityEnabled && exactAlarmAllowed && notificationsAllowed
}

object PermissionSetup {
    private const val PREFS_NAME = "permission_setup_prefs"
    private const val KEY_SEEN_FIRST_PROMPT = "seen_first_setup_prompt"

    fun getStatus(context: Context): PermissionChecklistStatus {
        return PermissionChecklistStatus(
            accessibilityEnabled = isAccessibilityEnabled(context),
            exactAlarmAllowed = isExactAlarmAllowed(context),
            notificationsAllowed = isNotificationsAllowed(context)
        )
    }

    fun shouldShowFirstPrompt(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return !prefs.getBoolean(KEY_SEEN_FIRST_PROMPT, false)
    }

    fun markFirstPromptSeen(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_SEEN_FIRST_PROMPT, true).apply()
    }

    fun createAccessibilitySettingsIntent(): Intent =
        Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)

    @RequiresApi(Build.VERSION_CODES.S)
    fun createExactAlarmSettingsIntent(context: Context): Intent {
        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
            data = "package:${context.packageName}".toUri()
        }
        if (intent.resolveActivity(context.packageManager) == null) {
            return Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = "package:${context.packageName}".toUri()
            }
        }
        return intent
    }

    private fun isAccessibilityEnabled(context: Context): Boolean {
        val manager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = manager.getEnabledAccessibilityServiceList(
            AccessibilityServiceInfo.FEEDBACK_ALL_MASK
        )
        return enabledServices.any { info ->
            val serviceInfo = info.resolveInfo?.serviceInfo ?: return@any false
            serviceInfo.packageName == context.packageName &&
                    serviceInfo.name == MonkeyAccessibilityService::class.java.name
        }
    }
    private fun isExactAlarmAllowed(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return manager.canScheduleExactAlarms()
    }

    private fun isNotificationsAllowed(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }
}
