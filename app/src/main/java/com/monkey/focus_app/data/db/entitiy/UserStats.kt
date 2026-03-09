package com.monkey.focus_app.data.db.entitiy

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_stats"
)
data class UserStats(
    @PrimaryKey val id: Int = 1,
    @ColumnInfo(name = "total_monkney") val totalMonkney: Int,
    @ColumnInfo(name = "total_focus_minutes") val totalFocusMinutes: Int
)