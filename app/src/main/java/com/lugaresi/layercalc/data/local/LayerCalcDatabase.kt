package com.lugaresi.layercalc.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [CalibrationProfileEntity::class],
    version = 1,
    exportSchema = true
)
abstract class LayerCalcDatabase : RoomDatabase() {
    abstract fun calibrationProfileDao(): CalibrationProfileDao

    companion object {
        @Volatile
        private var instance: LayerCalcDatabase? = null

        fun getInstance(context: Context): LayerCalcDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    LayerCalcDatabase::class.java,
                    "layercalc.db"
                ).build().also { instance = it }
            }
    }
}