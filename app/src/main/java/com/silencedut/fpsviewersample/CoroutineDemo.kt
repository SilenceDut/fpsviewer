package com.silencedut.fpsviewersample

import android.util.Log
import kotlinx.coroutines.*

/**
 * @author SilenceDut
 * @date 2019-05-24
 */
class CoroutineDemo {

    fun start() {
        GlobalScope.launch(CoroutineName("hhh")) {
            val res = testIo()
            coroutineContext[Job]
        }
    }

    suspend fun testIo() : String{
        val res =  withContext(Dispatchers.IO) {
            Thread.sleep(5000)
            return@withContext "hhh"
        }
        return res
    }
}