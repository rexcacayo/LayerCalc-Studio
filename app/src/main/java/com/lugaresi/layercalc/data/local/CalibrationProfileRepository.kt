package com.lugaresi.layercalc.data.local

/**
 * Boundary entre la UI y Room. Mantiene las operaciones de persistencia fuera del Dashboard
 * y deja abierta la sustitución por otra fuente de datos en el futuro.
 */
class CalibrationProfileRepository(
    private val dao: CalibrationProfileDao
) {
    suspend fun getProfiles(): List<CalibrationProfileEntity> = dao.getAll()

    suspend fun save(profile: CalibrationProfileEntity): Long {
        return if (profile.id == 0L) {
            dao.insert(profile)
        } else {
            dao.update(profile)
            profile.id
        }
    }

    suspend fun delete(profile: CalibrationProfileEntity) = dao.delete(profile)
}
