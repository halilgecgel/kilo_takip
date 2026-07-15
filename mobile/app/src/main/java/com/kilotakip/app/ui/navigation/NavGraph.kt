package com.kilotakip.app.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kilotakip.app.ui.admin.AdminScreen
import com.kilotakip.app.ui.auth.LoginScreen
import com.kilotakip.app.ui.auth.RegisterScreen
import com.kilotakip.app.ui.dashboard.DashboardScreen
import com.kilotakip.app.ui.history.HistoryScreen
import com.kilotakip.app.ui.reminder.ReminderListScreen

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val DASHBOARD = "dashboard"
    const val HISTORY = "history"
    const val REMINDERS = "reminders"
    const val ADMIN = "admin"
}

private val mainScreenRoutes = setOf(Routes.DASHBOARD, Routes.HISTORY, Routes.REMINDERS, Routes.ADMIN)

@Composable
fun KiloTakipNavGraph(navController: NavHostController = rememberNavController()) {
    val mainViewModel: MainNavViewModel = hiltViewModel()
    val sessionState by mainViewModel.sessionState.collectAsState()
    val userProfile by mainViewModel.userProfile.collectAsState()

    if (sessionState.isChecking) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val startDestination = if (sessionState.isAuthenticated) Routes.DASHBOARD else Routes.LOGIN
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: startDestination

    val isMainScreen = currentRoute in mainScreenRoutes

    val navigateMain: (String) -> Unit = { route ->
        if (route != currentRoute) {
            navController.navigate(route) {
                popUpTo(Routes.DASHBOARD) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    val onLogout: () -> Unit = {
        mainViewModel.logout()
        navController.navigate(Routes.LOGIN) {
            popUpTo(0) { inclusive = true }
        }
    }

    if (isMainScreen && sessionState.isAuthenticated) {
        MainScaffold(
            currentRoute = currentRoute,
            userName = userProfile.name,
            isAdmin = sessionState.isAdmin,
            onNavigate = navigateMain,
            onLogout = onLogout
        ) {
            NavHost(
                navController = navController,
                startDestination = startDestination,
                enterTransition = { fadeIn(tween(200)) },
                exitTransition = { fadeOut(tween(200)) }
            ) {
                composable(Routes.LOGIN) {
                    LoginScreen(
                        onLoginSuccess = {
                            navController.navigate(Routes.DASHBOARD) {
                                popUpTo(Routes.LOGIN) { inclusive = true }
                            }
                        },
                        onNavigateToRegister = { navController.navigate(Routes.REGISTER) }
                    )
                }
                composable(Routes.REGISTER) {
                    RegisterScreen(
                        onRegisterSuccess = {
                            navController.navigate(Routes.DASHBOARD) {
                                popUpTo(Routes.LOGIN) { inclusive = true }
                            }
                        },
                        onNavigateToLogin = { navController.popBackStack() }
                    )
                }
                composable(Routes.DASHBOARD) {
                    DashboardScreen()
                }
                composable(Routes.HISTORY) {
                    HistoryScreen()
                }
                composable(Routes.REMINDERS) {
                    ReminderListScreen()
                }
                composable(Routes.ADMIN) {
                    AdminScreen(onBack = { navController.popBackStack() })
                }
            }
        }
    } else {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            enterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
            },
            exitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300))
            },
            popEnterTransition = {
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
            },
            popExitTransition = {
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300))
            }
        ) {
            composable(Routes.LOGIN) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Routes.DASHBOARD) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = { navController.navigate(Routes.REGISTER) }
                )
            }
            composable(Routes.REGISTER) {
                RegisterScreen(
                    onRegisterSuccess = {
                        navController.navigate(Routes.DASHBOARD) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = { navController.popBackStack() }
                )
            }
            composable(Routes.DASHBOARD) {
                DashboardScreen()
            }
            composable(Routes.HISTORY) {
                HistoryScreen()
            }
            composable(Routes.REMINDERS) {
                ReminderListScreen()
            }
            composable(Routes.ADMIN) {
                AdminScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
