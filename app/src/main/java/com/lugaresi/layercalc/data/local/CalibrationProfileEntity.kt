package com.lugaresi.layercalc.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Snapshot persistente de una calibración de filamento.
 *
 * Se guardan tanto los datos de identidad como las entradas del test. Así LayerCalc puede
 * reconstruir el estado del Dashboard y no depender únicamente de resultados derivados.
 * Inventario de bobina y coste de una pieza se mantienen fuera de esta entidad porque tienen
 * un ciclo de vida distinto al perfil de calibración.
 */
@Entity(tableName = "calibration_profiles")
data class CalibrationProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileName: String,

    val filamentManufacturer: String,
    val filamentCommercialName: String,
    val filamentVariant: String,
    val materialType: String,
    val density: Double,

    val printerManufacturer: String,
    val printerModel: String,
    val nozzleDiameter: Double,
    val extruderType: String,

    val speed: Double,
    val layerHeight: Double,
    val lineWidth: Double,
    val maxVolumetricFlow: Double,

    val currentFlowRatio: Double,
    val theoreticalWidth: Double,
    val wall1: Double,
    val wall2: Double,
    val wall3: Double,
    val wall4: Double,
    val optimalZHeight: Double,

    val calculatedFlowRatio: Double,
    val calculatedPressureAdvance: Double,
    val calculatedMaxSafeSpeed: Double,

    val createdAt: Long,
    val updatedAt: Long
)
