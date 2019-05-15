package com.silencedut.fpsviewer.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @author SilenceDut
 * @date 2019/5/5
 */
@Entity(tableName = "jank_table")
data class JankInfo(@PrimaryKey val occurredTime: Long, val frameCost: Int,
                    val stackWitchCount: List<Pair<String, Int>>, var resolved :Boolean = false)