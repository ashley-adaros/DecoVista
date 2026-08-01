package com.decovista.core.arengine

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun ArSceneViewComposable(
    modelUrl: String,
    onModelLoaded: () -> Unit = {},
    onPlaneDetected: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var planeDetected by remember { mutableStateOf(false) }
    var scale by remember { mutableStateOf(1f) }
    var rotation by remember { mutableStateOf(0f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // Simular detección de planos asíncrona
    LaunchedEffect(Unit) {
        delay(2000) // 2 segundos buscando plano
        planeDetected = true
        onPlaneDetected()
        onModelLoaded()
    }

    val state = rememberTransformableState { zoomChange, _, rotationChange ->
        // Bloquear zoom (escala) para mantener dimensiones físicas reales de la app
        rotation += rotationChange
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1E293B)) // Simula el fondo de la cámara activa
    ) {
        val width = constraints.maxWidth.toFloat()
        val height = constraints.maxHeight.toFloat()
        
        // Centrar posición inicial
        if (offset == Offset.Zero) {
            offset = Offset(width / 2f, height / 2f)
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .transformable(state = state)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offset = Offset(offset.x + dragAmount.x, offset.y + dragAmount.y)
                    }
                }
        ) {
            // 1. Dibujar Rejilla 3D perspectivada en el suelo
            if (planeDetected) {
                val gridPath = Path().apply {
                    val horizonY = height * 0.6f
                    var i = -4
                    while (i <= 4) {
                        // Líneas de perspectiva que van del horizonte hacia la pantalla
                        moveTo(width / 2f + i * (width / 12f), horizonY)
                        lineTo(width / 2f + i * (width / 3f), height)
                        i++
                    }
                    // Líneas horizontales del plano
                    var j = 0
                    while (j <= 5) {
                        val currY = horizonY + (height - horizonY) * (j / 5f)
                        val currW = width * (j / 5f)
                        moveTo(width / 2f - currW / 2f, currY)
                        lineTo(width / 2f + currW / 2f, currY)
                        j++
                    }
                }
                drawPath(
                    path = gridPath,
                    color = Color(0xFF38BDF8).copy(alpha = 0.4f),
                    style = Stroke(width = 2f)
                )
            }

            // 2. Dibujar Módulo de Mueble 3D Simulado perspectivado (un prisma en perspectiva isométrica)
            if (planeDetected) {
                rotate(degrees = rotation, pivot = offset) {
                    val size = 180f // Tamaño base del mueble en píxeles

                    // Dibujar caras del prisma isométrico
                    val pLeft = Offset(offset.x - size / 2f, offset.y + size / 4f)
                    val pRight = Offset(offset.x + size / 2f, offset.y + size / 4f)
                    val pTop = Offset(offset.x, offset.y - size / 4f)
                    val pBottom = Offset(offset.x, offset.y + size / 2f)
                    val pTopLeft = Offset(offset.x - size / 2f, offset.y - size / 2f)
                    val pTopRight = Offset(offset.x + size / 2f, offset.y - size / 2f)
                    val pTopCenter = Offset(offset.x, offset.y - size * 0.75f)

                    // Cara Izquierda
                    val pathLeft = Path().apply {
                        moveTo(pLeft.x, pLeft.y)
                        lineTo(pBottom.x, pBottom.y)
                        lineTo(offset.x, offset.y)
                        lineTo(pTopLeft.x, pTopLeft.y)
                        close()
                    }
                    drawPath(pathLeft, Color(0xFF38BDF8).copy(alpha = 0.5f))
                    drawPath(pathLeft, Color(0xFF0284C7), style = Stroke(width = 3f))

                    // Cara Derecha
                    val pathRight = Path().apply {
                        moveTo(pBottom.x, pBottom.y)
                        lineTo(pRight.x, pRight.y)
                        lineTo(pTopRight.x, pTopRight.y)
                        lineTo(offset.x, offset.y)
                        close()
                    }
                    drawPath(pathRight, Color(0xFF0284C7).copy(alpha = 0.6f))
                    drawPath(pathRight, Color(0xFF0284C7), style = Stroke(width = 3f))

                    // Cara Superior (Tapa)
                    val pathTop = Path().apply {
                        moveTo(pTopLeft.x, pTopLeft.y)
                        lineTo(offset.x, offset.y)
                        lineTo(pTopRight.x, pTopRight.y)
                        lineTo(pTopCenter.x, pTopCenter.y)
                        close()
                    }
                    drawPath(pathTop, Color(0xFF7DD3FC).copy(alpha = 0.7f))
                    drawPath(pathTop, Color(0xFF0284C7), style = Stroke(width = 3f))
                }
            }
        }

        // Indicador de estado de cámara
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .background(Color(0xCC0F172A), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = "Modo: Realidad Aumentada",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
