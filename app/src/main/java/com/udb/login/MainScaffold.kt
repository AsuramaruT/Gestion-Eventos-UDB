package com.udb.login

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun MainScaffold(navController: NavHostController) {
    // NavController interno para navegación entre pantallas inferiores
    val innerNavController = rememberNavController()

    // Obtener una sola vez el estado actual de la pila para evitar múltiples llamadas
    val navBackStackEntry by innerNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentRoute == Screens.Home.route,
                    onClick = {
                        if (currentRoute != Screens.Home.route) {
                            innerNavController.navigate(Screens.Home.route) {
                                launchSingleTop = true
                                restoreState = true
                                popUpTo(innerNavController.graph.startDestinationId) {
                                    saveState = true
                                }
                            }
                        }
                    },
                    icon = { Icon(Icons.Default.Schedule, contentDescription = "Eventos") },
                    label = { Text("Eventos") }
                )
                NavigationBarItem(
                    selected = currentRoute == Screens.CreateEvent.route,
                    onClick = {
                        if (currentRoute != Screens.CreateEvent.route) {
                            innerNavController.navigate(Screens.CreateEvent.route) {
                                launchSingleTop = true
                                restoreState = true
                                popUpTo(innerNavController.graph.startDestinationId) {
                                    saveState = true
                                }
                            }
                        }
                    },
                    icon = { Icon(Icons.Default.Add, contentDescription = "Crear") },
                    label = { Text("Crear") }
                )
                NavigationBarItem(
                    selected = currentRoute == Screens.History.route,
                    onClick = {
                        if (currentRoute != Screens.History.route) {
                            innerNavController.navigate(Screens.History.route) {
                                launchSingleTop = true
                                restoreState = true
                                popUpTo(innerNavController.graph.startDestinationId) {
                                    saveState = true
                                }
                            }
                        }
                    },
                    icon = { Icon(Icons.Default.History, contentDescription = "Historial") },
                    label = { Text("Historial") }
                )
                NavigationBarItem(
                    selected = currentRoute == Screens.Profile.route,
                    onClick = {
                        if (currentRoute != Screens.Profile.route) {
                            innerNavController.navigate(Screens.Profile.route) {
                                launchSingleTop = true
                                restoreState = true
                                popUpTo(innerNavController.graph.startDestinationId) {
                                    saveState = true
                                }
                            }
                        }
                    },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil") }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = innerNavController,
            startDestination = Screens.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screens.Home.route) {
                EventListScreen(innerNavController)
            }
            composable(Screens.CreateEvent.route) {
                CreateEventScreen(innerNavController)
            }
            composable(Screens.History.route) {
                EventHistoryScreen(innerNavController)
            }
            composable(Screens.Profile.route) {
                ProfileScreen(navController) // Usa navController superior para Logout y navegación general
            }
            composable(
                route = Screens.EventDetail.route,
                arguments = listOf(navArgument("eventId") { type = NavType.StringType })
            ) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
                EventDetailScreen(eventId, innerNavController)
            }
        }
    }
}
