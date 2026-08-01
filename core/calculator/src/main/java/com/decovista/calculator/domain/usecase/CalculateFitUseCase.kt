package com.decovista.calculator.domain.usecase

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * Representa los estados resultantes de validar la colocación de un mueble.
 */
sealed interface FitStatus {
    /**
     * El mueble cabe perfectamente y hay suficiente espacio libre.
     */
    data class Fits(
        val occupiedAreaPercentage: Float,
        val message: String
    ) : FitStatus

    /**
     * El mueble cabe físicamente, pero está muy cerca de una pared (menos de 15 cm) 
     * o la habitación está muy congestionada (más del 60% de ocupación).
     */
    data class TightFit(
        val occupiedAreaPercentage: Float,
        val message: String
    ) : FitStatus

    /**
     * El mueble sobresale de los límites físicos de la habitación.
     */
    data class ExceedsBounds(
        val message: String
    ) : FitStatus
}

/**
 * Parámetros del mueble actual que se desea evaluar.
 */
data class TargetFurnitureInput(
    val widthMeters: Float,
    val heightMeters: Float,
    val depthMeters: Float,
    val positionX: Float,      // Centro del mueble en X
    val positionY: Float,      // Centro del mueble en Y
    val rotationDegrees: Float // Rotación del mueble en el plano horizontal (0 - 360)
)

/**
 * Parámetros de la habitación física.
 */
data class RoomDimensionsInput(
    val widthMeters: Float,
    val lengthMeters: Float
)

/**
 * Muebles que ya están ubicados en la habitación (para cálculo de áreas).
 */
data class ExistingFurnitureFootprint(
    val widthMeters: Float,
    val depthMeters: Float
)

/**
 * Caso de Uso responsable de verificar la viabilidad espacial de colocación de un mueble
 * y estimar la densidad de ocupación del piso de la habitación.
 */
class CalculateFitUseCase {

    companion object {
        // Tolerancia mínima para alertar sobre espacio estrecho (15 centímetros)
        private const val TIGHT_PROXIMITY_THRESHOLD_METERS = 0.15f
        // Umbral crítico de ocupación de área de piso (60%)
        private const val CRITICAL_OCCUPATION_PERCENTAGE = 60.0f
    }

    /**
     * Evalúa el encaje físico y de densidad de ocupación del mueble.
     *
     * @param target Mueble que se intenta posicionar.
     * @param room Dimensiones de la habitación.
     * @param existingFurniture Lista de dimensiones de los muebles que ya están en el espacio (excluyendo el actual).
     */
    operator fun invoke(
        target: TargetFurnitureInput,
        room: RoomDimensionsInput,
        existingFurniture: List<ExistingFurnitureFootprint>
    ): FitStatus {
        // 1. Calcular límites proyectados considerando la rotación (Bounding Box 2D)
        val rotationRadians = Math.toRadians(target.rotationDegrees.toDouble())
        val cosRad = abs(cos(rotationRadians))
        val sinRad = abs(sin(rotationRadians))

        val halfWidthProjected = 0.5f * (target.widthMeters * cosRad + target.depthMeters * sinRad).toFloat()
        val halfDepthProjected = 0.5f * (target.widthMeters * sinRad + target.depthMeters * cosRad).toFloat()

        val furnitureMinX = target.positionX - halfWidthProjected
        val furnitureMaxX = target.positionX + halfWidthProjected
        val furnitureMinY = target.positionY - halfDepthProjected
        val furnitureMaxY = target.positionY + halfDepthProjected

        // 2. Comprobar si excede los límites de la habitación (0.0 a RoomDimension)
        val exceedsX = furnitureMinX < 0f || furnitureMaxX > room.widthMeters
        val exceedsY = furnitureMinY < 0f || furnitureMaxY > room.lengthMeters

        if (exceedsX || exceedsY) {
            return FitStatus.ExceedsBounds(
                message = "El mueble sobresale de los límites de la habitación. Posición no permitida."
            )
        }

        // 3. Calcular áreas y densidad de ocupación
        val roomArea = room.widthMeters * room.lengthMeters
        if (roomArea <= 0f) {
            return FitStatus.ExceedsBounds(message = "Dimensiones de la habitación inválidas.")
        }

        val targetFootprintArea = target.widthMeters * target.depthMeters
        val existingFootprintArea = existingFurniture.sumOf { (it.widthMeters * it.depthMeters).toDouble() }.toFloat()
        
        val totalOccupiedArea = existingFootprintArea + targetFootprintArea
        val occupiedAreaPercentage = (totalOccupiedArea / roomArea) * 100f

        // 4. Evaluar proximidad crítica a las paredes
        val distanceToLeftWall = furnitureMinX
        val distanceToRightWall = room.widthMeters - furnitureMaxX
        val distanceToBottomWall = furnitureMinY
        val distanceToTopWall = room.lengthMeters - furnitureMaxY

        val isCloseToAnyWall = distanceToLeftWall < TIGHT_PROXIMITY_THRESHOLD_METERS ||
                distanceToRightWall < TIGHT_PROXIMITY_THRESHOLD_METERS ||
                distanceToBottomWall < TIGHT_PROXIMITY_THRESHOLD_METERS ||
                distanceToTopWall < TIGHT_PROXIMITY_THRESHOLD_METERS

        // 5. Determinar FitStatus y emitir alertas claras
        return when {
            isCloseToAnyWall -> {
                FitStatus.TightFit(
                    occupiedAreaPercentage = occupiedAreaPercentage,
                    message = "El mueble cabe, pero está a menos de ${TIGHT_PROXIMITY_THRESHOLD_METERS * 100} cm de una pared. Podría dificultar el paso."
                )
            }
            occupiedAreaPercentage > CRITICAL_OCCUPATION_PERCENTAGE -> {
                FitStatus.TightFit(
                    occupiedAreaPercentage = occupiedAreaPercentage,
                    message = "Cuidado: La densidad de muebles es alta. Se ha ocupado el ${String.format("%.1f", occupiedAreaPercentage)}% del espacio del suelo."
                )
            }
            else -> {
                FitStatus.Fits(
                    occupiedAreaPercentage = occupiedAreaPercentage,
                    message = "El mueble cabe perfectamente. Ocupación total de suelo: ${String.format("%.1f", occupiedAreaPercentage)}%."
                )
            }
        }
    }
}
