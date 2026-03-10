package com.monkey.focus_app.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.monkey.focus_app.data.db.entitiy.Tag


@Dao
interface TagDao {
    @Query("SELECT * FROM tag_table")
    fun getAll(): List<Tag>

    @Query("SELECT * FROM tag_table WHERE tag_id IN (:id)")
    fun getSessionsById(id: Int): Tag

    @Insert
    fun insertAll(vararg tag: Tag)

    @Update
    fun updateAll(vararg tag: Tag)

    @Delete
    fun delete(vararg tag: Tag)
}