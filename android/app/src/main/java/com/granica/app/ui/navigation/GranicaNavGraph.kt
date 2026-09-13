package com.granica.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.granica.app.AppSettings
import com.granica.app.data.repository.GranicaRepository
import com.granica.app.ui.screens.BordersScreen
import com.granica.app.ui.screens.CountriesScreen
import com.granica.app.ui.screens.CrossingDetailScreen
import com.granica.app.ui.screens.CrossingsScreen
import com.granica.app.ui.screens.FavoritesScreen
import com.granica.app.ui.screens.SettingsScreen
import com.granica.app.ui.viewmodel.BordersViewModel
import com.granica.app.ui.viewmodel.CountriesViewModel
import com.granica.app.ui.viewmodel.CrossingDetailViewModel
import com.granica.app.ui.viewmodel.CrossingsViewModel
import com.granica.app.ui.viewmodel.FavoritesViewModel

private object Routes {
    const val COUNTRIES = "countries"
    const val FAVORITES = "favorites"
    const val SETTINGS = "settings"
    const val BORDERS = "borders/{countryCode}/{countryName}"
    const val CROSSINGS = "crossings/{borderId}/{title}/{countryCode}"
    const val DETAIL = "detail/{crossingId}/{countryCode}"

    fun borders(code: String, name: String) = "borders/$code/${java.net.URLEncoder.encode(name, "UTF-8")}"
    fun crossings(borderId: String, title: String, countryCode: String) =
        "crossings/$borderId/${java.net.URLEncoder.encode(title, "UTF-8")}/$countryCode"
    fun detail(crossingId: String, countryCode: String) = "detail/$crossingId/$countryCode"
}

@Composable
fun GranicaNavGraph(repository: GranicaRepository, settings: AppSettings) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomBar(navController) }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.COUNTRIES,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.COUNTRIES) {
                val vm: CountriesViewModel = viewModel(factory = CountriesViewModel.Factory(repository))
                CountriesScreen(
                    viewModel = vm,
                    onCountryClick = { country ->
                        navController.navigate(Routes.borders(country.code, country.name))
                    }
                )
            }
            composable(
                Routes.BORDERS,
                arguments = listOf(
                    navArgument("countryCode") { type = NavType.StringType },
                    navArgument("countryName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val countryCode = backStackEntry.arguments?.getString("countryCode").orEmpty()
                val countryName = java.net.URLDecoder.decode(backStackEntry.arguments?.getString("countryName").orEmpty(), "UTF-8")
                val vm: BordersViewModel = viewModel(factory = BordersViewModel.Factory(repository, countryCode))
                BordersScreen(
                    countryName = countryName,
                    countryCode = countryCode,
                    viewModel = vm,
                    onBorderClick = { border ->
                        val title = "$countryName ↔ ${border.otherCountryName}"
                        navController.navigate(Routes.crossings(border.id, title, countryCode))
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                Routes.CROSSINGS,
                arguments = listOf(
                    navArgument("borderId") { type = NavType.StringType },
                    navArgument("title") { type = NavType.StringType },
                    navArgument("countryCode") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val borderId = backStackEntry.arguments?.getString("borderId").orEmpty()
                val title = java.net.URLDecoder.decode(backStackEntry.arguments?.getString("title").orEmpty(), "UTF-8")
                val countryCode = backStackEntry.arguments?.getString("countryCode").orEmpty()
                val vm: CrossingsViewModel = viewModel(factory = CrossingsViewModel.Factory(repository, borderId))
                CrossingsScreen(
                    title = title,
                    viewModel = vm,
                    onCrossingClick = { crossing ->
                        navController.navigate(Routes.detail(crossing.id, countryCode))
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                Routes.DETAIL,
                arguments = listOf(
                    navArgument("crossingId") { type = NavType.StringType },
                    navArgument("countryCode") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val crossingId = backStackEntry.arguments?.getString("crossingId").orEmpty()
                val countryCode = backStackEntry.arguments?.getString("countryCode").orEmpty()
                val vm: CrossingDetailViewModel = viewModel(
                    factory = CrossingDetailViewModel.Factory(repository, crossingId, countryCode)
                )
                CrossingDetailScreen(viewModel = vm, settings = settings, onBack = { navController.popBackStack() })
            }
            composable(Routes.FAVORITES) {
                val vm: FavoritesViewModel = viewModel(factory = FavoritesViewModel.Factory(repository))
                FavoritesScreen(
                    viewModel = vm,
                    onFavoriteClick = { favorite ->
                        navController.navigate(Routes.detail(favorite.crossingId, favorite.countryCode))
                    }
                )
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    settings = settings,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
private fun BottomBar(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination

    NavigationBar {
        NavigationBarItem(
            selected = currentRoute?.hierarchy?.any { it.route == Routes.COUNTRIES } == true,
            onClick = {
                navController.navigate(Routes.COUNTRIES) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = { Icon(Icons.Filled.List, contentDescription = null) },
            label = { Text("Страны") }
        )
        NavigationBarItem(
            selected = currentRoute?.hierarchy?.any { it.route == Routes.FAVORITES } == true,
            onClick = {
                navController.navigate(Routes.FAVORITES) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = { Icon(Icons.Filled.Favorite, contentDescription = null) },
            label = { Text("Избранное") }
        )
        NavigationBarItem(
            selected = currentRoute?.hierarchy?.any { it.route == Routes.SETTINGS } == true,
            onClick = {
                navController.navigate(Routes.SETTINGS) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
            label = { Text("Настройки") }
        )
    }
}
