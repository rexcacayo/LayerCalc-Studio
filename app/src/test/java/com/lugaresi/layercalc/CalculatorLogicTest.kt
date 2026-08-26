package com.lugaresi.layercalc

import com.lugaresi.layercalc.domain.CalculatorLogic
import org.junit.Assert.assertEquals
import org.junit.Test

class CalculatorLogicTest {

    @Test
    fun calculateMaxSpeed_returnsExpectedValue() {
        val result = CalculatorLogic.calculateMaxSpeed(
            maxVolumetricFlow = 18.0,
            layerHeight = 0.20,
            lineWidth = 0.45
        )

        assertEquals(200.0, result, 0.001)
    }

    @Test
    fun calculateFlowRatio_usesCurrentFlowAndAverageOfFourMeasurements() {
        val result = CalculatorLogic.calculateFlowRatio(
            currentFlowRatio = 1.0,
            theoreticalWidth = 0.45,
            wallMeasurements = listOf(0.49, 0.48, 0.48, 0.47)
        )

        assertEquals(0.938, result, 0.0001)
    }

    @Test
    fun calculatePressureAdvance_returnsExpectedValue() {
        val result = CalculatorLogic.calculatePressureAdvance(
            optimalZHeightMm = 12.5,
            stepFactor = 0.002
        )

        assertEquals(0.0250, result, 0.0001)
    }

    @Test
    fun calculateRemainingFilament_subtractsSpoolTare() {
        val result = CalculatorLogic.calculateRemainingFilament(
            grossWeightGrams = 420.0,
            spoolTareGrams = 210.0,
            densityGramsPerCm3 = 1.24
        )

        assertEquals(210.0, result.netWeightGrams, 0.001)
        assertEquals(70.4, result.meters, 0.1)
    }

    @Test
    fun calculateTotalCost_returnsFullBreakdown() {
        val result = CalculatorLogic.calculateTotalCost(
            filamentWeightG = 120.0,
            pricePerKg = 20.0,
            printTimeHours = 4.0,
            powerConsumptionW = 120.0,
            kwhPrice = 0.18,
            machineWearCostPerHour = 0.20,
            wastePercentage = 5.0
        )

        assertEquals(2.52, result.materialCost, 0.001)
        assertEquals(0.086, result.electricityCost, 0.001)
        assertEquals(0.80, result.machineCost, 0.001)
        assertEquals(3.41, result.totalCost, 0.001)
    }
}
