package com.silencedut.fpsviewer.sniper

/**
 * @author SilenceDut
 * @date 2019/5/5
 */
data class JankInfo(val occurredTime: Long, val frameCost: Int, val jankPoint: Int, val stackWitchCount: List<Pair<String, Int>>)