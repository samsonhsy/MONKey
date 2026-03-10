package com.monkey.focus_app.data

import com.monkey.focus_app.data.db.dao.FocusLogDao
import com.monkey.focus_app.data.db.dao.RewardItemDao
import com.monkey.focus_app.data.db.dao.SessionDao
import com.monkey.focus_app.data.db.dao.TagDao
import com.monkey.focus_app.data.db.dao.UserStatsDao
import com.monkey.focus_app.data.db.entity.FocusLog
import com.monkey.focus_app.data.db.entity.RewardItem
import com.monkey.focus_app.data.db.entity.Session
import com.monkey.focus_app.data.db.entity.Tag
import com.monkey.focus_app.data.db.entity.UserStats

class AppRepository(
    private val focusLogDao: FocusLogDao,
    private val rewardDao: RewardItemDao,
    private val sessionDao: SessionDao,
    private val tagDao: TagDao,
    private val userStatsDao: UserStatsDao,
    ) {
/*--------------------------------------------------------------Focus Log------------------------------------------------------- */
    fun getAllFocusLog() = focusLogDao.getAll()

    fun getAllFocusLogByIds(focusLogID: IntArray) = focusLogDao.loadAllByIds(focusLogID)

    fun getSessionByFocusIds(focusLogID: IntArray) = focusLogDao.findSessionByIds(focusLogID)

    fun insertAllFocusLog(vararg focusLog: FocusLog) = focusLogDao.insertAll(*focusLog)

    fun deleteFocusLog(vararg focusLog: FocusLog) = focusLogDao.delete(*focusLog)

/*--------------------------------------------------------------Reward--------------------------------------------------------- */
    fun getAllReward() = rewardDao.getAll()

    fun getAllRewardByIds(rewardItemID: IntArray) = rewardDao.loadAllByIds(rewardItemID)

    fun getAllRewardByNames(rewardItemName: Array<String>) = rewardDao.loadAllByNames(rewardItemName)

    fun getAllRewardWithinCost(rewardItemCost: Int) = rewardDao.loadAllWithinCost(rewardItemCost)

    fun insertAllReward(vararg rewardItem: RewardItem) = rewardDao.insertAll(*rewardItem)

    fun deleteReward(rewardItem: RewardItem) = rewardDao.delete(rewardItem)
/*--------------------------------------------------------------Session------------------------------------------------------- */

    fun getAllSession() = sessionDao.getAll()

    suspend fun getAllSessionById(id: Int) = sessionDao.getSessionsById(id)

    fun getAllActiveSession() = sessionDao.getActiveSession()

    fun insertAllSession(vararg session: Session) = sessionDao.insertAll(*session)

    suspend fun updateAllSession(vararg session: Session) = sessionDao.updateAll(*session)

    suspend fun deleteSession(vararg session: Session) = sessionDao.delete(*session)

/*--------------------------------------------------------------Tag---------------------------------------------------------- */

    fun getAllTag() = tagDao.getAll()

    fun getTagsById(id: Int) = tagDao.getTagsById(id)

    suspend fun insertAllTag(vararg tag: Tag) = tagDao.insertAll(*tag)

    suspend fun updateAllTag(vararg tag: Tag) = tagDao.updateAll(*tag)

    suspend fun deleteTag(vararg tag: Tag) = tagDao.delete(*tag)

/*--------------------------------------------------------------User Stats--------------------------------------------------- */

    fun getAllUserStats() = userStatsDao.getAll()

    suspend fun updateAllUserStats(userStats: UserStats) = userStatsDao.update(userStats)

}