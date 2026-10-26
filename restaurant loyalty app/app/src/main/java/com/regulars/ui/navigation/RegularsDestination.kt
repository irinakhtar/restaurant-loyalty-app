package com.regulars.ui.navigation

sealed interface RegularsDestination {
    val route: String

    data object AllRestaurants : RegularsDestination {
        override val route: String = "restaurants"
    }

    data object MyCards : RegularsDestination {
        override val route: String = "cards"
    }

    data object RestaurantDetail : RegularsDestination {
        const val RESTAURANT_ID: String = "restaurantId"
        override val route: String = "restaurants/{$RESTAURANT_ID}"

        fun route(restaurantId: String): String = "restaurants/$restaurantId"
    }

    data object Settings : RegularsDestination {
        override val route: String = "settings"
    }
}
