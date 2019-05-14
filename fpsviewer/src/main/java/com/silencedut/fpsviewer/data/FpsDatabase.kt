
package com.silencedut.fpsviewer.data

import android.content.Context
import androidx.room.*

import com.silencedut.fpsviewer.FpsViewer
import com.silencedut.fpsviewer.utilities.FpsConstants.DATABASE_NAME

/**
 * The Room database for this app
 */
@Database(entities = [JankInfo::class], version = 1, exportSchema = false)
@TypeConverters(JankHashConverters::class)
abstract class FpsDatabase : RoomDatabase() {
    abstract fun jankDao(): JankDao

    companion object {

        // For Singleton instantiation
        @Volatile private var instance: FpsDatabase? = null

        fun getInstance(context: Context): FpsDatabase {

            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        // Create and pre-populate the database. See this article for more details:
        // https://medium.com/google-developers/7-pro-tips-for-room-fbadea4bfbd1#4785
        private fun buildDatabase(context: Context): FpsDatabase {
            return Room.databaseBuilder(context, FpsDatabase::class.java, DATABASE_NAME).build()
        }
    }
}
