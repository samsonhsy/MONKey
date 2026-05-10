package com.monkey.focus_app.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.monkey.focus_app.data.db.entity.FocusLog
import com.monkey.focus_app.data.db.entity.Session
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusLogDao {
    @Query("SELECT * FROM focus_log")
    fun observeAll(): Flow<List<FocusLog>>

    @Query("SELECT * FROM focus_log")
    suspend fun getAll(): List<FocusLog>

    @Query("SELECT * FROM focus_log WHERE focus_log_id IN (:focusLogID)")
    suspend fun loadAllByIds(focusLogID: IntArray): List<FocusLog>

    @Query(
        "SELECT * FROM session_table " +
                "JOIN focus_log ON focus_log.session_id = session_table.session_id " +
                "WHERE focus_log_id IN (:focusLogID)"
    )
    suspend fun findSessionByIds(focusLogID: IntArray): List<Session>

    @Insert
    suspend fun insertAll(vararg focusLog: FocusLog)

    @Delete
    suspend fun delete(vararg focusLog: FocusLog)
}