package dev.bhaswat.aura.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.bhaswat.aura.ui.screens.home.HomeScreen
import dev.bhaswat.aura.ui.screens.home.HomeViewModel
import dev.bhaswat.aura.ui.screens.home.HomeViewModelFactory
import dev.bhaswat.aura.ui.screens.plan.PlanScreen
import androidx.compose.runtime.getValue // <-- ADD THIS IMPORT

object Routes {
    const val HOME_SCREEN = "home"
    const val PLAN_SCREEN = "plan"

}

@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    val sharedViewModel: SharedViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Routes.HOME_SCREEN
    ) {
        composable(Routes.HOME_SCREEN) {
            val homeViewModel : HomeViewModel = viewModel(
                factory = HomeViewModelFactory(sharedViewModel)
            )
            HomeScreen(
                homeViewModel = homeViewModel,
                onNavigateToPlan = {
                    navController.navigate(Routes.PLAN_SCREEN)
                }
            )
        }

        composable(Routes.PLAN_SCREEN) {
            // Get the plan from the SharedViewModel
            val plan by sharedViewModel.plan.collectAsState()
            plan?.let {
                PlanScreen(plan = it)
            }
        }
    }
}