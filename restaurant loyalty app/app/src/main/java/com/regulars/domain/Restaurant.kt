package com.regulars.domain

data class Restaurant(
    val id: String,
    val name: String,
    val cuisine: String,
    val imageUrl: String,
    val address: String,
    val openingHours: String,
    val rewardText: String,
)
