package com.example.alchemygarden.data.local

import android.content.Context
import com.example.alchemygarden.data.model.GameConfig
import kotlinx.serialization.json.Json

class AssetConfigLoader(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true }

    fun loadConfig(): GameConfig {
        return try {
            val raw = context.assets.open("game_config.json").bufferedReader().use { it.readText() }
            json.decodeFromString<GameConfig>(raw)
        } catch (e: Exception) {
            e.printStackTrace()
            GameConfig()
        }
    }
}
