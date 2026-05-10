package com.monkey.focus_app.service.scheduler

object AlarmIds{
    const val ACTION_REMINDER = "com.monkey.focus_app.service.scheduler.ACTION_REMINDER"
    const val ACTION_SESSION_START = "com.monkey.focus_app.service.scheduler.ACTION_SESSION_START"
    const val ACTION_SESSION_END = "com.monkey.focus_app.service.scheduler.ACTION_SESSION_END"
    const val EXTRA_SESSION_ID = "extra_session_id"

    private const val TYPE_REMINDER = 1
    private const val TYPE_START = 2
    private const val TYPE_END = 3

    // 3 types of request code for a session
    fun reminderRequestCode(sessionId: Int): Int = sessionId * 10 + TYPE_REMINDER
    fun startRequestCode(sessionId: Int): Int = sessionId * 10 + TYPE_START
    fun endRequestCode(sessionId: Int): Int = sessionId * 10 + TYPE_END
}
