package com.lugaresi.layercalc.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface CalibrationProfileDao {
    @Query("SELECT * FROM calibration_profiles ORDER BY updatedAt DESC")
    suspend fun getAll(): List<CalibrationProfileEntity>

    @Query("SELECT * FROM calibration_profiles WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): CalibrationProfileEntity?

    @Insert
    suspend fun insert(profile: CalibrationProfileEntity): Long

    @Update
    suspend fun update(profile: CalibrationProfileEntity)

    @Delete
    suspend fun delete(profile: CalibrationProfileEntity)
}
