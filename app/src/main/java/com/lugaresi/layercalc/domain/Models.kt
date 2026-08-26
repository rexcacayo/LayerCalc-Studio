package com.lugaresi.layercalc.domain

/**
 * Material de impresión y densidad usada para estimar longitud de filamento.
 * La densidad se expresa en g/cm³.
 */
data class PrintMaterial(
    val name: String,
    val density: Double
)

/**
 * Materiales disponibles por defecto en LayerCalc Studio.
 * Se mantiene como lista para conservar compatibilidad con DashboardScreen.
 */
val DefaultMaterials = listOf(
    PrintMaterial("PLA", 1.24),
    PrintMaterial("PETG", 1.27),
    PrintMaterial("ABS", 1.04),
    PrintMaterial("ASA", 1.07),
    PrintMaterial("TPU", 1.21),
    PrintMaterial("PC", 1.20),
    PrintMaterial("Nylon", 1.08)
)

/**
 * Presets de tara para carretes vacíos.
 * El peso se expresa en gramos.
 */
enum class SpoolType(
    val label: String,
    val tareGrams: Double
) {
    PLASTIC_STANDARD("Plástico estándar", 200.0),
    CARDBOARD("Cartón", 140.0),
    HEAVY_PLASTIC("Plástico pesado", 250.0),
    CUSTOM("Personalizado", 200.0)
}

/**
 * Tipo de sistema de extrusión usado para calcular Pressure Advance.
 * stepFactor es el factor de paso del test de torre.
 */
enum class ExtruderType(
    val label: String,
    val stepFactor: Double
) {
    DIRECT_DRIVE("Direct Drive", 0.002),
    BOWDEN("Bowden", 0.02)
}

/**
 * Identifica el filamento físico que se está calibrando.
 * No contiene datos dependientes de una impresora concreta.
 */
data class FilamentProfile(
    val manufacturer: String,
    val commercialName: String,
    val variant: String = "",
    val material: PrintMaterial
) {
    /** Nombre legible del filamento, por ejemplo: "eSun PLA+ Black". */
    val displayName: String
        get() = listOf(manufacturer, commercialName, variant)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .joinToString(" ")
}

/**
 * Describe la impresora y la configuración física usada en la calibración.
 * La boquilla se expresa en milímetros.
 */
data class PrinterProfile(
    val manufacturer: String,
    val model: String,
    val nozzleDiameter: Double,
    val extruderType: ExtruderType
) {
    /** Nombre legible de la impresora, por ejemplo: "Flashforge AD5M Pro 0.4". */
    val displayName: String
        get() = listOf(
            manufacturer.trim(),
            model.trim(),
            formatNozzleDiameter(nozzleDiameter)
        )
            .filter { it.isNotEmpty() }
            .joinToString(" ")

    private fun formatNozzleDiameter(value: Double): String {
        val text = if (value % 1.0 == 0.0) {
            value.toInt().toString()
        } else {
            value.toString().trimEnd('0').trimEnd('.')
        }
        return "$text mm"
    }
}

/**
 * Une un filamento físico con una impresora concreta para formar un perfil
 * de calibración exportable posteriormente a OrcaSlicer o Bambu Studio.
 *
 * Los parámetros calculados (Flow Ratio, PA, caudal, etc.) se incorporarán
 * en una fase posterior para no duplicar todavía la lógica del Dashboard.
 */
data class CalibrationProfile(
    val filament: FilamentProfile,
    val printer: PrinterProfile,
    val baseProfile: String,
    val customName: String = ""
) {
    /**
     * Nombre recomendado para el perfil.
     * Ejemplo: "eSun PLA+ Black @ Flashforge AD5M Pro 0.4 mm".
     * Si el usuario escribe customName, se respeta ese nombre.
     */
    val displayName: String
        get() = customName.trim().ifEmpty {
            "${filament.displayName} @ ${printer.displayName}"
        }
}

