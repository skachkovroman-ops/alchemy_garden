package com.example.alchemygarden.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PlayerState(
    val seeds: Map<String, Int> = mapOf("moon_flower" to 3, "sun_herb" to 2),
    val harvest: Map<String, Int> = emptyMap(),
    val supplies: Map<String, Int> = emptyMap(),
    val activePots: List<ActivePot> = List(6) { ActivePot(id = it) },
    val activeCraft: ActiveCraft? = null,
    val activeExpedition: ActiveExpedition? = null,
    val unlockedBiomes: List<String> = listOf("misty_forest"),
    val discoveredPlants: List<String> = listOf("moon_flower", "sun_herb"),
    val unhatchedEggs: Int = 1,
    val hatchedHomunculi: List<String> = emptyList()
)

@Serializable
data class ActivePot(
    val id: Int,
    val plantId: String? = null,
    val plantedAtMillis: Long = 0L,
    val durationMillis: Long = 0L
)

@Serializable
data class ActiveCraft(
    val recipeId: String,
    val startedAtMillis: Long,
    val durationMillis: Long
)

@Serializable
data class ActiveExpedition(
    val biomeId: String,
    val startedAtMillis: Long,
    val durationMillis: Long
)
