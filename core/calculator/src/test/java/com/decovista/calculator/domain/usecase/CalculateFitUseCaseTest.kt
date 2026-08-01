package com.decovista.calculator.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculateFitUseCaseTest {

    private val useCase = CalculateFitUseCase()

    @Test
    fun testFurnitureFitsPerfecty_returnsFits() {
        // Habitación de 6x5 metros
        val room = RoomDimensionsInput(widthMeters = 6.0f, lengthMeters = 5.0f)
        
        // Sofá de 2.0x0.9 en el centro (3.0, 2.5) sin rotar
        val target = TargetFurnitureInput(
            widthMeters = 2.0f,
            heightMeters = 0.85f,
            depthMeters = 0.9f,
            positionX = 3.0f,
            positionY = 2.5f,
            rotationDegrees = 0f
        )
        
        val status = useCase(target, room, emptyList())
        
        assertTrue("Debería ser un estado Fits", status is FitStatus.Fits)
        val fitsResult = status as FitStatus.Fits
        // Área del sofá = 2.0 * 0.9 = 1.8. Área habitación = 30.0. Porcentaje = (1.8/30)*100 = 6.0%
        assertEquals(6.0f, fitsResult.occupiedAreaPercentage, 0.01f)
    }

    @Test
    fun testFurnitureExceedsBounds_returnsExceedsBounds() {
        val room = RoomDimensionsInput(widthMeters = 6.0f, lengthMeters = 5.0f)
        
        // Mueble posicionado fuera de los límites (X=6.5)
        val target = TargetFurnitureInput(
            widthMeters = 2.0f,
            heightMeters = 0.85f,
            depthMeters = 0.9f,
            positionX = 6.5f,
            positionY = 2.5f,
            rotationDegrees = 0f
        )
        
        val status = useCase(target, room, emptyList())
        
        assertTrue("Debería exceder los límites", status is FitStatus.ExceedsBounds)
    }

    @Test
    fun testFurnitureIsCloseToWall_returnsTightFit() {
        val room = RoomDimensionsInput(widthMeters = 6.0f, lengthMeters = 5.0f)
        
        // Mesa muy cerca de la pared izquierda (X=1.1, ancho=2.0) -> minX = 1.1 - 1.0 = 0.1m (10cm, menor a 15cm)
        val target = TargetFurnitureInput(
            widthMeters = 2.0f,
            heightMeters = 0.75f,
            depthMeters = 0.9f,
            positionX = 1.1f,
            positionY = 2.5f,
            rotationDegrees = 0f
        )
        
        val status = useCase(target, room, emptyList())
        
        assertTrue("Debería ser un TightFit por cercanía a pared", status is FitStatus.TightFit)
        val tightResult = status as FitStatus.TightFit
        assertTrue(tightResult.message.contains("pared"))
    }

    @Test
    fun testOccupiedAreaIsCritical_returnsTightFit() {
        val room = RoomDimensionsInput(widthMeters = 4.0f, lengthMeters = 4.0f) // Área = 16 m²
        
        // Mueble a añadir: Área = 2.0 * 2.0 = 4 m² (25%)
        val target = TargetFurnitureInput(
            widthMeters = 2.0f,
            heightMeters = 0.85f,
            depthMeters = 2.0f,
            positionX = 2.0f,
            positionY = 2.0f,
            rotationDegrees = 0f
        )
        
        // Muebles ya colocados con área total de 7 m² (Ocupación total tras agregar mueble = 4 + 7 = 11 m² / 16 m² = 68.75% > 60%)
        val existing = listOf(
            ExistingFurnitureFootprint(widthMeters = 2.0f, depthMeters = 2.0f), // 4 m²
            ExistingFurnitureFootprint(widthMeters = 3.0f, depthMeters = 1.0f)  // 3 m²
        )
        
        val status = useCase(target, room, existing)
        
        assertTrue("Debería ser un TightFit por ocupación crítica", status is FitStatus.TightFit)
        val tightResult = status as FitStatus.TightFit
        assertEquals(68.75f, tightResult.occupiedAreaPercentage, 0.01f)
        assertTrue(tightResult.message.contains("densidad"))
    }

    @Test
    fun testFurnitureRotatedExceedsBounds_returnsExceedsBounds() {
        val room = RoomDimensionsInput(widthMeters = 6.0f, lengthMeters = 5.0f)
        
        // Mueble largo colocado cerca del borde. Sin rotar cabe, pero al rotarlo 90º excede el límite superior.
        // Dimensiones: 3.0 x 1.0. Posición central: (4.0, 4.2).
        // Sin rotar: maxY = 4.2 + 0.5 = 4.7 < 5.0 (Cabe)
        // Rotado 90º: maxY = 4.2 + 1.5 = 5.7 > 5.0 (No cabe)
        val target = TargetFurnitureInput(
            widthMeters = 3.0f,
            heightMeters = 1.0f,
            depthMeters = 1.0f,
            positionX = 4.0f,
            positionY = 4.2f,
            rotationDegrees = 90f
        )
        
        val status = useCase(target, room, emptyList())
        
        assertTrue("Al estar rotado 90º debería exceder el límite superior de la pared", status is FitStatus.ExceedsBounds)
    }
}
