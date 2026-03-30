package com.monkey.focus_app.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.monkey.focus_app.data.db.entity.Tag
import kotlinx.coroutines.flow.Flow


@Dao
interface TagDao {
    @Query("SELECT * FROM tag_table")
    fun getAll(): Flow<List<Tag>>

    @Query("SELECT * FROM tag_table WHERE tag_id = :id")
    suspend fun getTagsById(id: Int): Tag

    @Insert
    suspend fun insertAll(vararg tag: Tag): LongArray

    @Update
    suspend fun updateAll(vararg tag: Tag)

    @Delete
    suspend fun delete(vararg tag: Tag)
}
