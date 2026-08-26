package com.lugaresi.layercalc.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ThreeJsCoverBackground(
    modifier: Modifier = Modifier
) {
    var manualRotX by remember { mutableFloatStateOf(0.35f) }
    var manualRotY by remember { mutableFloatStateOf(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "3D Cinematic Engine")

    // 1. Giro constante automático
    val autoRotY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (Math.PI * 2).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "AutoRot"
    )

    // 2. Oscilación vertical del cabezal (Flotación tipo Hotend activo)
    val headBounce by infiniteTransition.animateFloat(
        initialValue = -18f,
        targetValue = 18f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "HeadBounce"
    )

    // 3. Pulso de expansión de las capas extruidas
    val layerPulse by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LayerPulse"
    )

    // 4. Trazador láser circular en tiempo real
    val laserSweep by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (Math.PI * 2).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "LaserSweep"
    )

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    manualRotY += dragAmount.x * 0.008f
                    manualRotX = (manualRotX + dragAmount.y * 0.008f).coerceIn(-0.8f, 0.8f)
                }
            }
    ) {
        val centerX = size.width / 2f
        val centerY = size.height * 0.44f
        val currentRotY = autoRotY + manualRotY
        val currentRotX = manualRotX

        fun project(x: Float, y: Float, z: Float): Offset {
            val cosY = cos(currentRotY)
            val sinY = sin(currentRotY)
            val x1 = x * cosY - z * sinY
            val z1 = z * cosY + x * sinY

            val cosX = cos(currentRotX)
            val sinX = sin(currentRotX)
            val y2 = y * cosX - z1 * sinX
            val z2 = z1 * cosX + y * sinX

            val fov = 450f
            val distance = 550f
            val scale = fov / (distance + z2)

            return Offset(
                x = centerX + x1 * scale,
                y = centerY + y2 * scale
            )
        }

        // 1. Cama Caliente (Grid naranja neón)
        val gridSize = 160f
        val step = 40f
        val bedY = 140f

        var g = -gridSize
        while (g <= gridSize) {
            val p1 = project(g, bedY, -gridSize)
            val p2 = project(g, bedY, gridSize)
            drawLine(
                color = Color(0xFFFF6D00).copy(alpha = 0.22f),
                start = p1,
                end = p2,
                strokeWidth = 1.5f
            )

            val p3 = project(-gridSize, bedY, g)
            val p4 = project(gridSize, bedY, g)
            drawLine(
                color = Color(0xFFFF6D00).copy(alpha = 0.22f),
                start = p3,
                end = p4,
                strokeWidth = 1.5f
            )
            g += step
        }

        // 2. Capas circulares pulsantes (Filamento cian)
        val layers = listOf(
            Triple(100f, 95f * layerPulse, 0.35f),
            Triple(65f, 80f * (2f - layerPulse), 0.65f),
            Triple(30f, 65f * layerPulse, 0.95f)
        )

        layers.forEach { (ly, radius, alpha) ->
            val path = Path()
            val segments = 44
            for (i in 0..segments) {
                val angle = (i.toFloat() / segments) * (Math.PI * 2).toFloat()
                val px = cos(angle) * radius
                val pz = sin(angle) * radius
                val pt = project(px, ly, pz)
                if (i == 0) path.moveTo(pt.x, pt.y) else path.lineTo(pt.x, pt.y)
            }
            drawPath(
                path = path,
                color = Color(0xFF00E5FF).copy(alpha = alpha),
                style = Stroke(width = 3.5f)
            )
        }

        // 3. Hotend Flotante
        val floatOffset = headBounce - 25f

        // Aletas del disipador metálico
        var finY = -70f
        while (finY <= -20f) {
            val path = Path()
            val segments = 24
            for (i in 0..segments) {
                val angle = (i.toFloat() / segments) * (Math.PI * 2).toFloat()
                val px = cos(angle) * 38f
                val pz = sin(angle) * 38f
                val pt = project(px, finY + floatOffset, pz)
                if (i == 0) path.moveTo(pt.x, pt.y) else path.lineTo(pt.x, pt.y)
            }
            drawPath(path, color = Color(0xFF90A4AE), style = Stroke(width = 4f))
            finY += 12f
        }

        // Bloque calefactor naranja maker
        val bw = 35f
        val bh = 22f
        val bd = 35f
        val blockY = floatOffset

        val v = listOf(
            project(-bw, blockY, -bd),
            project(bw, blockY, -bd),
            project(bw, blockY + bh, -bd),
            project(-bw, blockY + bh, -bd),
            project(-bw, blockY, bd),
            project(bw, blockY, bd),
            project(bw, blockY + bh, bd),
            project(-bw, blockY + bh, bd)
        )

        val faces = listOf(
            listOf(0, 1, 2, 3), listOf(4, 5, 6, 7),
            listOf(0, 1, 5, 4), listOf(2, 3, 7, 6),
            listOf(0, 3, 7, 4), listOf(1, 2, 6, 5)
        )

        faces.forEach { face ->
            val p = Path().apply {
                moveTo(v[face[0]].x, v[face[0]].y)
                lineTo(v[face[1]].x, v[face[1]].y)
                lineTo(v[face[2]].x, v[face[2]].y)
                lineTo(v[face[3]].x, v[face[3]].y)
                close()
            }
            drawPath(p, color = Color(0xFFFF6D00).copy(alpha = 0.88f))
            drawPath(p, color = Color(0xFFFF9E40), style = Stroke(width = 2f))
        }

        // Boquilla cónica de latón (Nozzle)
        val tipY = blockY + bh + 18f
        val tip = project(0f, tipY, 0f)
        val baseNozzle = listOf(
            project(-12f, blockY + bh, -12f),
            project(12f, blockY + bh, -12f),
            project(12f, blockY + bh, 12f),
            project(-12f, blockY + bh, 12f)
        )

        baseNozzle.forEachIndexed { index, p1 ->
            val p2 = baseNozzle[(index + 1) % 4]
            val nozzleFace = Path().apply {
                moveTo(p1.x, p1.y)
                lineTo(p2.x, p2.y)
                lineTo(tip.x, tip.y)
                close()
            }
            drawPath(nozzleFace, color = Color(0xFFFFD54F))
            drawPath(nozzleFace, color = Color(0xFFFFB300), style = Stroke(width = 1.5f))
        }

        // 4. Partícula de extrusión láser activa en la capa superior
        val laserRadius = 65f * layerPulse
        val laserX = cos(laserSweep) * laserRadius
        val laserZ = sin(laserSweep) * laserRadius
        val laserPt = project(laserX, 30f, laserZ)

        // Resplandor del punto de deposición
        drawCircle(
            color = Color(0xFF00E5FF).copy(alpha = 0.4f),
            radius = 9f,
            center = laserPt
        )
        drawCircle(
            color = Color.White,
            radius = 4.5f,
            center = laserPt
        )
    }
}