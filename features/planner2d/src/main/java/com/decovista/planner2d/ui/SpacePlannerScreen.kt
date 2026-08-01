package com.decovista.planner2d.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SpacePlannerScreen(
    viewModel: SpacePlannerViewModel,
    onViewInAr: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)) // Fondo oscuro premium (Slate 900)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // 1. Cabecera con Métricas de Ocupación
            HeaderBar(occupiedPercentage = uiState.occupiedAreaPercentage)

            // 2. Plano de Lienzo 2D Interactivo
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, Color(0.2f, 0.3f, 0.4f, 0.2f), RoundedCornerShape(24.dp))
            ) {
                InteractiveCanvas(
                    uiState = uiState,
                    onSelect = viewModel::selectFurnitureAt,
                    onDrag = viewModel::moveSelectedFurniture,
                    onDeselect = viewModel::clearSelection
                )

                // Barra de herramientas flotante de Rotación/Eliminación
                val selectedItem = uiState.placedFurniture.find { it.id == uiState.selectedFurnitureId }
                
                if (selectedItem != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                    ) {
                        FloatingControls(
                            onRotateClockwise = { viewModel.rotateSelectedFurniture(15f) },
                            onRotateCounterClockwise = { viewModel.rotateSelectedFurniture(-15f) },
                            onDelete = viewModel::deleteSelectedFurniture,
                            onViewInAr = {
                                selectedItem?.let { onViewInAr(it.modelGlbUrl) }
                            }
                        )
                    }
                }

                // Banner de Alerta por Colisión o Fuera de Límites
                val conflictiveItem = uiState.placedFurniture.find { it.hasCollision || it.isOutOfBounds }
                if (conflictiveItem != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 16.dp)
                    ) {
                        AlertBanner(conflictiveItem)
                    }
                }
            }

            // 3. Catálogo Deslizable Inferior (BottomSheet)
            CatalogDrawer(
                catalogItems = uiState.catalogFurniture,
                onAddItem = viewModel::addFurnitureToLayout
            )
        }
    }
}

@Composable
fun HeaderBar(occupiedPercentage: Float) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Plano de Distribución",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Arrastra y rota elementos en el plano real",
                color = Color(0xFF94A3B8),
                fontSize = 12.sp
            )
        }

        // Indicador de densidad de ocupación circular/píldora
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50.dp))
                .background(
                    if (occupiedPercentage > 60f) Color(0xFF991B1B) else Color(0xFF1E293B)
                )
                .border(
                    1.dp,
                    if (occupiedPercentage > 60f) Color(0xFFEF4444) else Color(0xFF334155),
                    RoundedCornerShape(50.dp)
                )
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Text(
                text = "Ocupado: ${String.format("%.1f", occupiedPercentage)}%",
                color = if (occupiedPercentage > 60f) Color(0xFFFCA5A5) else Color(0xFF38BDF8),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun InteractiveCanvas(
    uiState: SpacePlannerUiState,
    onSelect: (Float, Float) -> Unit,
    onDrag: (Float, Float) -> Unit,
    onDeselect: () -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)) // Slate 900
    ) {
        val viewW = constraints.maxWidth.toFloat()
        val viewH = constraints.maxHeight.toFloat()

        // Ajustar escala asegurando mantener proporción
        val scaleX = viewW / uiState.roomWidthMeters
        val scaleY = viewH / uiState.roomLengthMeters
        val scale = minOf(scaleX, scaleY) * 0.9f // Margen del 10%

        // Desplazamiento para centrar el plano en el espacio disponible
        val offsetX = (viewW - (uiState.roomWidthMeters * scale)) / 2f
        val offsetY = (viewH - (uiState.roomLengthMeters * scale)) / 2f

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            // Transformar coordenadas de pantalla a metros del plano
                            val xMeters = (offset.x - offsetX) / scale
                            val yMeters = (offset.y - offsetY) / scale
                            onSelect(xMeters, yMeters)
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            // Arrastre traducido a metros
                            val dragXMeters = dragAmount.x / scale
                            val dragYMeters = dragAmount.y / scale
                            onDrag(dragXMeters, dragYMeters)
                        },
                        onDragEnd = { }
                    )
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { onDeselect() }
                )
        ) {
            // 1. Dibujar Cuadrícula de Referencia (Paso de 0.5 metros)
            val gridStep = 0.5f
            var gx = 0f
            while (gx <= uiState.roomWidthMeters) {
                drawLine(
                    color = Color(0xFF1E293B),
                    start = Offset(offsetX + gx * scale, offsetY),
                    end = Offset(offsetX + gx * scale, offsetY + uiState.roomLengthMeters * scale),
                    strokeWidth = 1f
                )
                gx += gridStep
            }
            var gy = 0f
            while (gy <= uiState.roomLengthMeters) {
                drawLine(
                    color = Color(0xFF1E293B),
                    start = Offset(offsetX, offsetY + gy * scale),
                    end = Offset(offsetX + uiState.roomWidthMeters * scale, offsetY + gy * scale),
                    strokeWidth = 1f
                )
                gy += gridStep
            }

            // 2. Dibujar Habitación (Paredes perimetrales)
            drawRect(
                color = Color(0xFF334155), // Slate 700
                topLeft = Offset(offsetX, offsetY),
                size = Size(uiState.roomWidthMeters * scale, uiState.roomLengthMeters * scale),
                style = Stroke(width = 6f)
            )

            // 3. Dibujar Muebles Colocados
            for (item in uiState.placedFurniture) {
                val wPx = item.widthMeters * scale
                val dPx = item.depthMeters * scale
                val cxPx = offsetX + item.positionX * scale
                val cyPx = offsetY + item.positionY * scale

                // Rotar el scope del dibujo sobre el centro del mueble
                rotate(degrees = item.rotationDegrees, pivot = Offset(cxPx, cyPx)) {
                    val rectTopLeft = Offset(cxPx - wPx / 2, cyPx - dPx / 2)

                    // Color de relleno según su estado físico
                    val fillColor = when {
                        item.isSelected -> Color(0xFF38BDF8).copy(alpha = 0.25f) // Seleccionado: Celeste translúcido
                        item.hasCollision || item.isOutOfBounds -> Color(0xFFEF4444).copy(alpha = 0.25f) // Error: Rojo translúcido
                        else -> Color(0xFF64748B).copy(alpha = 0.15f) // Por defecto: Slate translúcido
                    }

                    // Color del contorno
                    val strokeColor = when {
                        item.isSelected -> Color(0xFF38BDF8) // Celeste
                        item.hasCollision || item.isOutOfBounds -> Color(0xFFEF4444) // Rojo vibrante
                        else -> Color(0xFF475569) // Gris medio
                    }

                    // Dibujar fondo sólido/translúcido
                    drawRect(
                        color = fillColor,
                        topLeft = rectTopLeft,
                        size = Size(wPx, dPx)
                    )

                    // Dibujar borde del bloque
                    drawRect(
                        color = strokeColor,
                        topLeft = rectTopLeft,
                        size = Size(wPx, dPx),
                        style = Stroke(width = if (item.isSelected) 4f else 2f)
                    )

                    // Dibujar guía visual de orientación del frente (línea blanca y flecha)
                    drawLine(
                        color = strokeColor.copy(alpha = 0.8f),
                        start = Offset(cxPx, cyPx),
                        end = Offset(cxPx, rectTopLeft.y),
                        strokeWidth = 3f
                    )
                }
            }
        }
    }
}

