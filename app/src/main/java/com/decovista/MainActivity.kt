package com.decovista

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.decovista.planner2d.ui.SpacePlannerScreen
import com.decovista.planner2d.ui.SpacePlannerViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Obtener el ViewModel provisto e inyectado por Hilt
                    val plannerViewModel: SpacePlannerViewModel = hiltViewModel()
                    
                    // Cargar la pantalla de diseño espacial 2D interactivo
                    SpacePlannerScreen(viewModel = plannerViewModel)
                }
            }
        }
    }
}
