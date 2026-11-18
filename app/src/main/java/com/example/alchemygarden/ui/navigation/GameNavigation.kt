package com.example.alchemygarden.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.alchemygarden.ui.screens.encyclopedia.EncyclopediaScreen
import com.example.alchemygarden.ui.screens.greenhouse.GreenhouseScreen
import com.example.alchemygarden.ui.screens.laboratory.LaboratoryScreen
import com.example.alchemygarden.ui.screens.map.IslandMapScreen
import com.example.alchemygarden.ui.theme.MossGreen
import com.example.alchemygarden.ui.theme.SoftCream
import com.example.alchemygarden.ui.theme.WarmParchment
import com.example.alchemygarden.ui.viewmodel.GameViewModel

data class NavItem(val route: String, val label: String, val emoji: String)

@Composable
fun GameMainScaffold(viewModel: GameViewModel) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val eventMessage by viewModel.lastEventMessage.collectAsState()

    LaunchedEffect(eventMessage) {
        if (eventMessage != null) {
            snackbarHostState.showSnackbar(eventMessage!!)
            viewModel.clearEventMessage()
        }
    }

    val navItems = listOf(
        NavItem("greenhouse", "Сад", "🌿"),
        NavItem("laboratory", "Котел", "⚗️"),
        NavItem("map", "Карта", "🗺️"),
        NavItem("encyclopedia", "Книга", "📖")
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        containerColor = WarmParchment,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(containerColor = SoftCream) {
                navItems.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route,
                        onClick = {
                            if (currentRoute != item.route) {
                                navController.navigate(item.route) {
                                    popUpTo("greenhouse") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Text(item.emoji) },
                        label = { Text(item.label) },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = MossGreen.copy(alpha = 0.18f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "greenhouse",
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                fadeIn(animationSpec = tween(300)) +
                    slideInHorizontally(initialOffsetX = { it / 5 }, animationSpec = tween(300))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(250))
            }
        ) {
            composable("greenhouse") { GreenhouseScreen(viewModel) }
            composable("laboratory") { LaboratoryScreen(viewModel) }
            composable("map") { IslandMapScreen(viewModel) }
            composable("encyclopedia") { EncyclopediaScreen(viewModel) }
        }
    }
}
