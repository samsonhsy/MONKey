package com.monkey.focus_app.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.monkey.focus_app.data.db.entity.RewardItem

@Dao
interface RewardItemDao {
    @Query("SELECT * FROM reward_item")
    suspend fun getAll(): List<RewardItem>

    @Query("SELECT * FROM reward_item WHERE reward_item_id IN (:rewardItemID)")
    suspend fun loadAllByIds(rewardItemID: IntArray): List<RewardItem>

    @Query("SELECT * FROM reward_item WHERE reward_item_name IN (:rewardItemName)")
    suspend fun loadAllByNames(rewardItemName: Array<String>): List<RewardItem>

    @Query(value = "SELECT * FROM reward_item WHERE cost <= (:rewardItemCost)")
    suspend fun loadAllWithinCost(rewardItemCost: Int): RewardItem

    @Insert
    suspend fun insertAll(vararg rewardItem: RewardItem)

    @Delete
    suspend fun delete(rewardItem: RewardItem)
}