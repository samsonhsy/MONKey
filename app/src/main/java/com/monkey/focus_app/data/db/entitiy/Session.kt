package com.monkey.focus_app.data.db.entitiy

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Session(
    @PrimaryKey val sessionID: Int,
    @ColumnInfo val sessionName: String,
    @ColumnInfo val startDateTime: String, // e.g. #FFFF00
    @ColumnInfo val packageNames: List<String>,
)