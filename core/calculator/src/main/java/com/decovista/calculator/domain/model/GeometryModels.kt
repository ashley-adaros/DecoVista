package com.decovista.calculator.domain.model

data class Vector2D(val x: Float, val y: Float)

data class Box2D(
    val centerX: Float,
    val centerY: Float,
    val width: Float,
    val depth: Float,
    val rotationDegrees: Float
)
