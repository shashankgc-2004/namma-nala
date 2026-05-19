package com.example.nammanala

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.nammanala.ui.screens.*
import com.example.nammanala.ui.theme.CanalGreen
import com.google.firebase.auth.FirebaseAuth

private data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

@Composable
fun NammaNalaApp() {
    val navController = rememberNavController()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavItems = remember {
        listOf(
            BottomNavItem("home",   "Home",    Icons.Filled.Home),
            BottomNavItem("report", "Report",  Icons.Filled.ReportProblem),
            BottomNavItem("map",    "Map",     Icons.Filled.Map),
            BottomNavItem("status", "Status",  Icons.Filled.Waves)
        )
    }

    // Only show bottom bar on top-level destinations
    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = Color.White) {
                    bottomNavItems.forEach { item ->
                        val selected = navBackStackEntry?.destination?.hierarchy
                            ?.any { it.route == item.route } == true
                        NavigationBarItem(
                            icon     = { Icon(item.icon, contentDescription = item.label) },
                            label    = { Text(item.label) },
                            selected = selected,
                            colors   = NavigationBarItemDefaults.colors(
                                selectedIconColor   = CanalGreen,
                                selectedTextColor   = CanalGreen,
                                indicatorColor      = CanalGreen.copy(alpha = 0.12f)
                            ),
                            onClick  = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState    = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController    = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home"){ HomeScreen(navController) }
            composable("report") { ReportBreachScreen(navController) }
            composable("map")    { MapScreen(navController) }
            composable("status") { WaterStatusScreen(navController) }
        }
    }
}
