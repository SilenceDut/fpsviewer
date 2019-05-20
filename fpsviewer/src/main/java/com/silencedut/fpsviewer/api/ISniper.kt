package com.silencedut.fpsviewer.api

import com.silencedut.fpsviewer.transfer.ITransfer

/**
 * @author SilenceDut
 * @date 2019-05-20
 */
interface ISniper : ITransfer{
    fun appendSection(sectionName:String)
    fun removeSection(sectionName:String)
}