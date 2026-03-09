package com.monkey.focus_app.data.db.entitiy

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Tag(
    @PrimaryKey val tagID: Int,
    @ColumnInfo val name: String,
    @ColumnInfo val colorHex: String, // e.g. #FFFF00
    @ColumnInfo val packageNames: List<String>,
)