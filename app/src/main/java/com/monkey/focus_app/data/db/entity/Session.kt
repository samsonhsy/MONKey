package com.monkey.focus_app.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "session_table",
    foreignKeys = [
        ForeignKey(
            entity = Tag::class,
            parentColumns = arrayOf("tag_id"),
            childColumns = arrayOf("tag_id"),
            onDelete = ForeignKey.SET_NULL
        ),
    ])
data class Session(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "session_id") val sessionID: Int,
    @ColumnInfo(name = "session_name") val sessionName: String,
    @ColumnInfo(name = "start_datetime") val startDateTime: Long,
    @ColumnInfo(name="end_datetime") val endDateTime: Long,
    @ColumnInfo(name="duration_min") val durationMin: Int,
    val recurrence: String,
    @ColumnInfo(name="tag_id")val tagID: Int?,
    @ColumnInfo(name="unlock_key_level")val unlockKeyLevel: String,
    @ColumnInfo(name="reminder_offset_minutes")val reminderOffsetMinutes: String,
    @ColumnInfo(name="is_active")val isActive: Boolean,
    @ColumnInfo(name="unlock_phrase")val unlockPhrase: String?,
)