@Composable
fun FloatingControls(
    onRotateClockwise: () -> Unit,
    onRotateCounterClockwise: () -> Unit,
    onDelete: () -> Unit,
    onViewInAr: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xCC1E293B)), // Glassmorphism
        modifier = Modifier.border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onRotateCounterClockwise) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Rotar Izquierda",
                    tint = Color.White
                )
            }
            IconButton(onClick = onRotateClockwise) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Rotar Derecha",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Botón de Ver en AR
            Button(
                onClick = onViewInAr,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
                contentPadding = PaddingValues(horizontal = 12.dp),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color(0xFF0F172A),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text("Ver AR", color = Color(0xFF0F172A), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            IconButton(
                onClick = onDelete,
                colors = IconButtonDefaults.iconButtonColors(contentColor = Color(0xFFEF4444))
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar Mueble"
                )
            }
        }
    }
}

@Composable
fun AlertBanner(item: PlacedFurnitureUiModel) {
    val message = if (item.isOutOfBounds) {
        "${item.name} sobresale de la habitación."
    } else {
        "${item.name} colisiona con otro objeto."
    }

    Row(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF991B1B))
            .border(1.dp, Color(0xFFEF4444), RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Alerta",
            tint = Color(0xFFFCA5A5),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = message,
            color = Color(0xFFFEF2F2),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun CatalogDrawer(
    catalogItems: List<CatalogFurnitureUiModel>,
    onAddItem: (CatalogFurnitureUiModel) -> Unit
) {
    Card(
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)), // Slate 800
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp)
        ) {
            Text(
                text = "Catálogo de Muebles",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 12.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(catalogItems) { item ->
                    CatalogItemCard(item = item, onAdd = { onAddItem(item) })
                }
            }
        }
    }
}

@Composable
fun CatalogItemCard(
    item: CatalogFurnitureUiModel,
    onAdd: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)), // Slate 900
        modifier = Modifier
            .width(160.dp)
            .clickable { onAdd() }
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Placeholder de preview visual del mueble (Caja 3D simbólica)
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF334155), Color(0xFF1E293B))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "3D",
                    color = Color(0xFF38BDF8),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.name,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "${item.widthMeters}m x ${item.depthMeters}m",
                color = Color(0xFF64748B),
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
