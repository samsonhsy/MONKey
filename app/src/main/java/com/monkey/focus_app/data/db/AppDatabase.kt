package com.monkey.focus_app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.monkey.focus_app.data.db.dao.TagDao
import com.monkey.focus_app.data.db.dao.SessionDao
import com.monkey.focus_app.data.db.dao.FocusLogDao
import com.monkey.focus_app.data.db.dao.RewardItemDao
import com.monkey.focus_app.data.db.dao.UserStatsDao
import com.monkey.focus_app.data.db.entity.FocusLog
import com.monkey.focus_app.data.db.entity.RewardItem
import com.monkey.focus_app.data.db.entity.Session
import com.monkey.focus_app.data.db.entity.Tag
import com.monkey.focus_app.data.db.entity.UserStats

@Database(
    entities = [
        Session::class,
        Tag::class,
        FocusLog::class,
        RewardItem::class,
        UserStats::class],
    version = 1
)
@TypeConverters(
    TypeConverter::class
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tagDao(): TagDao
    abstract fun sessionDao(): SessionDao
    abstract fun focusLogDao(): FocusLogDao
    abstract fun rewardItemDao(): RewardItemDao
    abstract fun userStatsDao(): UserStatsDao
}
