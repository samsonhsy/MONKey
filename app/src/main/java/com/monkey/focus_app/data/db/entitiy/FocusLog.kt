package com.monkey.focus_app.data.db.entitiy

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "focus_log",
    foreignKeys = [
        ForeignKey(
            entity = Session::class,
            parentColumns = ["session_id"],
            childColumns = ["session_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class FocusLog(
    @PrimaryKey @ColumnInfo(name = "focus_log_id") val focusLogID: Int,
    @ColumnInfo(name = "session_id") val sessionID: Int,
    @ColumnInfo(name = "actual_start_time") val actualStartTime: Long,
    @ColumnInfo(name = "actual_end_time") val actualEndTime: Long,
    @ColumnInfo(name = "was_hard_unlocked") val wasHardUnlocked: Boolean,
    @ColumnInfo(name = "points_earned") val pointsEarned: Int
)