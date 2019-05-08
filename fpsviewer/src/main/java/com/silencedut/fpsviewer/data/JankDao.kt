package com.silencedut.fpsviewer.data

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * @author SilenceDut
 * @date 2019-05-08
 */
@Dao
interface JankDao{

    @Query("SELECT * from jank_table ORDER BY occurredTime ASC")
    suspend fun getAllJankInfos(): List<JankInfo>?

    @WorkerThread
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(jankInfo: JankInfo)

    @Query("DELETE FROM jank_table")
    fun deleteAll()
}