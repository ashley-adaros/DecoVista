package com.decovista.calculator.domain.logic

import com.decovista.calculator.domain.model.Box2D
import com.decovista.calculator.domain.model.Vector2D
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

object CollisionDetector {

    /**
     * Implementación del Teorema del Eje Separador (SAT) para OBB (Oriented Bounding Boxes).
     */
    fun checkOverlap(box1: Box2D, box2: Box2D): Boolean {
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

    fun isOutOfBounds(box: Box2D, roomWidth: Float, roomHeight: Float): Boolean {
        val vertices = getVertices(box)
        return vertices.any { it.x < 0f || it.x > roomWidth || it.y < 0f || it.y > roomHeight }
    }

    private fun getVertices(box: Box2D): List<Vector2D> {
        val halfW = box.width / 2
        val halfD = box.depth / 2
        
        val localVertices = listOf(
            Vector2D(-halfW, -halfD),
            Vector2D(halfW, -halfD),
            Vector2D(halfW, halfD),
            Vector2D(-halfW, halfD)
        )

        val rad = Math.toRadians(box.rotationDegrees.toDouble())
        val cosA = cos(rad).toFloat()
        val sinA = sin(rad).toFloat()

        return localVertices.map { v ->
            Vector2D(
                x = box.centerX + (v.x * cosA - v.y * sinA),
                y = box.centerY + (v.x * sinA + v.y * cosA)
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
}
