package com.monkey.focus_app.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(
    tableName = "reward_item"
)
data class RewardItem(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "reward_item_id") val rewardItemID: Int,
    @ColumnInfo(name = "reward_item_name") val rewardItemName: String,
    val description: String,
    val cost: Int,
    @ColumnInfo(name = "is_unlocked") val isUnlocked: Boolean,
    @ColumnInfo(name = "asset_name") val assetName: String
)