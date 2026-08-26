package com.lugaresi.layercalc.domain

import kotlin.math.PI
import kotlin.math.round

/**
 * Motor de cálculo de LayerCalc Studio.
 *
 * Todas las funciones trabajan con las unidades indicadas en sus parámetros.
 * La UI debe limitarse a recoger datos, llamar a estas funciones y mostrar resultados.
 */
object CalculatorLogic {

    private const val FILAMENT_DIAMETER_MM = 1.75
    private const val FILAMENT_VOLUME_CM3_PER_METER = 2.405

    data class FilamentResult(
        val netWeightGrams: Double,
        val meters: Double
    )

    data class CostBreakdown(
        val materialCost: Double,
        val electricityCost: Double,
        val machineCost: Double,
        val totalCost: Double
    )

    // -------------------------------------------------------------------------
    // 1. VELOCIDAD MÁXIMA POR CAUDAL
    // v_max = Q_max / (h * w)
    // -------------------------------------------------------------------------

    fun calculateMaxSpeed(
        maxVolumetricFlow: Double,
        layerHeight: Double,
        lineWidth: Double
    ): Double {
        if (maxVolumetricFlow < 0.0 || layerHeight <= 0.0 || lineWidth <= 0.0) {
            return 0.0
        }

        return (maxVolumetricFlow / (layerHeight * lineWidth)).roundTo(2)
    }

    /**
     * Compatibilidad temporal con DashboardScreen.
     * Calcula Q = velocidad * altura de capa * ancho de línea.
     */
    fun calculateVolumetricFlow(
        speed: Double,
        layerHeight: Double,
        lineWidth: Double
    ): Double {
        if (speed < 0.0 || layerHeight <= 0.0 || lineWidth <= 0.0) {
            return 0.0
        }

        return (speed * layerHeight * lineWidth).roundTo(3)
    }

    /** Compatibilidad temporal con DashboardScreen. */
    fun solveSpeedForFlow(
        flow: Double,
        layerHeight: Double,
        lineWidth: Double
    ): Double = calculateMaxSpeed(flow, layerHeight, lineWidth)

    /** Compatibilidad temporal con la versión actual del proyecto. */
    fun solveHeightForFlow(
        flow: Double,
        speed: Double,
        lineWidth: Double
    ): Double {
        if (flow < 0.0 || speed <= 0.0 || lineWidth <= 0.0) {
            return 0.0
        }

        return (flow / (speed * lineWidth)).roundTo(3)
    }

    // -------------------------------------------------------------------------
    // 2. FLOW RATIO
    // flujo_nuevo = flujo_actual * (w_teorico / w_medido_promedio)
    // -------------------------------------------------------------------------

    fun calculateFlowRatio(
        currentFlowRatio: Double,
        theoreticalWidth: Double,
        wallMeasurements: List<Double>
    ): Double {
        if (
            currentFlowRatio <= 0.0 ||
            theoreticalWidth <= 0.0 ||
            wallMeasurements.size != 4 ||
            wallMeasurements.any { it <= 0.0 }
        ) {
            return 0.0
        }

        val measuredAverage = wallMeasurements.average()
        val newFlowRatio = currentFlowRatio * (theoreticalWidth / measuredAverage)

        return newFlowRatio.roundTo(3)
    }

    /**
     * Compatibilidad temporal con DashboardScreen.
     * La UI actual solo entrega una medida y asume flow actual = 1.0.
     * Se sustituirá por la versión de cuatro medidas cuando editemos la pantalla.
     */
    fun calculateFlowRatio(actualWidth: Double, targetWidth: Double): Double {
        if (actualWidth <= 0.0 || targetWidth <= 0.0) {
            return 0.0
        }

        return (targetWidth / actualWidth).roundTo(3)
    }

    // -------------------------------------------------------------------------
    // 3. PRESSURE ADVANCE
    // k_pa = Z_optima * factor_paso
    // -------------------------------------------------------------------------

    fun calculatePressureAdvance(
        optimalZHeightMm: Double,
        stepFactor: Double
    ): Double {
        if (optimalZHeightMm < 0.0 || stepFactor <= 0.0) {
            return 0.0
        }

        return (optimalZHeightMm * stepFactor).roundTo(4)
    }

    /**
     * Compatibilidad temporal con una firma antigua del proyecto.
     * No representa la fórmula final y no debe usarse en la nueva UI.
     */
    @Deprecated(
        message = "Usa calculatePressureAdvance(optimalZHeightMm, stepFactor)",
        replaceWith = ReplaceWith("calculatePressureAdvance(targetLength, speedDiff)")
    )
    fun calculatePressureAdvance(
        targetLength: Double,
        actualLength: Double,
        speedDiff: Double
    ): Double {
        // Mantener compilación hasta que DashboardScreen elimine el estado antiguo.
        // actualLength se conserva únicamente por compatibilidad de firma.
        @Suppress("UNUSED_VARIABLE")
        val ignoredActualLength = actualLength
        return calculatePressureAdvance(targetLength, speedDiff)
    }

