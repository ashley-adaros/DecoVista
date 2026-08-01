package com.decovista.planner2d.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.decovista.calculator.domain.usecase.CalculateFitUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@HiltViewModel
class SpacePlannerViewModel @Inject constructor(
    private val calculateFitUseCase: CalculateFitUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SpacePlannerUiState())
    val uiState: StateFlow<SpacePlannerUiState> = _uiState.asStateFlow()

    init {
        loadCatalog()
    }

    private fun loadCatalog() {
        // Carga inicial simulada de elementos de catálogo
        _uiState.update {
            it.copy(
                catalogFurniture = listOf(
                    CatalogFurnitureUiModel("1", "Sofá Nórdico", "Salón", 2.2f, 0.9f, 0.9f, "https://example.com/sofa.glb", ""),
                    CatalogFurnitureUiModel("2", "Mesa Centro", "Salón", 1.1f, 0.4f, 0.6f, "https://example.com/table.glb", ""),
                    CatalogFurnitureUiModel("3", "Sillón Velvet", "Dormitorio", 0.8f, 1.0f, 0.8f, "https://example.com/armchair.glb", ""),
                    CatalogFurnitureUiModel("4", "Aparador Wood", "Comedor", 1.6f, 0.8f, 0.4f, "https://example.com/sideboard.glb", ""),
                    CatalogFurnitureUiModel("5", "Librería Indy", "Estudio", 0.9f, 1.9f, 0.3f, "https://example.com/shelf.glb", "")
                )
            )
        }
    }

    /**
     * Añade un nuevo mueble del catálogo al plano posicionándolo en el centro.
     */
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
            modelGlbUrl = catalogItem.modelGlbUrl,
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

    /**
     * Selecciona un mueble si el toque en coordenadas de metros coincide con su caja.
     */
    fun selectFurnitureAt(xMeters: Float, yMeters: Float) {
        var tappedId: String? = null
        
        // Iteramos de atrás hacia adelante para respetar el orden visual
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

    /**
     * Mueve las coordenadas del mueble seleccionado.
     */
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

    /**
     * Rota el mueble actualmente seleccionado un incremento en grados.
     */
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

    /**
     * Elimina el mueble seleccionado del lienzo.
     */
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

    /**
     * Deselecciona cualquier elemento activo.
     */
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
            val box = item.toBox2D()
            
            // Verificar límites físicos usando el motor central
            val isOut = CollisionDetector.isOutOfBounds(box, state.roomWidthMeters, state.roomLengthMeters)
            
            // Verificar colisiones con otros muebles colocados usando SAT
            val hasColl = list.filter { it.id != item.id }.any { other ->
                CollisionDetector.checkOverlap(box, other.toBox2D())
            }

            item.copy(isOutOfBounds = isOut, hasCollision = hasColl)
        }

        // Calcular porcentaje de ocupación del piso
        val totalRoomArea = state.roomWidthMeters * state.roomLengthMeters
        val occupiedArea = updatedList.sumOf { (it.widthMeters * it.depthMeters).toDouble() }.toFloat()
        val percentage = if (totalRoomArea > 0) (occupiedArea / totalRoomArea) * 100f else 0f

        return state.copy(
            placedFurniture = updatedList,
            occupiedAreaPercentage = percentage
        )
    }

    // --- Algoritmos Auxiliares de Colisión y Geometría ---

    private fun PlacedFurnitureUiModel.toBox2D(): Box2D = Box2D(
        centerX = positionX,
        centerY = positionY,
        width = widthMeters,
        depth = depthMeters,
        rotationDegrees = rotationDegrees
    )

    private fun isPointInsideFurniture(px: Float, py: Float, item: PlacedFurnitureUiModel): Boolean {
        // Transformar el punto de prueba al espacio local del mueble (deshacer traslación y rotación)
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
