package com.lugaresi.layercalc.domain

import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

enum class SlicerTarget(val label: String) {
    ORCA_SLICER("OrcaSlicer"),
    BAMBU_STUDIO("Bambu Studio")
}

data class ExportableFilamentProfile(
    val profileName: String,
    val filamentVendor: String,
    val materialType: String,
    val density: Double,
    val flowRatio: Double,
    val maxVolumetricSpeed: Double,
    val pressureAdvance: Double,
    val extruderType: String,
    val printerName: String,
    val nozzleDiameter: Double
)

data class ImportedFilamentProfile(
    val profileName: String,
    val filamentVendor: String?,
    val materialType: String?,
    val density: Double?,
    val flowRatio: Double?,
    val maxVolumetricSpeed: Double?,
    val pressureAdvance: Double?,
    val extruderType: String?,
    val printerName: String?,
    val nozzleDiameter: Double?
)

object ProfileJsonCodec {

    fun export(profile: ExportableFilamentProfile, target: SlicerTarget): String =
        when (target) {
            SlicerTarget.ORCA_SLICER -> exportOrca(profile)
            SlicerTarget.BAMBU_STUDIO -> exportBambu(profile)
        }

    fun exportFileName(profileName: String, target: SlicerTarget): String {
        val safeName = profileName
            .replace(Regex("[\\\\/:*?\"<>|]"), "-")
            .replace(Regex("\\s+"), " ")
            .trim()
            .ifBlank { "LayerCalc Profile" }
        val suffix = when (target) {
            SlicerTarget.ORCA_SLICER -> "Orca"
            SlicerTarget.BAMBU_STUDIO -> "Bambu"
        }
        return "$safeName - $suffix.json"
    }

    fun import(jsonText: String): ImportedFilamentProfile {
        val json = JSONObject(jsonText)
        require(json.optString("type", "filament") == "filament") {
            "El JSON no es un perfil de filamento."
        }

        return ImportedFilamentProfile(
            profileName = json.optString("name").ifBlank { "Perfil importado" },
            filamentVendor = firstString(json, "filament_vendor"),
            materialType = firstString(json, "filament_type"),
            density = firstDouble(json, "filament_density"),
            flowRatio = firstDouble(json, "filament_flow_ratio"),
            maxVolumetricSpeed = firstDouble(json, "filament_max_volumetric_speed"),
            pressureAdvance = firstDouble(json, "pressure_advance"),
            extruderType = json.optString("layercalc_extruder_type").takeIf { it.isNotBlank() },
            printerName = firstString(json, "compatible_printers")
                ?: json.optString("layercalc_printer").takeIf { it.isNotBlank() },
            nozzleDiameter = json.optString("layercalc_nozzle_diameter")
                .takeIf { it.isNotBlank() }
                ?.toDoubleOrNull()
        )
    }

    private fun exportOrca(profile: ExportableFilamentProfile): String {
        val json = baseProfileJson(profile).apply {
            put("from", "user")
            put("inherits", orcaBaseProfileFor(profile.materialType))
            put("filament_settings_id", arrayOfString(profile.profileName))
            put("enable_pressure_advance", arrayOfString("1"))
            put("pressure_advance", arrayOfString(format(profile.pressureAdvance, 4)))
        }
        return json.toString(2)
    }

    private fun exportBambu(profile: ExportableFilamentProfile): String {
        // Estructura de preset de usuario de Bambu Studio. Se mantiene deliberadamente
        // pequeña: hereda del material genérico y solo sobrescribe lo calibrado.
        val json = baseProfileJson(profile).apply {
            put("from", "User")
            put("inherits", bambuBaseProfileFor(profile.materialType))
            put("filament_settings_id", arrayOfString(profile.profileName))
            put("is_custom_defined", "0")

            // Bambu Studio reconoce estas claves internamente. Algunas versiones
            // pueden ocultarlas o descartarlas al volver a guardar el preset.
            put("enable_pressure_advance", arrayOfString("1"))
            put("pressure_advance", arrayOfString(format(profile.pressureAdvance, 4)))
        }
        return json.toString(2)
    }

    private fun baseProfileJson(profile: ExportableFilamentProfile): JSONObject {
        return JSONObject().apply {
            put("type", "filament")
            put("name", profile.profileName)
            put("from", "user")
            put("instantiation", "true")
            put("filament_vendor", arrayOfString(profile.filamentVendor.ifBlank { "Generic" }))
            put("filament_type", arrayOfString(profile.materialType))
            put("filament_density", arrayOfString(format(profile.density, 3)))
            put("filament_diameter", arrayOfString("1.75"))
            put("filament_flow_ratio", arrayOfString(format(profile.flowRatio, 3)))
            put("filament_max_volumetric_speed", arrayOfString(format(profile.maxVolumetricSpeed, 2)))
            put("compatible_printers", JSONArray())

            // Metadatos propios de LayerCalc. Los slicers que no los reconozcan los ignoran.
            put("layercalc_printer", profile.printerName)
            put("layercalc_nozzle_diameter", format(profile.nozzleDiameter, 2))
            put("layercalc_extruder_type", profile.extruderType)
            put("layercalc_generated_by", "LayerCalc Studio")
        }
    }

    private fun orcaBaseProfileFor(materialType: String): String = when (materialType.uppercase(Locale.US)) {
        "PLA" -> "Generic PLA @System"
        "PETG" -> "Generic PETG @System"
        "ABS" -> "Generic ABS @System"
        "ASA" -> "Generic ASA @System"
        "TPU" -> "Generic TPU @System"
        "PC" -> "Generic PC @System"
        "NYLON", "PA" -> "Generic PA @System"
        else -> "fdm_filament_common"
    }

    private fun bambuBaseProfileFor(materialType: String): String = when (materialType.uppercase(Locale.US)) {
        "PLA" -> "Generic PLA"
        "PETG" -> "Generic PETG"
        "ABS" -> "Generic ABS"
        "ASA" -> "Generic ASA"
        "TPU" -> "Generic TPU"
        "PC" -> "Generic PC"
        "NYLON", "PA" -> "Generic PA"
        else -> "Generic ${materialType.uppercase(Locale.US)}"
    }

    private fun arrayOfString(value: String): JSONArray = JSONArray().put(value)

    private fun firstString(json: JSONObject, key: String): String? {
        val value = json.opt(key) ?: return null
        return when (value) {
            is JSONArray -> if (value.length() > 0) value.optString(0).takeIf { it.isNotBlank() } else null
            is String -> value.takeIf { it.isNotBlank() }
            else -> value.toString().takeIf { it.isNotBlank() }
        }
    }

    private fun firstDouble(json: JSONObject, key: String): Double? =
        firstString(json, key)?.toDoubleOrNull()

    private fun format(value: Double, decimals: Int): String =
        String.format(Locale.US, "%.${decimals}f", value)
}
