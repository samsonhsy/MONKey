package com.monkey.focus_app.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.monkey.focus_app.data.db.entitiy.Session


@Dao
interface SessionDao {
    @Query("SELECT * FROM session_table")
    fun getAll(): List<Session>

    @Query("SELECT * FROM session_table WHERE session_id IN (:id)")
    fun getSessionsById(id: Int): Session

    @Query("SELECT * FROM session_table WHERE is_active = 1")
    fun getActiveSession(): List<Session>

    @Insert
    fun insertAll(vararg session: Session)

    @Update
    fun updateAll(vararg session: Session)

    @Delete
    fun delete(vararg session: Session)
}