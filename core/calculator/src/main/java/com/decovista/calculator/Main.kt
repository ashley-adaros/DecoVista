package com.decovista.calculator

import com.decovista.calculator.domain.usecase.CalculateFitUseCase
import com.decovista.calculator.domain.usecase.ExistingFurnitureFootprint
import com.decovista.calculator.domain.usecase.FitStatus
import com.decovista.calculator.domain.usecase.RoomDimensionsInput
import com.decovista.calculator.domain.usecase.TargetFurnitureInput

/**
 * Punto de entrada de consola para probar la lógica geométrica de DecoVista
 * sin requerir el SDK de Android ni emuladores.
 */
fun main() {
    val calculateFitUseCase = CalculateFitUseCase()

    println("=========================================================")
    println("      DECOVISTA - SIMULADOR DE ESPACIOS DE CONSOLA       ")
    println("=========================================================")

    // 1. Definir dimensiones de la habitación (6.0m de ancho x 5.0m de largo)
    val room = RoomDimensionsInput(widthMeters = 6.0f, lengthMeters = 5.0f)
    println("-> Habitación creada: ${room.widthMeters}m x ${room.lengthMeters}m (Área: ${room.widthMeters * room.lengthMeters} m²)")

    // 2. Definir muebles que ya existen en la habitación
    val existingFurniture = listOf(
        ExistingFurnitureFootprint(widthMeters = 2.0f, depthMeters = 1.0f), // Mesa comedor
        ExistingFurnitureFootprint(widthMeters = 0.8f, depthMeters = 0.8f)  // Sillón de lectura
    )
    println("-> Muebles existentes colocados en el espacio:")
    existingFurniture.forEachIndexed { index, item ->
        println("   [$index] Mueble de: ${item.widthMeters}m x ${item.depthMeters}m")
    }
    println("---------------------------------------------------------")

    // 3. Evaluar escenarios de colocación de un sofá rectangular (2.2m x 0.9m)
    val sofaWidth = 2.2f
    val sofaDepth = 0.9f
    val sofaHeight = 0.85f

    // Escenario A: Colocación perfecta en el centro
    println("\n[Escenario A]: Posicionar Sofá en el centro (X = 3.0m, Y = 2.5m, Rotación = 0°)")
    val targetA = TargetFurnitureInput(
        widthMeters = sofaWidth,
        heightMeters = sofaHeight,
        depthMeters = sofaDepth,
        positionX = 3.0f,
        positionY = 2.5f,
        rotationDegrees = 0f
    )
    evaluatePlacement(calculateFitUseCase, targetA, room, existingFurniture)

    // Escenario B: Colocación muy cerca de la pared izquierda (TightFit por proximidad)
    println("\n[Escenario B]: Posicionar Sofá muy pegado a la pared izquierda (X = 1.15m, Y = 2.5m, Rotación = 0°)")
    val targetB = TargetFurnitureInput(
        widthMeters = sofaWidth,
        heightMeters = sofaHeight,
        depthMeters = sofaDepth,
        positionX = 1.15f, // Límite minX será 1.15 - 1.1 = 0.05m (5 cm de la pared)
        positionY = 2.5f,
        rotationDegrees = 0f
    )
    evaluatePlacement(calculateFitUseCase, targetB, room, existingFurniture)

    // Escenario C: Colocación con Rotación a 45° que sobresale de los límites de la pared
    println("\n[Escenario C]: Posicionar Sofá en esquina superior derecha con rotación a 45° (X = 5.0m, Y = 4.3m)")
    val targetC = TargetFurnitureInput(
        widthMeters = sofaWidth,
        heightMeters = sofaHeight,
        depthMeters = sofaDepth,
        positionX = 5.0f,
        positionY = 4.3f,
        rotationDegrees = 45f // La rotación expandirá el tamaño de la caja delimitadora en X e Y
    )
    evaluatePlacement(calculateFitUseCase, targetC, room, existingFurniture)
    
    println("=========================================================")
}

private fun evaluatePlacement(
    useCase: CalculateFitUseCase,
    target: TargetFurnitureInput,
    room: RoomDimensionsInput,
    existing: List<ExistingFurnitureFootprint>
) {
    val status = useCase(target, room, existing)
    
    // Imprimir los resultados por consola
    when (status) {
        is FitStatus.Fits -> {
            println("   ✅ ¡ÉXITO! - El mueble cabe perfectamente.")
            println("   📊 Área ocupada del suelo: ${String.format("%.1f", status.occupiedAreaPercentage)}%")
            println("   💬 Mensaje: ${status.message}")
        }
        is FitStatus.TightFit -> {
            println("   ⚠️ ADVERTENCIA - Espacio ajustado.")
            println("   📊 Área ocupada del suelo: ${String.format("%.1f", status.occupiedAreaPercentage)}%")
            println("   💬 Mensaje: ${status.message}")
        }
        is FitStatus.ExceedsBounds -> {
            println("   ❌ ERROR - El mueble no cabe.")
            println("   💬 Mensaje: ${status.message}")
        }
    }
}
