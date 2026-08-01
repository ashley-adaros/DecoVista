package com.decovista.planner2d.ui

/**
 * Representa un mueble colocado interactivamente en el plano.
 */
data class PlacedFurnitureUiModel(
    val id: String,
    val name: String,
    val widthMeters: Float,
    val depthMeters: Float,
    val positionX: Float,      // Posición X central en metros
    val positionY: Float,      // Posición Y central en metros
    val rotationDegrees: Float,
    val modelGlbUrl: String = "",
    val isSelected: Boolean = false,
    val hasCollision: Boolean = false,
    val isOutOfBounds: Boolean = false
)

/**
 * Representa un mueble disponible en el catálogo de la barra inferior.
 */
data class CatalogFurnitureUiModel(
    val id: String,
    val name: String,
    val category: String,
    val widthMeters: Float,
    val heightMeters: Float,
    val depthMeters: Float,
    val modelGlbUrl: String,
    val previewImageUrl: String
)

/**
 * Estado inmutable de la pantalla de planificación 2D.
 */
data class SpacePlannerUiState(
    val roomWidthMeters: Float = 6.0f,
    val roomLengthMeters: Float = 5.0f,
    val placedFurniture: List<PlacedFurnitureUiModel> = emptyList(),
    val catalogFurniture: List<CatalogFurnitureUiModel> = emptyList(),
    val selectedFurnitureId: String? = null,
    val occupiedAreaPercentage: Float = 0f
)
