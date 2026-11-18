package com.example.alchemygarden.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.alchemygarden.data.local.AssetConfigLoader
import com.example.alchemygarden.data.local.GameStorage
import com.example.alchemygarden.data.model.*
import com.example.alchemygarden.data.worker.ExpeditionWorker
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val storage = GameStorage(application)
    val config: GameConfig = AssetConfigLoader(application).loadConfig()

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private val _nowMillis = MutableStateFlow(System.currentTimeMillis())
    val nowMillis: StateFlow<Long> = _nowMillis.asStateFlow()

    private val _lastEventMessage = MutableStateFlow<String?>(null)
    val lastEventMessage: StateFlow<String?> = _lastEventMessage.asStateFlow()

    init {
        viewModelScope.launch {
            storage.playerStateFlow.collect { saved ->
                _playerState.value = saved
            }
        }
        viewModelScope.launch {
            while (true) {
                delay(500L)
                _nowMillis.value = System.currentTimeMillis()
            }
        }
    }

    fun clearEventMessage() {
        _lastEventMessage.value = null
    }

    private fun getBonusMultiplier(bonusType: String): Float {
        val state = _playerState.value
        val totalBonus = config.homunculi
            .filter { it.id in state.hatchedHomunculi && it.bonusType == bonusType }
            .sumOf { it.bonusValue.toDouble() }
            .toFloat()
        return (1f - totalBonus).coerceAtLeast(0.3f)
    }

    fun plantSeed(potIndex: Int, plantId: String) {
        val state = _playerState.value
        val plant = config.plants.find { it.id == plantId } ?: return
        val currentSeeds = state.seeds[plantId] ?: 0
        if (currentSeeds <= 0) return

        val speedMult = getBonusMultiplier("growth_speed")
        val duration = (plant.growthTimeSeconds * 1000L * speedMult).toLong()

        val updatedSeeds = state.seeds.toMutableMap()
        if (currentSeeds == 1) updatedSeeds.remove(plantId) else updatedSeeds[plantId] = currentSeeds - 1

        val updatedPots = state.activePots.mapIndexed { idx, pot ->
            if (idx == potIndex) {
                pot.copy(
                    plantId = plantId,
                    plantedAtMillis = System.currentTimeMillis(),
                    durationMillis = duration
                )
            } else pot
        }

        updateAndSave(state.copy(seeds = updatedSeeds, activePots = updatedPots))
    }

    fun harvestPot(potIndex: Int): Int {
        val state = _playerState.value
        val pot = state.activePots.getOrNull(potIndex) ?: return 0
        val plantId = pot.plantId ?: return 0
        val plant = config.plants.find { it.id == plantId } ?: return 0

        val now = System.currentTimeMillis()
        if (now - pot.plantedAtMillis < pot.durationMillis) return 0

        val yieldAmount = plant.yield
        val updatedHarvest = state.harvest.toMutableMap()
        updatedHarvest[plantId] = (updatedHarvest[plantId] ?: 0) + yieldAmount

        val updatedSeeds = state.seeds.toMutableMap()
        if (Random.nextFloat() < 0.45f) {
            updatedSeeds[plantId] = (updatedSeeds[plantId] ?: 0) + 1
        }

        val updatedPots = state.activePots.mapIndexed { idx, p ->
            if (idx == potIndex) ActivePot(id = idx) else p
        }

        val discovered = (state.discoveredPlants + plantId).distinct()

        updateAndSave(
            state.copy(
                harvest = updatedHarvest,
                seeds = updatedSeeds,
                activePots = updatedPots,
                discoveredPlants = discovered
            )
        )
        return yieldAmount
    }

    fun startCraft(recipeId: String) {
        val state = _playerState.value
        if (state.activeCraft != null) return
        val recipe = config.recipes.find { it.id == recipeId } ?: return

        for ((ingId, reqAmount) in recipe.ingredients) {
            if ((state.harvest[ingId] ?: 0) < reqAmount) return
        }

        val updatedHarvest = state.harvest.toMutableMap()
        for ((ingId, reqAmount) in recipe.ingredients) {
            val left = (updatedHarvest[ingId] ?: 0) - reqAmount
            if (left <= 0) updatedHarvest.remove(ingId) else updatedHarvest[ingId] = left
        }

        val speedMult = getBonusMultiplier("craft_speed")
        val duration = (recipe.craftTimeSeconds * 1000L * speedMult).toLong()

        val craft = ActiveCraft(
            recipeId = recipeId,
            startedAtMillis = System.currentTimeMillis(),
            durationMillis = duration
        )

        updateAndSave(state.copy(harvest = updatedHarvest, activeCraft = craft))
    }

    fun collectCraft() {
        val state = _playerState.value
        val craft = state.activeCraft ?: return
        val now = System.currentTimeMillis()
        if (now - craft.startedAtMillis < craft.durationMillis) return

        val updatedSupplies = state.supplies.toMutableMap()
        updatedSupplies[craft.recipeId] = (updatedSupplies[craft.recipeId] ?: 0) + 1

        updateAndSave(state.copy(supplies = updatedSupplies, activeCraft = null))
        _lastEventMessage.value = "Припас готов и добавлен в сумку!"
    }

    fun unlockBiome(biomeId: String) {
        val state = _playerState.value
        if (biomeId in state.unlockedBiomes) return
        val biome = config.biomes.find { it.id == biomeId } ?: return

        for ((plantId, cost) in biome.unlockCost) {
            if ((state.harvest[plantId] ?: 0) < cost) return
        }

        val updatedHarvest = state.harvest.toMutableMap()
        for ((plantId, cost) in biome.unlockCost) {
            val left = (updatedHarvest[plantId] ?: 0) - cost
            if (left <= 0) updatedHarvest.remove(plantId) else updatedHarvest[plantId] = left
        }

        updateAndSave(
            state.copy(
                harvest = updatedHarvest,
                unlockedBiomes = state.unlockedBiomes + biomeId
            )
        )
        _lastEventMessage.value = "Открыта локация: ${biome.name}!"
    }

    fun startExpedition(biomeId: String) {
        val state = _playerState.value
        if (state.activeExpedition != null) return
        if (biomeId !in state.unlockedBiomes) return
        val biome = config.biomes.find { it.id == biomeId } ?: return

        for ((supId, req) in biome.requiredSupplies) {
            if ((state.supplies[supId] ?: 0) < req) return
        }

        val updatedSupplies = state.supplies.toMutableMap()
        for ((supId, req) in biome.requiredSupplies) {
            val left = (updatedSupplies[supId] ?: 0) - req
            if (left <= 0) updatedSupplies.remove(supId) else updatedSupplies[supId] = left
        }

        val speedMult = getBonusMultiplier("expedition_speed")
        val duration = (biome.expeditionDurationSeconds * 1000L * speedMult).toLong()

        scheduleNotification(
            delayMillis = duration,
            title = "Возвращение из экспедиции",
            message = "Алхимик вернулся из локации «${biome.name}»!"
        )

        val expedition = ActiveExpedition(
            biomeId = biomeId,
            startedAtMillis = System.currentTimeMillis(),
            durationMillis = duration
        )

        updateAndSave(state.copy(supplies = updatedSupplies, activeExpedition = expedition))
    }

    fun claimExpeditionReward() {
        val state = _playerState.value
        val exp = state.activeExpedition ?: return
        val now = System.currentTimeMillis()
        if (now - exp.startedAtMillis < exp.durationMillis) return

        val biome = config.biomes.find { it.id == exp.biomeId }
        val updatedSeeds = state.seeds.toMutableMap()
        val foundNames = mutableListOf<String>()

        if (biome != null && biome.possibleRewards.isNotEmpty()) {
            repeat(2) {
                val rewardPlantId = biome.possibleRewards.random()
                updatedSeeds[rewardPlantId] = (updatedSeeds[rewardPlantId] ?: 0) + 1
                val plantName = config.plants.find { it.id == rewardPlantId }?.name ?: rewardPlantId
                foundNames.add(plantName)
            }
        }

        var eggs = state.unhatchedEggs
        var eggFoundText = ""
        if (biome != null && Random.nextFloat() < biome.homunculusEggChance) {
            eggs += 1
            eggFoundText = " И найдено таинственное яйцо духа! 🥚"
        }

        updateAndSave(
            state.copy(
                seeds = updatedSeeds,
                unhatchedEggs = eggs,
                activeExpedition = null
            )
        )
        _lastEventMessage.value = "Найдены семена: ${foundNames.joinToString(", ")}.$eggFoundText"
    }

    fun hatchEgg() {
        val state = _playerState.value
        if (state.unhatchedEggs <= 0) return

        val lockedHomunculi = config.homunculi.filter { it.id !in state.hatchedHomunculi }
        if (lockedHomunculi.isEmpty()) {
            _lastEventMessage.value = "Все духи-помощники уже пробуждены!"
            return
        }

        val chosen = lockedHomunculi.random()
        updateAndSave(
            state.copy(
                unhatchedEggs = state.unhatchedEggs - 1,
                hatchedHomunculi = state.hatchedHomunculi + chosen.id
            )
        )
        _lastEventMessage.value = "Пробужден новый помощник: ${chosen.emoji} ${chosen.name}!"
    }

    private fun scheduleNotification(delayMillis: Long, title: String, message: String) {
        val request = OneTimeWorkRequestBuilder<ExpeditionWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf("title" to title, "message" to message))
            .build()
        WorkManager.getInstance(getApplication()).enqueue(request)
    }

    private fun updateAndSave(newState: PlayerState) {
        _playerState.value = newState
        viewModelScope.launch {
            storage.saveState(newState)
        }
    }
}
