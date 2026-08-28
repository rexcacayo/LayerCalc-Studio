package com.lugaresi.layercalc.ui.screens

import android.content.ClipData
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lugaresi.layercalc.data.local.CalibrationProfileEntity
import com.lugaresi.layercalc.data.local.CalibrationProfileRepository
import com.lugaresi.layercalc.data.local.LayerCalcDatabase
import com.lugaresi.layercalc.domain.CalculatorLogic
import com.lugaresi.layercalc.domain.DefaultMaterials
import com.lugaresi.layercalc.domain.ExportableFilamentProfile
import com.lugaresi.layercalc.domain.ProfileJsonCodec
import com.lugaresi.layercalc.domain.SlicerTarget
import com.lugaresi.layercalc.domain.ExtruderType
import com.lugaresi.layercalc.domain.SpoolType
import com.lugaresi.layercalc.ui.components.BentoCard
import com.lugaresi.layercalc.ui.components.BentoInput
import com.lugaresi.layercalc.ui.components.MetricValue
import com.lugaresi.layercalc.ui.theme.LayerCalcStudioTheme
import java.io.File
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen() {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val profileRepository = remember(context) {
        CalibrationProfileRepository(
            LayerCalcDatabase.getInstance(context).calibrationProfileDao()
        )
    }
    var savedProfiles by remember { mutableStateOf<List<CalibrationProfileEntity>>(emptyList()) }
    var currentSavedProfile by remember { mutableStateOf<CalibrationProfileEntity?>(null) }
    var showProfilesDialog by remember { mutableStateOf(false) }
    var profilePendingDelete by remember { mutableStateOf<CalibrationProfileEntity?>(null) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showExportActionDialog by remember { mutableStateOf(false) }
    var pendingExportJson by remember { mutableStateOf<String?>(null) }
    var pendingExportTarget by remember { mutableStateOf<SlicerTarget?>(null) }
    var pendingExportFileName by remember { mutableStateOf<String?>(null) }

    val createJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        val json = pendingExportJson
        if (uri != null && json != null) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { writer ->
                    writer.write(json)
                } ?: error("No se pudo abrir el archivo de destino.")
            }.onSuccess {
                Toast.makeText(
                    context,
                    "Perfil ${pendingExportTarget?.label ?: "JSON"} exportado",
                    Toast.LENGTH_SHORT
                ).show()
            }.onFailure { error ->
                Toast.makeText(context, "Error al exportar: ${error.message}", Toast.LENGTH_LONG).show()
            }
        }
        pendingExportJson = null
        pendingExportTarget = null
        pendingExportFileName = null
    }

    // ---------------------------------------------------------------------
    // PERFIL / IMPRESIÓN
    // ---------------------------------------------------------------------
    var speed by remember { mutableStateOf("60") }
    var layerHeight by remember { mutableStateOf("0.20") }
    var lineWidth by remember { mutableStateOf("0.45") }
    var maxVolumetricFlow by remember { mutableStateOf("18.0") }

    // Identificación del perfil
    var filamentManufacturer by remember { mutableStateOf("") }
    var filamentCommercialName by remember { mutableStateOf("") }
    var filamentVariant by remember { mutableStateOf("") }
    var printerManufacturer by remember { mutableStateOf("") }
    var printerModel by remember { mutableStateOf("") }
    var nozzleDiameter by remember { mutableStateOf("0.4") }
    var showNozzleMenu by remember { mutableStateOf(false) }
    var showProfileMaterialMenu by remember { mutableStateOf(false) }

    // ---------------------------------------------------------------------
    // FLOW RATIO
    // ---------------------------------------------------------------------
    var currentFlowRatio by remember { mutableStateOf("1.000") }
    var theoreticalWidth by remember { mutableStateOf("0.45") }
    var wall1 by remember { mutableStateOf("0.49") }
    var wall2 by remember { mutableStateOf("0.48") }
    var wall3 by remember { mutableStateOf("0.48") }
    var wall4 by remember { mutableStateOf("0.47") }

    // ---------------------------------------------------------------------
    // PRESSURE ADVANCE
    // ---------------------------------------------------------------------
    var optimalZHeight by remember { mutableStateOf("12.5") }
    var selectedExtruder by remember { mutableStateOf(ExtruderType.DIRECT_DRIVE) }
    var showExtruderMenu by remember { mutableStateOf(false) }

    // ---------------------------------------------------------------------
    // FILAMENTO
    // ---------------------------------------------------------------------
    var selectedMaterial by remember { mutableStateOf(DefaultMaterials[0]) }
    var showMaterialMenu by remember { mutableStateOf(false) }
    var grossSpoolWeight by remember { mutableStateOf("420") }
    var spoolTare by remember { mutableStateOf("210") }
    var selectedSpoolType by remember { mutableStateOf(SpoolType.CUSTOM) }
    var showSpoolMenu by remember { mutableStateOf(false) }

    // ---------------------------------------------------------------------
    // COSTES
    // ---------------------------------------------------------------------
    var partWeightG by remember { mutableStateOf("120") }
    var pricePerKg by remember { mutableStateOf("20.0") }
    var printTimeHours by remember { mutableStateOf("4") }
    var powerW by remember { mutableStateOf("120") }
    var kwhPrice by remember { mutableStateOf("0.18") }
    var machineWearPerHour by remember { mutableStateOf("0.20") }
    var wastePercentage by remember { mutableStateOf("5") }

    // ---------------------------------------------------------------------
    // CÁLCULOS REACTIVOS
    // ---------------------------------------------------------------------
    val currentFlow = remember(speed, layerHeight, lineWidth) {
        CalculatorLogic.calculateVolumetricFlow(
            speed = speed.toDoubleOrNull() ?: 0.0,
            layerHeight = layerHeight.toDoubleOrNull() ?: 0.0,
            lineWidth = lineWidth.toDoubleOrNull() ?: 0.0
        )
    }

    val maxSafeSpeed = remember(maxVolumetricFlow, layerHeight, lineWidth) {
        CalculatorLogic.calculateMaxSpeed(
            maxVolumetricFlow = maxVolumetricFlow.toDoubleOrNull() ?: 0.0,
            layerHeight = layerHeight.toDoubleOrNull() ?: 0.0,
            lineWidth = lineWidth.toDoubleOrNull() ?: 0.0
        )
    }

    val flowRatio = remember(
        currentFlowRatio,
        theoreticalWidth,
        wall1,
        wall2,
        wall3,
        wall4
    ) {
        CalculatorLogic.calculateFlowRatio(
            currentFlowRatio = currentFlowRatio.toDoubleOrNull() ?: 0.0,
            theoreticalWidth = theoreticalWidth.toDoubleOrNull() ?: 0.0,
            wallMeasurements = listOf(
                wall1.toDoubleOrNull() ?: 0.0,
                wall2.toDoubleOrNull() ?: 0.0,
                wall3.toDoubleOrNull() ?: 0.0,
                wall4.toDoubleOrNull() ?: 0.0
            )
        )
    }

    val pressureAdvance = remember(optimalZHeight, selectedExtruder) {
        CalculatorLogic.calculatePressureAdvance(
            optimalZHeightMm = optimalZHeight.toDoubleOrNull() ?: 0.0,
            stepFactor = selectedExtruder.stepFactor
        )
    }

    val remainingFilament = remember(grossSpoolWeight, spoolTare, selectedMaterial) {
        CalculatorLogic.calculateRemainingFilament(
            grossWeightGrams = grossSpoolWeight.toDoubleOrNull() ?: 0.0,
            spoolTareGrams = spoolTare.toDoubleOrNull() ?: 0.0,
            densityGramsPerCm3 = selectedMaterial.density
        )
    }

    val costBreakdown = remember(
        partWeightG,
        pricePerKg,
        printTimeHours,
        powerW,
        kwhPrice,
        machineWearPerHour,
        wastePercentage
    ) {
        CalculatorLogic.calculateTotalCost(
            filamentWeightG = partWeightG.toDoubleOrNull() ?: 0.0,
            pricePerKg = pricePerKg.toDoubleOrNull() ?: 0.0,
            printTimeHours = printTimeHours.toDoubleOrNull() ?: 0.0,
            powerConsumptionW = powerW.toDoubleOrNull() ?: 0.0,
            kwhPrice = kwhPrice.toDoubleOrNull() ?: 0.0,
            machineWearCostPerHour = machineWearPerHour.toDoubleOrNull() ?: 0.0,
            wastePercentage = wastePercentage.toDoubleOrNull() ?: 0.0
        )
    }

    val maxFlowValue = maxVolumetricFlow.toDoubleOrNull() ?: 0.0

    val filamentCoreName = filamentCommercialName.trim().ifBlank { selectedMaterial.name }

    val filamentDisplayName = listOf(
        filamentManufacturer.trim(),
        filamentCoreName,
        filamentVariant.trim()
    ).filter { it.isNotBlank() }.joinToString(" ")

    val printerDisplayName = listOf(
        printerManufacturer.trim(),
        printerModel.trim()
    ).filter { it.isNotBlank() }.joinToString(" ")

    val generatedProfileName = buildString {
        append(if (filamentDisplayName.isBlank()) selectedMaterial.name else filamentDisplayName)
        if (printerDisplayName.isNotBlank()) append(" @ $printerDisplayName")
        if (nozzleDiameter.isNotBlank()) append(" ${nozzleDiameter.trim()} mm")
    }

    val exportableProfile = ExportableFilamentProfile(
        profileName = generatedProfileName,
        filamentVendor = filamentManufacturer.trim(),
        materialType = selectedMaterial.name,
        density = selectedMaterial.density,
        flowRatio = flowRatio,
        maxVolumetricSpeed = maxFlowValue,
        pressureAdvance = pressureAdvance,
        extruderType = selectedExtruder.label,
        printerName = printerDisplayName,
        nozzleDiameter = nozzleDiameter.toDoubleOrNull() ?: 0.4
    )

    fun prepareExport(target: SlicerTarget) {
        pendingExportTarget = target
        pendingExportJson = ProfileJsonCodec.export(exportableProfile, target)
        pendingExportFileName = ProfileJsonCodec.exportFileName(generatedProfileName, target)
        showExportDialog = false
        showExportActionDialog = true
    }

    fun sharePendingJson() {
        val json = pendingExportJson ?: return
        val fileName = pendingExportFileName ?: return
        runCatching {
            val shareDir = File(context.cacheDir, "shared_profiles").apply { mkdirs() }
            val file = File(shareDir, fileName).apply { writeText(json) }
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_SUBJECT, fileName.removeSuffix(".json"))
                putExtra(Intent.EXTRA_STREAM, uri)
                clipData = ClipData.newRawUri(fileName, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(
                Intent.createChooser(shareIntent, "Compartir perfil JSON")
            )
        }.onFailure { error ->
            Toast.makeText(context, "Error al compartir: ${error.message}", Toast.LENGTH_LONG).show()
        }
    }

    val importJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            runCatching {
                val jsonText = context.contentResolver.openInputStream(uri)
                    ?.bufferedReader()
                    ?.use { it.readText() }
                    ?: error("No se pudo leer el archivo.")
                ProfileJsonCodec.import(jsonText)
            }.onSuccess { imported ->
                imported.materialType?.let { materialName ->
                    DefaultMaterials.firstOrNull { it.name.equals(materialName, ignoreCase = true) }
                        ?.let { selectedMaterial = it }
                }
                imported.filamentVendor?.let { filamentManufacturer = it }
                imported.flowRatio?.let { importedFlow ->
                    currentFlowRatio = formatUs(importedFlow, 3)
                    // El JSON contiene el Flow Ratio final, no las cuatro mediciones del test.
                    // Igualamos las paredes al ancho teórico para conservar exactamente ese valor.
                    wall1 = theoreticalWidth
                    wall2 = theoreticalWidth
                    wall3 = theoreticalWidth
                    wall4 = theoreticalWidth
                }
                imported.maxVolumetricSpeed?.let { maxVolumetricFlow = formatUs(it, 2) }
                imported.extruderType?.let { label ->
                    ExtruderType.values().firstOrNull { it.label.equals(label, ignoreCase = true) }
                        ?.let { selectedExtruder = it }
                }
                imported.pressureAdvance?.let { pa ->
                    if (selectedExtruder.stepFactor > 0.0) {
                        optimalZHeight = formatUs(pa / selectedExtruder.stepFactor, 2)
                    }
                }
                imported.printerName?.let { printer ->
                    printerModel = printer
                }
                imported.nozzleDiameter?.let { nozzle ->
                    nozzleDiameter = formatUs(nozzle, 2).trimEnd('0').trimEnd('.')
                }

                val vendor = imported.filamentVendor.orEmpty().trim()
                val leftName = imported.profileName.substringBefore("@").trim()
                filamentCommercialName = leftName.removePrefix(vendor).trim()

                currentSavedProfile = null
                Toast.makeText(context, "Perfil importado en LayerCalc", Toast.LENGTH_SHORT).show()
            }.onFailure { error ->
                Toast.makeText(context, "No se pudo importar: ${error.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun refreshSavedProfiles() {
        scope.launch {
            savedProfiles = runCatching { profileRepository.getProfiles() }
                .getOrElse {
                    Toast.makeText(context, "No se pudieron cargar los perfiles: ${it.message}", Toast.LENGTH_LONG).show()
                    emptyList()
                }
        }
    }

    fun saveCurrentProfile(asNew: Boolean = false) {
        if (printerModel.isBlank()) {
            Toast.makeText(context, "Añade el modelo de impresora antes de guardar.", Toast.LENGTH_SHORT).show()
            return
        }

        val now = System.currentTimeMillis()
        val existing = if (asNew) null else currentSavedProfile
        val entity = CalibrationProfileEntity(
            id = existing?.id ?: 0L,
            profileName = generatedProfileName,
            filamentManufacturer = filamentManufacturer.trim(),
            filamentCommercialName = filamentCommercialName.trim(),
            filamentVariant = filamentVariant.trim(),
            materialType = selectedMaterial.name,
            density = selectedMaterial.density,
            printerManufacturer = printerManufacturer.trim(),
            printerModel = printerModel.trim(),
            nozzleDiameter = nozzleDiameter.toDoubleOrNull() ?: 0.4,
            extruderType = selectedExtruder.name,
            speed = speed.toDoubleOrNull() ?: 0.0,
            layerHeight = layerHeight.toDoubleOrNull() ?: 0.0,
            lineWidth = lineWidth.toDoubleOrNull() ?: 0.0,
            maxVolumetricFlow = maxFlowValue,
            currentFlowRatio = currentFlowRatio.toDoubleOrNull() ?: 0.0,
            theoreticalWidth = theoreticalWidth.toDoubleOrNull() ?: 0.0,
            wall1 = wall1.toDoubleOrNull() ?: 0.0,
            wall2 = wall2.toDoubleOrNull() ?: 0.0,
            wall3 = wall3.toDoubleOrNull() ?: 0.0,
            wall4 = wall4.toDoubleOrNull() ?: 0.0,
            optimalZHeight = optimalZHeight.toDoubleOrNull() ?: 0.0,
            calculatedFlowRatio = flowRatio,
            calculatedPressureAdvance = pressureAdvance,
            calculatedMaxSafeSpeed = maxSafeSpeed,
            createdAt = existing?.createdAt ?: now,
            updatedAt = now
        )

        scope.launch {
            runCatching { profileRepository.save(entity) }
                .onSuccess { savedId ->
                    currentSavedProfile = entity.copy(id = savedId)
                    savedProfiles = profileRepository.getProfiles()
                    Toast.makeText(
                        context,
                        if (existing == null) "Perfil guardado en el dispositivo" else "Perfil actualizado",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .onFailure { error ->
                    Toast.makeText(context, "No se pudo guardar: ${error.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    fun loadSavedProfile(profile: CalibrationProfileEntity) {
        filamentManufacturer = profile.filamentManufacturer
        filamentCommercialName = profile.filamentCommercialName
        filamentVariant = profile.filamentVariant
        DefaultMaterials.firstOrNull { it.name.equals(profile.materialType, ignoreCase = true) }
            ?.let { selectedMaterial = it }
        printerManufacturer = profile.printerManufacturer
        printerModel = profile.printerModel
        nozzleDiameter = formatUs(profile.nozzleDiameter, 2).trimEnd('0').trimEnd('.')
        ExtruderType.values().firstOrNull { it.name == profile.extruderType }
            ?.let { selectedExtruder = it }
        speed = formatUs(profile.speed, 2).trimEnd('0').trimEnd('.')
        layerHeight = formatUs(profile.layerHeight, 2)
        lineWidth = formatUs(profile.lineWidth, 2)
        maxVolumetricFlow = formatUs(profile.maxVolumetricFlow, 2).trimEnd('0').trimEnd('.')
        currentFlowRatio = formatUs(profile.currentFlowRatio, 3)
        theoreticalWidth = formatUs(profile.theoreticalWidth, 2)
        wall1 = formatUs(profile.wall1, 3)
        wall2 = formatUs(profile.wall2, 3)
        wall3 = formatUs(profile.wall3, 3)
        wall4 = formatUs(profile.wall4, 3)
        optimalZHeight = formatUs(profile.optimalZHeight, 2).trimEnd('0').trimEnd('.')
        currentSavedProfile = profile
        showProfilesDialog = false
        Toast.makeText(context, "Perfil cargado", Toast.LENGTH_SHORT).show()
    }

    fun deleteSavedProfile(profile: CalibrationProfileEntity) {
        scope.launch {
            runCatching { profileRepository.delete(profile) }
                .onSuccess {
                    if (currentSavedProfile?.id == profile.id) currentSavedProfile = null
                    savedProfiles = profileRepository.getProfiles()
                    Toast.makeText(context, "Perfil eliminado", Toast.LENGTH_SHORT).show()
                }
                .onFailure { error ->
                    Toast.makeText(context, "No se pudo eliminar: ${error.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    LaunchedEffect(Unit) {
        savedProfiles = runCatching { profileRepository.getProfiles() }.getOrDefault(emptyList())
    }

    val flowProgress = if (maxFlowValue > 0.0) {
        (currentFlow / maxFlowValue).toFloat().coerceIn(0f, 1f)
    } else {
        0f
    }

    val orcaProfileText = buildString {
        appendLine("LAYERCALC STUDIO - PERFIL DE CALIBRACIÓN")
        appendLine()
        appendLine("Perfil: $generatedProfileName")
        appendLine("Filamento: ${if (filamentDisplayName.isBlank()) selectedMaterial.name else filamentDisplayName}")
        appendLine("Material: ${selectedMaterial.name}")
        appendLine("Densidad: ${formatUs(selectedMaterial.density, 2)} g/cm³")
        appendLine("Impresora: ${if (printerDisplayName.isBlank()) "No definida" else printerDisplayName}")
        appendLine("Boquilla: ${nozzleDiameter.ifBlank { "No definida" }} mm")
        appendLine()
        appendLine("[PARÁMETROS ORCASLICER]")
        appendLine("Flow Ratio: ${formatUs(flowRatio, 3)}")
        appendLine("Pressure Advance: ${formatUs(pressureAdvance, 4)}")
        appendLine("Max Volumetric Flow: ${formatUs(maxFlowValue, 1)} mm³/s")
        appendLine("Max Speed: ${formatUs(maxSafeSpeed, 1)} mm/s")
        appendLine("Layer Height: ${formatUs(layerHeight.toDoubleOrNull() ?: 0.0, 2)} mm")
        appendLine("Line Width: ${formatUs(lineWidth.toDoubleOrNull() ?: 0.0, 2)} mm")
        appendLine()
        appendLine("[FILAMENTO]")
        appendLine("Disponible: ${formatUs(remainingFilament.netWeightGrams, 1)} g")
        appendLine("Longitud aproximada: ${formatUs(remainingFilament.meters, 1)} m")
        appendLine()
        appendLine("[COSTE]")
        appendLine("Material: ${formatUs(costBreakdown.materialCost, 2)} EUR")
        appendLine("Electricidad: ${formatUs(costBreakdown.electricityCost, 3)} EUR")
        appendLine("Máquina: ${formatUs(costBreakdown.machineCost, 2)} EUR")
        append("Total: ${formatUs(costBreakdown.totalCost, 2)} EUR")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "LAYERCALC STUDIO",
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // -----------------------------------------------------------------
            // 1. IDENTIFICACIÓN DE FILAMENTO + IMPRESORA
            // -----------------------------------------------------------------
            BentoCard(
                title = "IMPRESORA Y PERFIL",
                subtitle = "Identifica el filamento y la máquina donde se calibra",
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "FILAMENTO",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    BentoInput(
                        value = filamentManufacturer,
                        onValueChange = { filamentManufacturer = it },
                        label = "Fabricante",
                        keyboardType = KeyboardType.Text
                    )

                    DropdownSelector(
                        label = "Tipo de material",
                        value = selectedMaterial.name,
                        expanded = showProfileMaterialMenu,
                        onExpandedChange = { showProfileMaterialMenu = it },
                        onDismiss = { showProfileMaterialMenu = false }
                    ) {
                        DefaultMaterials.forEach { material ->
                            DropdownMenuItem(
                                text = { Text("${material.name} · ${formatUs(material.density, 2)} g/cm³") },
                                onClick = {
                                    selectedMaterial = material
                                    showProfileMaterialMenu = false
                                }
                            )
                        }
                    }

                    BentoInput(
                        value = filamentCommercialName,
                        onValueChange = { filamentCommercialName = it },
                        label = "Gama / nombre comercial (opcional)",
                        keyboardType = KeyboardType.Text
                    )

                    BentoInput(
                        value = filamentVariant,
                        onValueChange = { filamentVariant = it },
                        label = "Variante / color (opcional)",
                        keyboardType = KeyboardType.Text
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                    Text(
                        text = "IMPRESORA",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        BentoInput(
                            value = printerManufacturer,
                            onValueChange = { printerManufacturer = it },
                            label = "Fabricante",
                            keyboardType = KeyboardType.Text,
                            modifier = Modifier.weight(1f)
                        )
                        BentoInput(
                            value = printerModel,
                            onValueChange = { printerModel = it },
                            label = "Modelo",
                            keyboardType = KeyboardType.Text,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    DropdownSelector(
                        label = "Boquilla",
                        value = "${nozzleDiameter.ifBlank { "0.4" }} mm",
                        expanded = showNozzleMenu,
                        onExpandedChange = { showNozzleMenu = it },
                        onDismiss = { showNozzleMenu = false }
                    ) {
                        listOf("0.2", "0.4", "0.6", "0.8").forEach { nozzle ->
                            DropdownMenuItem(
                                text = { Text("$nozzle mm") },
                                onClick = {
                                    nozzleDiameter = nozzle
                                    showNozzleMenu = false
                                }
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                    Text(
                        text = "PERFIL GENERADO",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                    )
                    Text(
                        text = generatedProfileName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            // -----------------------------------------------------------------
            // 2. PERFIL DE IMPRESIÓN / CAUDAL
            // -----------------------------------------------------------------
            BentoCard(
                title = "PERFIL DE IMPRESIÓN",
                subtitle = "Geometría usada para calcular el caudal",
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        BentoInput(
                            value = speed,
                            onValueChange = { speed = it },
                            label = "Velocidad",
                            suffix = "mm/s",
                            modifier = Modifier.weight(1f)
                        )
                        BentoInput(
                            value = layerHeight,
                            onValueChange = { layerHeight = it },
                            label = "Altura capa",
                            suffix = "mm",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        BentoInput(
                            value = lineWidth,
                            onValueChange = { lineWidth = it },
                            label = "Ancho línea",
                            suffix = "mm",
                            modifier = Modifier.weight(1f)
                        )
                        BentoInput(
                            value = maxVolumetricFlow,
                            onValueChange = { maxVolumetricFlow = it },
                            label = "Caudal máx.",
                            suffix = "mm³/s",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            BentoCard(
                title = "TELEMETRÍA DE FLUJO",
                subtitle = "Caudal actual frente al máximo del hotend",
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetricValue(
                        value = formatUs(currentFlow, 2),
                        unit = "mm³/s",
                        color = if (maxFlowValue > 0.0 && currentFlow > maxFlowValue) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.secondary
                        }
                    )
                    LinearProgressIndicator(
                        progress = { flowProgress },
                        modifier = Modifier.fillMaxWidth(),
                        color = if (maxFlowValue > 0.0 && currentFlow > maxFlowValue) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.secondary
                        }
                    )
                    Text(
                        text = "Límite calculado: ${formatUs(maxSafeSpeed, 1)} mm/s",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            // -----------------------------------------------------------------
            // 2. FLOW RATIO
            // -----------------------------------------------------------------
            BentoCard(
                title = "FLOW RATIO",
                subtitle = "Media de cuatro paredes medidas con calibre",
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    MetricValue(
                        value = formatUs(flowRatio, 3),
                        unit = "ratio",
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        BentoInput(
                            value = currentFlowRatio,
                            onValueChange = { currentFlowRatio = it },
                            label = "Flujo actual",
                            modifier = Modifier.weight(1f)
                        )
                        BentoInput(
                            value = theoreticalWidth,
                            onValueChange = { theoreticalWidth = it },
                            label = "Ancho teórico",
                            suffix = "mm",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        BentoInput(
                            value = wall1,
                            onValueChange = { wall1 = it },
                            label = "Pared 1",
                            suffix = "mm",
                            modifier = Modifier.weight(1f)
                        )
                        BentoInput(
                            value = wall2,
                            onValueChange = { wall2 = it },
                            label = "Pared 2",
                            suffix = "mm",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        BentoInput(
                            value = wall3,
                            onValueChange = { wall3 = it },
                            label = "Pared 3",
                            suffix = "mm",
                            modifier = Modifier.weight(1f)
                        )
                        BentoInput(
                            value = wall4,
                            onValueChange = { wall4 = it },
                            label = "Pared 4",
                            suffix = "mm",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // -----------------------------------------------------------------
            // 3. PRESSURE ADVANCE
            // -----------------------------------------------------------------
            BentoCard(
                title = "PRESSURE ADVANCE",
                subtitle = "Tower Test · altura Z × factor de paso",
                modifier = Modifier.fillMaxWidth(),
                icon = {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    MetricValue(
                        value = formatUs(pressureAdvance, 4),
                        unit = "s",
                        color = MaterialTheme.colorScheme.primary
                    )
                    BentoInput(
                        value = optimalZHeight,
                        onValueChange = { optimalZHeight = it },
                        label = "Altura Z óptima",
                        suffix = "mm"
                    )
                    DropdownSelector(
                        label = "Extrusor",
                        value = selectedExtruder.label,
                        expanded = showExtruderMenu,
                        onExpandedChange = { showExtruderMenu = it },
                        onDismiss = { showExtruderMenu = false }
                    ) {
                        ExtruderType.values().forEach { extruder ->
                            DropdownMenuItem(
                                text = {
                                    Text("${extruder.label} · paso ${formatUs(extruder.stepFactor, 3)}")
                                },
                                onClick = {
                                    selectedExtruder = extruder
                                    showExtruderMenu = false
                                }
                            )
                        }
                    }
                }
            }

            // -----------------------------------------------------------------
            // 4. FILAMENTO RESTANTE
            // -----------------------------------------------------------------
            BentoCard(
                title = "FILAMENTO RESTANTE",
                subtitle = "Peso bruto menos tara del carrete",
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        MetricValue(
                            value = formatUs(remainingFilament.netWeightGrams, 1),
                            unit = "g",
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        MetricValue(
                            value = formatUs(remainingFilament.meters, 1),
                            unit = "m",
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }

                    DropdownSelector(
                        label = "Material",
                        value = "${selectedMaterial.name} · ${formatUs(selectedMaterial.density, 2)} g/cm³",
                        expanded = showMaterialMenu,
                        onExpandedChange = { showMaterialMenu = it },
                        onDismiss = { showMaterialMenu = false }
                    ) {
                        DefaultMaterials.forEach { material ->
                            DropdownMenuItem(
                                text = { Text("${material.name} · ${formatUs(material.density, 2)} g/cm³") },
                                onClick = {
                                    selectedMaterial = material
                                    showMaterialMenu = false
                                }
                            )
                        }
                    }

                    DropdownSelector(
                        label = "Tipo de carrete",
                        value = if (selectedSpoolType == SpoolType.CUSTOM) {
                            "Personalizado"
                        } else {
                            "${selectedSpoolType.label} · ${formatUs(selectedSpoolType.tareGrams, 0)} g"
                        },
                        expanded = showSpoolMenu,
                        onExpandedChange = { showSpoolMenu = it },
                        onDismiss = { showSpoolMenu = false }
                    ) {
                        SpoolType.values().forEach { spoolType ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        if (spoolType == SpoolType.CUSTOM) {
                                            spoolType.label
                                        } else {
                                            "${spoolType.label} · ${formatUs(spoolType.tareGrams, 0)} g"
                                        }
                                    )
                                },
                                onClick = {
                                    selectedSpoolType = spoolType
                                    if (spoolType != SpoolType.CUSTOM) {
                                        spoolTare = formatUs(spoolType.tareGrams, 0)
                                    }
                                    showSpoolMenu = false
                                }
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        BentoInput(
                            value = grossSpoolWeight,
                            onValueChange = { grossSpoolWeight = it },
                            label = "Peso báscula",
                            suffix = "g",
                            modifier = Modifier.weight(1f)
                        )
                        BentoInput(
                            value = spoolTare,
                            onValueChange = {
                                spoolTare = it
                                selectedSpoolType = SpoolType.CUSTOM
                            },
                            label = "Tara",
                            suffix = "g",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // -----------------------------------------------------------------
            // 5. COSTE TOTAL
            // -----------------------------------------------------------------
            BentoCard(
                title = "COSTE DE LA PIEZA",
                subtitle = "Material + electricidad + desgaste",
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    MetricValue(
                        value = formatUs(costBreakdown.totalCost, 2),
                        unit = "EUR",
                        color = MaterialTheme.colorScheme.secondary
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        BentoInput(
                            value = partWeightG,
                            onValueChange = { partWeightG = it },
                            label = "Peso pieza",
                            suffix = "g",
                            modifier = Modifier.weight(1f)
                        )
                        BentoInput(
                            value = pricePerKg,
                            onValueChange = { pricePerKg = it },
                            label = "Precio",
                            suffix = "€/kg",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        BentoInput(
                            value = printTimeHours,
                            onValueChange = { printTimeHours = it },
                            label = "Tiempo",
                            suffix = "h",
                            modifier = Modifier.weight(1f)
                        )
                        BentoInput(
                            value = powerW,
                            onValueChange = { powerW = it },
                            label = "Potencia",
                            suffix = "W",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        BentoInput(
                            value = kwhPrice,
                            onValueChange = { kwhPrice = it },
                            label = "Tarifa luz",
                            suffix = "€/kWh",
                            modifier = Modifier.weight(1f)
                        )
                        BentoInput(
                            value = machineWearPerHour,
                            onValueChange = { machineWearPerHour = it },
                            label = "Desgaste",
                            suffix = "€/h",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    BentoInput(
                        value = wastePercentage,
                        onValueChange = { wastePercentage = it },
                        label = "Merma / fallos",
                        suffix = "%"
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                    CostRow("Material", costBreakdown.materialCost, 2)
                    CostRow("Electricidad", costBreakdown.electricityCost, 3)
                    CostRow("Máquina", costBreakdown.machineCost, 2)
                }
            }

            // -----------------------------------------------------------------
            // ACCIONES DE PERFIL
            // -----------------------------------------------------------------
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { saveCurrentProfile() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (currentSavedProfile == null) "GUARDAR PERFIL" else "ACTUALIZAR PERFIL")
                }

                if (currentSavedProfile != null) {
                    OutlinedButton(
                        onClick = { saveCurrentProfile(asNew = true) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("GUARDAR COMO NUEVO")
                    }
                }

                OutlinedButton(
                    onClick = {
                        refreshSavedProfiles()
                        showProfilesDialog = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("MIS PERFILES (${savedProfiles.size})")
                }

                Button(
                    onClick = { showExportDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("EXPORTAR PERFIL")
                }

                OutlinedButton(
                    onClick = { importJsonLauncher.launch(arrayOf("application/json", "text/plain")) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("IMPORTAR PERFIL JSON")
                }

                OutlinedButton(
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "LayerCalc Studio - Perfil de calibración")
                            putExtra(Intent.EXTRA_TEXT, orcaProfileText)
                        }
                        context.startActivity(
                            Intent.createChooser(shareIntent, "Compartir ficha de LayerCalc Studio")
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("COMPARTIR FICHA")
                }
            }

            if (showProfilesDialog) {
                AlertDialog(
                    onDismissRequest = { showProfilesDialog = false },
                    title = { Text("Mis perfiles") },
                    text = {
                        if (savedProfiles.isEmpty()) {
                            Text("Todavía no hay perfiles guardados en este dispositivo.")
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 420.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(savedProfiles, key = { it.id }) { profile ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                                MaterialTheme.shapes.medium
                                            )
                                            .padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            profile.profileName,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        Text(
                                            "${profile.materialType} · Flow ${formatUs(profile.calculatedFlowRatio, 3)} · PA ${formatUs(profile.calculatedPressureAdvance, 4)} · ${formatUs(profile.maxVolumetricFlow, 1)} mm³/s",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                onClick = { loadSavedProfile(profile) },
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("CARGAR")
                                            }
                                            TextButton(
                                                onClick = { profilePendingDelete = profile },
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("ELIMINAR")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showProfilesDialog = false }) {
                            Text("CERRAR")
                        }
                    }
                )
            }

            profilePendingDelete?.let { profile ->
                AlertDialog(
                    onDismissRequest = { profilePendingDelete = null },
                    title = { Text("Eliminar perfil") },
                    text = { Text("¿Quieres eliminar '${profile.profileName}' del almacenamiento local?") },
                    confirmButton = {
                        Button(
                            onClick = {
                                profilePendingDelete = null
                                deleteSavedProfile(profile)
                            }
                        ) {
                            Text("ELIMINAR")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { profilePendingDelete = null }) {
                            Text("CANCELAR")
                        }
                    }
                )
            }

            if (showExportDialog) {
                AlertDialog(
                    onDismissRequest = { showExportDialog = false },
                    title = { Text("Exportar perfil") },
                    text = {
                        Text(
                            "Elige el formato. LayerCalc generará un archivo JSON para importarlo en el slicer."
                        )
                    },
                    confirmButton = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { prepareExport(SlicerTarget.ORCA_SLICER) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("ORCASLICER")
                            }
                            OutlinedButton(
                                onClick = { prepareExport(SlicerTarget.BAMBU_STUDIO) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("BAMBU STUDIO")
                            }
                            OutlinedButton(
                                onClick = { showExportDialog = false },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("CANCELAR")
                            }
                        }
                    },
                    dismissButton = {}
                )
            }


            if (showExportActionDialog) {
                AlertDialog(
                    onDismissRequest = { showExportActionDialog = false },
                    title = { Text("Perfil ${pendingExportTarget?.label ?: "JSON"} listo") },
                    text = {
                        Text(
                            "¿Qué quieres hacer con ${pendingExportFileName ?: "el archivo JSON"}?"
                        )
                    },
                    confirmButton = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    showExportActionDialog = false
                                    createJsonLauncher.launch(
                                        pendingExportFileName ?: "LayerCalc_Profile.json"
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("GUARDAR JSON")
                            }
                            OutlinedButton(
                                onClick = {
                                    showExportActionDialog = false
                                    sharePendingJson()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("COMPARTIR JSON")
                            }
                            OutlinedButton(
                                onClick = {
                                    showExportActionDialog = false
                                    pendingExportJson = null
                                    pendingExportTarget = null
                                    pendingExportFileName = null
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("CANCELAR")
                            }
                        }
                    },
                    dismissButton = {}
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun DropdownSelector(
    label: String,
    value: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = label.uppercase(Locale.US),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            fontSize = 10.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { onExpandedChange(true) }
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = onDismiss
            ) {
                content()
            }
        }
    }
}

@Composable
private fun CostRow(label: String, value: Double, decimals: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = "${formatUs(value, decimals)} €",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun formatUs(value: Double, decimals: Int): String =
    String.format(Locale.US, "%.${decimals}f", value)

@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    LayerCalcStudioTheme {
        DashboardScreen()
    }
}

