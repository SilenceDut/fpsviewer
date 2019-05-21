package com.silencedut.fpsviewer.data

import android.arch.persistence.room.*
import android.support.annotation.WorkerThread

/**
 * @author SilenceDut
 * @date 2019-05-08
 */
@Dao
interface JankDao{

    @Query("SELECT * from jank_table ORDER BY occurredTime ASC")
    fun getAllJankInfos(): List<JankInfo>?

    @WorkerThread
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(jankInfo: JankInfo)

    @Query("DELETE FROM jank_table WHERE occurredTime =:jankId")
    fun delete(jankId : Long)

    @Update
    fun update(jankInfo: JankInfo)

}