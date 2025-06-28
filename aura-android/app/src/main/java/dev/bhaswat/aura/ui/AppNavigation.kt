package dev.bhaswat.aura.ui

import android.app.Application
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.bhaswat.aura.ui.screens.auth.AuthUiState
import dev.bhaswat.aura.ui.screens.auth.AuthViewModel
import dev.bhaswat.aura.ui.screens.auth.LoginScreen
import dev.bhaswat.aura.ui.screens.auth.SignUpScreen
import dev.bhaswat.aura.ui.screens.auth.VerificationPendingScreen
import dev.bhaswat.aura.ui.screens.home.HomeScreen
import dev.bhaswat.aura.ui.screens.home.HomeViewModel
import dev.bhaswat.aura.ui.screens.home.HomeViewModelFactory
import dev.bhaswat.aura.ui.screens.plan.PlanScreen

object Routes {
    const val LOGIN_SCREEN = "login"

    const val SIGNUP_SCREEN = "signup"
    const val HOME_SCREEN = "home"
    const val PLAN_SCREEN = "plan"

    const val SIGNUP_SUCCESS_SCREEN = "signup_success"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val sharedViewModel: SharedViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.uiState.collectAsState()

    // Check the user's status when the app starts
    LaunchedEffect(Unit) {
        authViewModel.checkCurrentUser()
    }

    // Create the SnackbarHostState here, in the central component that owns the Scaffold
    val snackbarHostState = remember { SnackbarHostState() }

    // The Scaffold is the root layout of our app.
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->

        //val startDestination = if (authState is AuthUiState.Success) Routes.HOME_SCREEN else Routes.LOGIN_SCREEN
        val startDestination = when (authState) {
            is AuthUiState.Success -> Routes.HOME_SCREEN
            is AuthUiState.SignUpSuccessPendingVerification -> Routes.SIGNUP_SUCCESS_SCREEN // Navigate to pending verification screen
            else -> Routes.LOGIN_SCREEN
        }

        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.LOGIN_SCREEN) {
                LoginScreen(
                    authViewModel = authViewModel,
                    onLoginSuccess = {
                        navController.navigate(Routes.HOME_SCREEN) {
                            popUpTo(Routes.LOGIN_SCREEN) { inclusive = true }
                        }
                    },
                    onNavigateToSignUp = {
                        navController.navigate(Routes.SIGNUP_SCREEN)
                    }
                )
            }
            // THIS IS THE NEW ROUTE WE ARE ADDING
            composable(Routes.SIGNUP_SCREEN) {
                SignUpScreen(
                    authViewModel = authViewModel,
                    onSignUpSuccess = {
                        navController.navigate(Routes.SIGNUP_SUCCESS_SCREEN) {
                            popUpTo(Routes.LOGIN_SCREEN) { inclusive = true }
                        }
                    },
                    // This allows the user to go back to the login screen
                    onNavigateToLogin = {
                        navController.popBackStack()
                    }
                )
            }
            composable(Routes.SIGNUP_SUCCESS_SCREEN) {
                VerificationPendingScreen(
                    authViewModel = authViewModel, // Pass AuthViewModel
                    onNavigateToLogin = {
                        navController.navigate(Routes.LOGIN_SCREEN) {
                            popUpTo(Routes.SIGNUP_SUCCESS_SCREEN) { inclusive = true } // Clear back stack up to this screen
                        }
                    }
                )
            }


            composable(Routes.HOME_SCREEN) {
                val application = LocalContext.current.applicationContext as Application
                val homeViewModel: HomeViewModel = viewModel(
                    factory = HomeViewModelFactory(application, sharedViewModel)
                )

                // THIS IS THE FIX: We now correctly pass the snackbarHostState
                HomeScreen(
                    homeViewModel = homeViewModel,
                    snackbarHostState = snackbarHostState,
                    onNavigateToPlan = {
                        navController.navigate(Routes.PLAN_SCREEN)
                    }
                )
            }
            composable(Routes.PLAN_SCREEN) {
                val plan by sharedViewModel.plan.collectAsState()
                plan?.let {
                    PlanScreen(plan = it)
                }
            }
        }
    }
}