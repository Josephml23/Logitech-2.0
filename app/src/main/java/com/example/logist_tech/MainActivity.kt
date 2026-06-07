package com.example.logist_tech

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.logist_tech.anomalias.AnomaliasScreen
import com.example.logist_tech.auth.LoginScreen
import com.example.logist_tech.auth.SessionManager
import com.example.logist_tech.history.HistoryScreen
import com.example.logist_tech.inventory.InventarioScreen
import com.example.logist_tech.scanner.ScannerScreen
import com.example.logist_tech.ui.screens.HomeScreen
import com.example.logist_tech.ui.theme.Logist_TechTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Logist_TechTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val startDestination = if (SessionManager.estaLogueado()) "home" else "login"
                    NavHost(
                        navController = navController,
                        startDestination = startDestination
                    ) {
                        composable("login") {
                            LoginScreen(
                                onLoginSuccess = {
                                    navController.navigate("home") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("home") {
                            HomeScreen(
                                onNavigateScanner = { navController.navigate("scanner") },
                                onNavigateInventory = { navController.navigate("inventory") },
                                onNavigateHistory = { navController.navigate("history") },
                                onNavigateAnomalies = { navController.navigate("anomalias") },
                                onLogout = {
                                    SessionManager.logout()
                                    navController.navigate("login") {
                                        popUpTo("home") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("scanner") { ScannerScreen() }
                        composable("inventory") {
                            InventarioScreen(onNavigateBack = { navController.popBackStack() })
                        }
                        composable("history") {
                            HistoryScreen(onNavigateBack = { navController.popBackStack() })
                        }
                        composable("anomalias") {
                            AnomaliasScreen(onNavigateBack = { navController.popBackStack() })
                        }
                    }
                }
            }
        }
    }
}
