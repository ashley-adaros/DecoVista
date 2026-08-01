package com.decovista.viewer3d.ui

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.decovista.core.arengine.ArSceneViewComposable

@Composable
fun ArFurnitureViewerScreen(
    modelUrl: String, // Enlace al archivo .glb del mueble seleccionado
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
            if (!granted) {
                Toast.makeText(context, "El permiso de cámara es requerido para Realidad Aumentada.", Toast.LENGTH_LONG).show()
            }
        }
    )

    Box(modifier = modifier.fillMaxSize().background(Color(0xFF0F172A))) {
        if (hasCameraPermission) {
            // Cargar el visor ARCore + Sceneview
            ArSceneViewContainer(modelUrl = modelUrl)
        } else {
            // Pantalla informativa de solicitud de permisos
            PermissionRequestScreen(
                onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) }
            )
        }
    }
}

@Composable
fun ArSceneViewContainer(
    modelUrl: String,
    modifier: Modifier = Modifier
) {
    var isLoadingModel by remember { mutableStateOf(true) }
    var planeDetected by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        ArSceneViewComposable(
            modelUrl = modelUrl,
            onModelLoaded = { isLoadingModel = false },
            onPlaneDetected = { planeDetected = true },
            modifier = Modifier.fillMaxSize()
        )

        // Indicador de escaneo inicial de suelo
        AnimatedVisibility(
            visible = !planeDetected && !isLoadingModel,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center).padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(Color(0xCC1E293B), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "Mueve el móvil lentamente en círculos para detectar el suelo...",
                    color = Color.White,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Cargador asíncrono del mesh 3D
        if (isLoadingModel) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(Color(0xCC1E293B), RoundedCornerShape(20.dp))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(color = Color(0xFF38BDF8))
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Descargando Modelo 3D...",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Guía inferior de interacción táctil
        AnimatedVisibility(
            visible = planeDetected && !isLoadingModel,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(Color(0x990F172A), RoundedCornerShape(50.dp))
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "Toca para colocar • Arrastra para mover • Rota con dos dedos",
                    color = Color(0xFF38BDF8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun PermissionRequestScreen(
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Acceso a la Cámara Requerido",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Para poder proyectar los muebles en tu propia habitación mediante Realidad Aumentada, DecoVista necesita permiso para usar la cámara.",
            color = Color(0xFF94A3B8),
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRequestPermission,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8))
        ) {
            Text(
                text = "Habilitar Cámara",
                color = Color(0xFF0F172A),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}
