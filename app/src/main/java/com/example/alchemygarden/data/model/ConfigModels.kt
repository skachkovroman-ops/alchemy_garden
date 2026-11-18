package com.example.alchemygarden.data.model

import kotlinx.serialization.Serializable

@Serializable
data class GameConfig(
    val plants: List<PlantConfig> = emptyList(),
    val recipes: List<RecipeConfig> = emptyList(),
    val biomes: List<BiomeConfig> = emptyList(),
    val homunculi: List<HomunculusConfig> = emptyList()
)

@Serializable
data class PlantConfig(
    val id: String,
    val name: String,
    val emoji: String = "🌿",
    val description: String = "",
    val growthTimeSeconds: Long,
    val yield: Int,
    val biomeId: String
)

@Serializable
data class RecipeConfig(
    val id: String,
    val name: String,
    val emoji: String = "🧪",
    val description: String = "",
    val ingredients: Map<String, Int>,
    val craftTimeSeconds: Long
)

@Serializable
data class BiomeConfig(
    val id: String,
    val name: String,
    val emoji: String = "🌲",
    val description: String = "",
    val unlockedByDefault: Boolean,
    val unlockCost: Map<String, Int> = emptyMap(),
    val expeditionDurationSeconds: Long = 60L,
    val requiredSupplies: Map<String, Int> = emptyMap(),
    val possibleRewards: List<String> = emptyList(),
    val homunculusEggChance: Float = 0.3f
)

@Serializable
data class HomunculusConfig(
    val id: String,
    val name: String,
    val emoji: String = "✨",
    val description: String = "",
    val bonusType: String,
    val bonusValue: Float
)
