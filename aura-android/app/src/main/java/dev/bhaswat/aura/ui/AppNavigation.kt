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
import dev.bhaswat.aura.ui.screens.auth.OtpScreen
import dev.bhaswat.aura.ui.screens.auth.SignUpScreen
import dev.bhaswat.aura.ui.screens.home.HomeScreen
import dev.bhaswat.aura.ui.screens.home.HomeViewModel
import dev.bhaswat.aura.ui.screens.home.HomeViewModelFactory
import dev.bhaswat.aura.ui.screens.plan.PlanScreen
import dev.bhaswat.aura.ui.screens.saved.SavedPlansScreen

object Routes {
    const val LOGIN_SCREEN = "login"

    const val SIGNUP_SCREEN = "signup"

    const val OTP_SCREEN = "otp"
    const val HOME_SCREEN = "home"
    const val PLAN_SCREEN = "plan"

    const val SAVED_PLANS_SCREEN = "saved_plans"
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
            // The new state we added for the OTP flow
            is AuthUiState.NeedsOtpVerification -> Routes.OTP_SCREEN
            else -> Routes.LOGIN_SCREEN
        }

        NavHost(
            navController = navController ,
            startDestination = startDestination ,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.LOGIN_SCREEN) {
                LoginScreen(
                    authViewModel = authViewModel ,
                    onLoginSuccess = {
                        navController.navigate(Routes.HOME_SCREEN) {
                            popUpTo(Routes.LOGIN_SCREEN) { inclusive = true }
                        }
                    } ,
                    onNavigateToSignUp = {
                        navController.navigate(Routes.SIGNUP_SCREEN)
                    }
                )
            }
            // THIS IS THE NEW ROUTE WE ARE ADDING
            composable(Routes.SIGNUP_SCREEN) {
                SignUpScreen(
                    authViewModel = authViewModel ,
                    onSignUpSuccess = {
                        navController.navigate(Routes.OTP_SCREEN) {
                        }
                    } ,
                    // This allows the user to go back to the login screen
                    onNavigateToLogin = {
                        navController.popBackStack()
                    }
                )
            }
            composable(Routes.OTP_SCREEN) {
                OtpScreen(
                    authViewModel = authViewModel ,
                    onVerificationSuccess = {
                        // When the user clicks "Go to Login", we clear the auth flow
                        // and go back to a clean login screen.
                        navController.navigate(Routes.HOME_SCREEN) {
                            popUpTo(Routes.LOGIN_SCREEN) { inclusive = true }
                        }
                    }
                )
            }


            composable(Routes.HOME_SCREEN) {
                val application = LocalContext.current.applicationContext as Application
                val homeViewModel: HomeViewModel = viewModel(
                    factory = HomeViewModelFactory(application , sharedViewModel)
                )

                // THIS IS THE FIX: We now correctly pass the snackbarHostState
                HomeScreen(
                    homeViewModel = homeViewModel ,
                    snackbarHostState = snackbarHostState ,
                    onNavigateToPlan = {
                        navController.navigate(Routes.PLAN_SCREEN)
                    } ,
                    onNavigateToSavedPlans = {
                        navController.navigate(Routes.SAVED_PLANS_SCREEN)
                    } ,
                    onLogout = {
                        authViewModel.logout()
                        navController.navigate(Routes.LOGIN_SCREEN) {
                            popUpTo(Routes.HOME_SCREEN) { inclusive = true }
                        }
                    }
                )
            }
            composable(Routes.PLAN_SCREEN) {
                val plan by sharedViewModel.plan.collectAsState()
                plan?.let {
                    PlanScreen(plan = it)
                }
            }
            composable(Routes.SAVED_PLANS_SCREEN) {
                SavedPlansScreen(
                    onPlanClicked = { plan ->
                        // When a saved plan is clicked, put it in the SharedViewModel
                        sharedViewModel.setPlan(plan)
                        // Then navigate to the regular PlanScreen to view it
                        navController.navigate(Routes.PLAN_SCREEN)
                    } ,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}