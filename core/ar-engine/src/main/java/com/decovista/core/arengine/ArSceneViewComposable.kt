package com.decovista.core.arengine

import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.PlacementMode

@Composable
fun ArSceneViewComposable(
    modelUrl: String,
    onModelLoaded: () -> Unit = {},
    onPlaneDetected: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var planeDetected by remember { mutableStateOf(false) }
    var currentModelNode by remember { mutableStateOf<ArModelNode?>(null) }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { ctx ->
            ARSceneView(ctx).apply {
                planeRenderer.isVisible = true
                
                val modelNode = ArModelNode(
                    engine = engine,
                    modelGlbUrl = modelUrl,
                    autoAnimate = true,
                    scaleToUnits = 1.0f
                ).apply {
                    placementMode = PlacementMode.PLANE_HORIZONTAL
                    isPositionEditable = true
                    isRotationEditable = true
                    isScaleEditable = false
                    
                    this.onModelLoaded = {
                        onModelLoaded()
                    }
                    
                    onModelError = { exception ->
                        Toast.makeText(context, "Error: ${exception.message}", Toast.LENGTH_LONG).show()
                    }
                }

                addChild(modelNode)
                currentModelNode = modelNode

                onSessionUpdated = { _, frame ->
                    val planes = frame.getUpdatedTrackables(io.google.ar.core.Plane::class.java)
                    if (planes.isNotEmpty() && !planeDetected) {
                        planeDetected = true
                        onPlaneDetected()
                    }
                }
            }
        },
        onRelease = { arSceneView ->
            currentModelNode?.let { arSceneView.removeChild(it) }
            arSceneView.destroy()
        }
    )
}
