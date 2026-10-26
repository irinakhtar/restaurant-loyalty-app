package com.regulars.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

@Composable
fun RegularsNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = RegularsDestination.AllRestaurants.route,
        modifier = modifier,
    ) {
        composable(RegularsDestination.AllRestaurants.route) {
            PlaceholderScreen(title = "All restaurants")
        }
        composable(RegularsDestination.MyCards.route) {
            PlaceholderScreen(title = "My cards")
        }
        composable(
            route = RegularsDestination.RestaurantDetail.route,
            arguments = listOf(
                navArgument(RegularsDestination.RestaurantDetail.RESTAURANT_ID) {
                    type = NavType.StringType
                },
            ),
        ) { entry ->
            val restaurantId = entry.arguments
                ?.getString(RegularsDestination.RestaurantDetail.RESTAURANT_ID)
                .orEmpty()
            PlaceholderScreen(title = "Restaurant $restaurantId".trim())
        }
        composable(RegularsDestination.Settings.route) {
            PlaceholderScreen(title = "Settings")
        }
    }
}

@Composable
private fun PlaceholderScreen(
    title: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
        )
    }
}
