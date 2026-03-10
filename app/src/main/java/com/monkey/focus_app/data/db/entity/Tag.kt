package com.monkey.focus_app.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tag_table")
data class Tag(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "tag_id") val tagID: Int,
    @ColumnInfo(name="tag_name") val tagName: String,
    @ColumnInfo(name="color_hex") val colorHex: String, // e.g. #FFFF00
    @ColumnInfo(name = "package_names") val packageNames: List<String>,
)