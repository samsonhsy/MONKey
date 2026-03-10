package com.monkey.focus_app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.monkey.focus_app.data.db.entity.UserStats

@Dao
interface UserStatsDao {

    @Query("SELECT * FROM user_stats")
    fun getAll(): Flow<UserStats?>

    @Insert
    suspend fun insert(userStats: UserStats)

    @Update
    suspend fun update(userStats: UserStats)
}