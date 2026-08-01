package com.decovista.planner2d.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.decovista.calculator.domain.usecase.CalculateFitUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

class SpacePlannerViewModel(
    private val calculateFitUseCase: CalculateFitUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SpacePlannerUiState())
    val uiState: StateFlow<SpacePlannerUiState> = _uiState.asStateFlow()

    init {
        loadCatalog()
    }

    private fun loadCatalog() {
        _uiState.update {
            it.copy(
                catalogFurniture = listOf(
                    CatalogFurnitureUiModel("1", "Sofá Nórdico 3 Plazas", "Salón", 2.2f, 0.85f, 0.9f, "models/sofa_nordic.glb", ""),
                    CatalogFurnitureUiModel("2", "Mesa de Centro Kora", "Salón", 1.1f, 0.45f, 0.6f, "models/coffee_table.glb", ""),
                    CatalogFurnitureUiModel("3", "Sillón Orejero Velvet", "Dormitorio", 0.85f, 1.0f, 0.8f, "models/armchair.glb", ""),
                    CatalogFurnitureUiModel("4", "Aparador Vintage Wood", "Comedor", 1.6f, 0.75f, 0.45f, "models/sideboard.glb", ""),
                    CatalogFurnitureUiModel("5", "Librería Estilo Industrial", "Estudio", 0.9f, 1.9f, 0.35f, "models/bookshelf.glb", "")
                )
            )
        }
    }

    fun addFurnitureToLayout(catalogItem: CatalogFurnitureUiModel) {
        val id = UUID.randomUUID().toString()
        val defaultX = _uiState.value.roomWidthMeters / 2
        val defaultY = _uiState.value.roomLengthMeters / 2

        val newPlaced = PlacedFurnitureUiModel(
            id = id,
            name = catalogItem.name,
            widthMeters = catalogItem.widthMeters,
            depthMeters = catalogItem.depthMeters,
            positionX = defaultX,
            positionY = defaultY,
            rotationDegrees = 0f,
            isSelected = true
        )

        _uiState.update { state ->
            val updatedList = state.placedFurniture.map { it.copy(isSelected = false) } + newPlaced
            val finalState = state.copy(
                placedFurniture = updatedList,
                selectedFurnitureId = id
            )
            recalculateLayoutMetrics(finalState)
        }
    }

    fun selectFurnitureAt(xMeters: Float, yMeters: Float) {
        var tappedId: String? = null
        val list = _uiState.value.placedFurniture
        for (item in list.reversed()) {
            if (isPointInsideFurniture(xMeters, yMeters, item)) {
                tappedId = item.id
                break
            }
        }

        _uiState.update { state ->
            val updatedList = state.placedFurniture.map {
                it.copy(isSelected = it.id == tappedId)
            }
            state.copy(
                placedFurniture = updatedList,
                selectedFurnitureId = tappedId
            )
        }
    }

    fun moveSelectedFurniture(deltaXMeters: Float, deltaYMeters: Float) {
        val selectedId = _uiState.value.selectedFurnitureId ?: return
        
        _uiState.update { state ->
            val updatedList = state.placedFurniture.map { item ->
                if (item.id == selectedId) {
                    val newX = (item.positionX + deltaXMeters).coerceIn(0f, state.roomWidthMeters)
                    val newY = (item.positionY + deltaYMeters).coerceIn(0f, state.roomLengthMeters)
                    item.copy(positionX = newX, positionY = newY)
                } else {
                    item
                }
            }
            val nextState = state.copy(placedFurniture = updatedList)
            recalculateLayoutMetrics(nextState)
        }
    }

    fun rotateSelectedFurniture(degrees: Float) {
        val selectedId = _uiState.value.selectedFurnitureId ?: return
        
        _uiState.update { state ->
            val updatedList = state.placedFurniture.map { item ->
                if (item.id == selectedId) {
                    val newRotation = (item.rotationDegrees + degrees) % 360f
                    item.copy(rotationDegrees = if (newRotation < 0) newRotation + 360f else newRotation)
                } else {
                    item
                }
            }
            val nextState = state.copy(placedFurniture = updatedList)
            recalculateLayoutMetrics(nextState)
        }
    }

    fun deleteSelectedFurniture() {
        val selectedId = _uiState.value.selectedFurnitureId ?: return
        _uiState.update { state ->
            val updatedList = state.placedFurniture.filterNot { it.id == selectedId }
            val nextState = state.copy(
                placedFurniture = updatedList,
                selectedFurnitureId = null
            )
            recalculateLayoutMetrics(nextState)
        }
    }

    fun clearSelection() {
        _uiState.update { state ->
            state.copy(
                placedFurniture = state.placedFurniture.map { it.copy(isSelected = false) },
                selectedFurnitureId = null
            )
        }
    }

    // --- Algoritmos Auxiliares de Colisión y Geometría ---

    private fun recalculateLayoutMetrics(state: SpacePlannerUiState): SpacePlannerUiState {
        val list = state.placedFurniture
        val updatedList = list.map { item ->
            val isOut = checkOutOfBounds(item, state.roomWidthMeters, state.roomLengthMeters)
            val hasColl = list.filter { it.id != item.id }.any { other ->
                checkOverlapOBB(item, other)
            }
            item.copy(isOutOfBounds = isOut, hasCollision = hasColl)
        }

        val totalRoomArea = state.roomWidthMeters * state.roomLengthMeters
        val occupiedArea = updatedList.sumOf { (it.widthMeters * it.depthMeters).toDouble() }.toFloat()
        val percentage = if (totalRoomArea > 0) (occupiedArea / totalRoomArea) * 100f else 0f

        return state.copy(
            placedFurniture = updatedList,
            occupiedAreaPercentage = percentage
        )
    }

    private fun checkOutOfBounds(item: PlacedFurnitureUiModel, roomW: Float, roomL: Float): Boolean {
        val bounds = getVertices(item)
        return bounds.any { it.x < 0f || it.x > roomW || it.y < 0f || it.y > roomL }
    }

    private fun checkOverlapOBB(box1: PlacedFurnitureUiModel, box2: PlacedFurnitureUiModel): Boolean {
        val vertices1 = getVertices(box1)
        val vertices2 = getVertices(box2)

        val axes = arrayOf(
            getNormalAxis(vertices1[0], vertices1[1]),
            getNormalAxis(vertices1[1], vertices1[2]),
            getNormalAxis(vertices2[0], vertices2[1]),
            getNormalAxis(vertices2[1], vertices2[2])
        )

        for (axis in axes) {
            val proj1 = projectToAxis(vertices1, axis)
            val proj2 = projectToAxis(vertices2, axis)

            if (proj1.second < proj2.first || proj2.second < proj1.first) {
                return false
            }
        }
        return true
    }

    private data class Vector2D(val x: Float, val y: Float)

    private fun getVertices(item: PlacedFurnitureUiModel): List<Vector2D> {
        val halfW = item.widthMeters / 2
        val halfD = item.depthMeters / 2
        
        val localVertices = listOf(
            Vector2D(-halfW, -halfD),
            Vector2D(halfW, -halfD),
            Vector2D(halfW, halfD),
            Vector2D(-halfW, halfD)
        )

        val rad = Math.toRadians(item.rotationDegrees.toDouble())
        val cosA = cos(rad).toFloat()
        val sinA = sin(rad).toFloat()

        return localVertices.map { v ->
            Vector2D(
                x = item.positionX + (v.x * cosA - v.y * sinA),
                y = item.positionY + (v.x * sinA + v.y * cosA)
            )
        }
    }

    private fun getNormalAxis(v1: Vector2D, v2: Vector2D): Vector2D {
        val edgeX = v2.x - v1.x
        val edgeY = v2.y - v1.y
        val len = Math.hypot(edgeX.toDouble(), edgeY.toDouble()).toFloat()
        return if (len > 0f) Vector2D(-edgeY / len, edgeX / len) else Vector2D(0f, 0f)
    }

    private fun projectToAxis(vertices: List<Vector2D>, axis: Vector2D): Pair<Float, Float> {
        var min = Float.MAX_VALUE
        var max = -Float.MAX_VALUE
        for (v in vertices) {
            val dot = v.x * axis.x + v.y * axis.y
            if (dot < min) min = dot
            if (dot > max) max = dot
        }
        return Pair(min, max)
    }

    private fun isPointInsideFurniture(px: Float, py: Float, item: PlacedFurnitureUiModel): Boolean {
        val dx = px - item.positionX
        val dy = py - item.positionY
        
        val rad = Math.toRadians(-item.rotationDegrees.toDouble())
        val cosA = cos(rad).toFloat()
        val sinA = sin(rad).toFloat()

        val localX = dx * cosA - dy * sinA
        val localY = dx * sinA + dy * cosA

        return abs(localX) <= item.widthMeters / 2 && abs(localY) <= item.depthMeters / 2
    }
}

/**
 * Fábrica nativa de Android para instanciar el ViewModel inyectándole el caso de uso.
 */
class SpacePlannerViewModelFactory(
    private val calculateFitUseCase: CalculateFitUseCase
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SpacePlannerViewModel::class.java)) {
            return SpacePlannerViewModel(calculateFitUseCase) as T
        }
        throw IllegalArgumentException("Clase ViewModel desconocida")
    }
}