    // -------------------------------------------------------------------------
    // 4. FILAMENTO RESTANTE
    // peso_neto = peso_bruto - tara
    // metros = peso_neto / (densidad * 2.405)
    // -------------------------------------------------------------------------

    fun calculateRemainingFilament(
        grossWeightGrams: Double,
        spoolTareGrams: Double,
        densityGramsPerCm3: Double
    ): FilamentResult {
        if (grossWeightGrams < 0.0 || spoolTareGrams < 0.0 || densityGramsPerCm3 <= 0.0) {
            return FilamentResult(netWeightGrams = 0.0, meters = 0.0)
        }

        val netWeight = (grossWeightGrams - spoolTareGrams).coerceAtLeast(0.0)
        val meters = netWeight / (densityGramsPerCm3 * FILAMENT_VOLUME_CM3_PER_METER)

        return FilamentResult(
            netWeightGrams = netWeight.roundTo(1),
            meters = meters.roundTo(1)
        )
    }

    /**
     * Compatibilidad temporal con DashboardScreen.
     * Esta función interpreta weight como peso NETO, porque la UI actual no dispone de tara.
     */
    fun calculateFilamentLength(
        weight: Double,
        density: Double,
        diameter: Double = FILAMENT_DIAMETER_MM
    ): Double {
        if (weight < 0.0 || density <= 0.0 || diameter <= 0.0) {
            return 0.0
        }

        val areaMm2 = PI * (diameter / 2.0) * (diameter / 2.0)
        val cm3PerMeter = areaMm2
        val meters = weight / (density * cm3PerMeter)

        return meters.roundTo(1)
    }

    /** Compatibilidad temporal con la versión actual del proyecto. */
    fun solveWeightForLength(
        lengthM: Double,
        density: Double,
        diameter: Double = FILAMENT_DIAMETER_MM
    ): Double {
        if (lengthM < 0.0 || density <= 0.0 || diameter <= 0.0) {
            return 0.0
        }

        val areaMm2 = PI * (diameter / 2.0) * (diameter / 2.0)
        val weight = lengthM * density * areaMm2

        return weight.roundTo(1)
    }

    // -------------------------------------------------------------------------
    // 5. COSTE TOTAL
    // material + electricidad + desgaste
    // -------------------------------------------------------------------------

    fun calculateTotalCost(
        filamentWeightG: Double,
        pricePerKg: Double,
        printTimeHours: Double,
        powerConsumptionW: Double,
        kwhPrice: Double,
        machineWearCostPerHour: Double,
        wastePercentage: Double
    ): CostBreakdown {
        if (
            filamentWeightG < 0.0 ||
            pricePerKg < 0.0 ||
            printTimeHours < 0.0 ||
            powerConsumptionW < 0.0 ||
            kwhPrice < 0.0 ||
            machineWearCostPerHour < 0.0 ||
            wastePercentage < 0.0
        ) {
            return CostBreakdown(0.0, 0.0, 0.0, 0.0)
        }

        val wasteFactor = 1.0 + (wastePercentage / 100.0)
        val materialCost = (filamentWeightG / 1000.0) * pricePerKg * wasteFactor
        val electricityCost = (powerConsumptionW * printTimeHours / 1000.0) * kwhPrice
        val machineCost = printTimeHours * machineWearCostPerHour
        val totalCost = materialCost + electricityCost + machineCost

        return CostBreakdown(
            materialCost = materialCost.roundTo(2),
            electricityCost = electricityCost.roundTo(3),
            machineCost = machineCost.roundTo(2),
            totalCost = totalCost.roundTo(2)
        )
    }

    /**
     * Compatibilidad temporal con DashboardScreen.
     * Mantiene la firma de 5 parámetros para que la pantalla siga compilando.
     * Internamente ya aplica los valores por defecto acordados: 0.20 €/h y 5 % de merma.
     */
    fun calculateTotalCost(
        filamentWeightG: Double,
        pricePerKg: Double,
        printTimeHours: Double,
        powerConsumptionW: Double,
        kwhPrice: Double
    ): Double = calculateTotalCost(
        filamentWeightG = filamentWeightG,
        pricePerKg = pricePerKg,
        printTimeHours = printTimeHours,
        powerConsumptionW = powerConsumptionW,
        kwhPrice = kwhPrice,
        machineWearCostPerHour = 0.20,
        wastePercentage = 5.0
    ).totalCost

    // -------------------------------------------------------------------------
    // UTILIDAD DE REDONDEO
    // -------------------------------------------------------------------------

    private fun Double.roundTo(decimals: Int): Double {
        val factor = 10.0.pow(decimals)
        return round(this * factor) / factor
    }

    private fun Double.pow(exponent: Int): Double {
        var result = 1.0
        repeat(exponent) {
            result *= this
        }
        return result
    }
}
