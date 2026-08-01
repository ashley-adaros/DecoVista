package com.decovista

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.decovista.calculator.domain.usecase.CalculateFitUseCase
import com.decovista.planner2d.ui.SpacePlannerScreen
import com.decovista.planner2d.ui.SpacePlannerViewModel
import com.decovista.planner2d.ui.SpacePlannerViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Instanciar el UseCase y la Factory nativa de Android de forma remota
                    val calculateFitUseCase = remember { CalculateFitUseCase() }
                    val plannerViewModel: SpacePlannerViewModel = viewModel(
                        factory = SpacePlannerViewModelFactory(calculateFitUseCase)
                    )
                    
                    // Cargar la pantalla de diseño espacial 2D interactivo
                    SpacePlannerScreen(
                        viewModel = plannerViewModel,
                        onViewInAr = { modelUrl ->
                            // Aquí se podría lanzar la pantalla 3D/AR en el flujo
                        }
                    )
                }
            }
        }
    }
}
