package com.decovista.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.decovista.planner2d.ui.SpacePlannerScreen
import com.decovista.planner2d.ui.SpacePlannerViewModel
import com.decovista.viewer3d.ui.ArFurnitureViewerScreen

sealed class Screen(val route: String) {
    object Planner2D : Screen("planner2d")
    object Viewer3D : Screen("viewer3d/{modelUrl}") {
        fun createRoute(modelUrl: String) = "viewer3d/$modelUrl"
    }
}

@Composable
fun DecoVistaNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Planner2D.route
    ) {
        composable(Screen.Planner2D.route) {
            val viewModel: SpacePlannerViewModel = viewModel()
            SpacePlannerScreen(
                viewModel = viewModel,
                onViewInAr = { modelUrl ->
                    navController.navigate(Screen.Viewer3D.createRoute(modelUrl))
                }
            )
        }
        composable(
            route = Screen.Viewer3D.route,
            arguments = listOf(navArgument("modelUrl") { type = NavType.StringType })
        ) { backStackEntry ->
            val modelUrl = backStackEntry.arguments?.getString("modelUrl") ?: ""
            ArFurnitureViewerScreen(modelUrl = modelUrl)
        }
    }
}
