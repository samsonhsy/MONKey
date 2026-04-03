package com.monkey.focus_app.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.monkey.focus_app.data.db.entity.Session
import kotlinx.coroutines.flow.Flow


@Dao
interface SessionDao {
    @Query("SELECT * FROM session_table")
    fun getAll(): Flow<List<Session>>

    @Query("SELECT * FROM session_table WHERE session_id = :id")
    suspend fun getSessionsById(id: Int): Session?

    @Query("SELECT * FROM session_table WHERE is_active = 1")
    fun getActiveSession(): Flow<Session?>

    @Query("SELECT * FROM session_table WHERE is_active = 1 LIMIT 1")
    suspend fun getActiveSessionNow(): Session?

    @Query("SELECT * FROM session_table WHERE end_datetime > :nowMillis")
    suspend fun getUpcomingOrOngoingSessions(nowMillis: Long): List<Session>

    @Insert
    suspend fun insertAll(vararg session: Session) : LongArray

    @Update
    suspend fun updateAll(vararg session: Session)

    @Delete
    suspend fun delete(vararg session: Session)
}
