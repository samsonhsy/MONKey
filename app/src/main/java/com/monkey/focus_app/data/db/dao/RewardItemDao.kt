package com.monkey.focus_app.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.monkey.focus_app.data.db.entitiy.RewardItem

@Dao
interface RewardItemDao {
    @Query("SELECT * FROM reward_item")
    fun getAll(): List<RewardItem>

    @Query("SELECT * FROM reward_item WHERE reward_item_id IN (:rewardItemID)")
    fun loadAllByIds(rewardItemID: IntArray): List<RewardItem>

    @Query("SELECT * FROM reward_item WHERE reward_item_name IN (:rewardItemName)")
    fun loadAllByNames(rewardItemName: Array<String>): List<RewardItem>

    @Query(value = "SELECT * FROM reward_item WHERE cost <= (:rewardItemCost)")
    fun loadAllWithinCost(rewardItemCost: Int): RewardItem

    @Insert
    fun insertAll(vararg rewardItem: RewardItem)

    @Delete
    fun delete(rewardItem: RewardItem)
}