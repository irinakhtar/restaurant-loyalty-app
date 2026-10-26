package com.regulars.data

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RestaurantContractTest {
    @Test
    fun eachRestaurantHasTheFieldsTheScreensWillLoad() {
        val restaurants =
            Json
                .parseToJsonElement(contractFile().readText())
                .jsonObject
                .getValue("restaurants")
                .jsonArray

        assertEquals(6, restaurants.size)
        restaurants.forEach { element ->
            val restaurant = element.jsonObject
            assertTrue(restaurant.keys.containsAll(REQUIRED_FIELDS))
            REQUIRED_FIELDS.forEach { field ->
                assertTrue(
                    restaurant
                        .getValue(field)
                        .jsonPrimitive.content
                        .isNotBlank(),
                    "Expected $field to be filled in for ${restaurant["name"]}",
                )
            }
        }

        val cuisines =
            restaurants.map { restaurant ->
                restaurant.jsonObject
                    .getValue("cuisine")
                    .jsonPrimitive.content
            }
        assertTrue(cuisines.containsAll(listOf("Café", "Pizza", "Asian")))

        val rewards =
            restaurants.map { restaurant ->
                restaurant.jsonObject
                    .getValue("rewardText")
                    .jsonPrimitive.content
            }
        assertTrue(rewards.contains("Free coffee"))
    }

    private fun contractFile(): File {
        val candidates =
            listOf(
                File("src/main/assets/restaurants.json"),
                File("app/src/main/assets/restaurants.json"),
            )
        return candidates.firstOrNull { it.isFile }
            ?: error("Mock restaurant contract was not found.")
    }

    private companion object {
        val REQUIRED_FIELDS =
            setOf(
                "id",
                "name",
                "cuisine",
                "imageUrl",
                "address",
                "openingHours",
                "rewardText",
            )
    }
}
