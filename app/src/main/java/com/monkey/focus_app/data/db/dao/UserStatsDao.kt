package com.monkey.focus_app.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.monkey.focus_app.data.db.entitiy.UserStats

@Dao
interface UserStatsDao {

    @Query("SELECT * FROM user_stats")
    fun getAll(): UserStats

    @Update
    fun updateAll(userStats: UserStats)

}