package com.example.alchemygarden.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.alchemygarden.data.model.PlayerState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore by preferencesDataStore(name = "alchemy_garden_prefs")

class GameStorage(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true }
    private val STATE_KEY = stringPreferencesKey("player_state_json")

    val playerStateFlow: Flow<PlayerState> = context.dataStore.data.map { prefs ->
        val raw = prefs[STATE_KEY]
        if (raw.isNullOrEmpty()) {
            PlayerState()
        } else {
            try {
                json.decodeFromString<PlayerState>(raw)
            } catch (e: Exception) {
                PlayerState()
            }
        }
    }

    suspend fun saveState(state: PlayerState) {
        context.dataStore.edit { prefs ->
            prefs[STATE_KEY] = json.encodeToString(state)
        }
    }
}
