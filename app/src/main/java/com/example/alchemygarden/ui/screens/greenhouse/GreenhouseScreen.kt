package com.example.alchemygarden.ui.screens.greenhouse

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alchemygarden.data.model.ActivePot
import com.example.alchemygarden.data.model.PlantConfig
import com.example.alchemygarden.ui.components.FloatingOverlayContainer
import com.example.alchemygarden.ui.components.ParticleBackground
import com.example.alchemygarden.ui.components.SwayingPlant
import com.example.alchemygarden.ui.theme.*
import com.example.alchemygarden.ui.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GreenhouseScreen(viewModel: GameViewModel) {
    val state by viewModel.playerState.collectAsState()
    val now by viewModel.nowMillis.collectAsState()
    var selectedPotIndex by remember { mutableStateOf<Int?>(null) }
    var showHatchSheet by remember { mutableStateOf(false) }

    FloatingOverlayContainer { showPopup ->
        Box(modifier = Modifier.fillMaxSize()) {
            ParticleBackground(particleCount = 22, particleColor = GoldenHarvest)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "🌿 Оранжерея",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepForest
                        )
                        Text(
                            text = "Выращивай травы для экспедиций",
                            fontSize = 13.sp,
                            color = MutedInk.copy(alpha = 0.7f)
                        )
                    }

                    FilledTonalButton(
                        onClick = { showHatchSheet = true },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = GoldenHarvest.copy(alpha = 0.25f)
                        )
                    ) {
                        Text("🥚 Духи (${state.unhatchedEggs})")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    itemsIndexed(state.activePots) { index, pot ->
                        val plant = viewModel.config.plants.find { it.id == pot.plantId }
                        PotCard(
                            pot = pot,
                            plant = plant,
                            nowMillis = now,
                            onEmptyClick = { selectedPotIndex = index },
                            onReadyClick = { coords ->
                                val amount = viewModel.harvestPot(index)
                                if (amount > 0 && plant != null) {
                                    showPopup("+$amount ${plant.emoji}", coords.x, coords.y)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (selectedPotIndex != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedPotIndex = null },
            containerColor = SoftCream
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Выбери семена для посадки",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepForest
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (state.seeds.isEmpty()) {
                    Text(
                        text = "Семян пока нет. Отправься в экспедицию на карте!",
                        color = MutedInk.copy(alpha = 0.7f),
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    state.seeds.forEach { (plantId, count) ->
                        val plant = viewModel.config.plants.find { it.id == plantId }
                        if (plant != null && count > 0) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        viewModel.plantSeed(selectedPotIndex!!, plantId)
                                        selectedPotIndex = null
                                    },
                                colors = CardDefaults.cardColors(containerColor = WarmParchment)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(plant.emoji, fontSize = 26.sp)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(plant.name, fontWeight = FontWeight.SemiBold)
                                            Text(
                                                "Рост: ${plant.growthTimeSeconds} сек • Урожай: +${plant.yield}",
                                                fontSize = 12.sp,
                                                color = MutedInk.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                    Badge(containerColor = MossGreen) {
                                        Text("x$count", color = SoftCream, modifier = Modifier.padding(4.dp))
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showHatchSheet) {
        HomunculusHatchSheet(
            viewModel = viewModel,
            onDismiss = { showHatchSheet = false }
        )
    }
}

@Composable
private fun PotCard(
    pot: ActivePot,
    plant: PlantConfig?,
    nowMillis: Long,
    onEmptyClick: () -> Unit,
    onReadyClick: (Offset) -> Unit
) {
    var cardPosition by remember { mutableStateOf(Offset.Zero) }

    val progress = if (pot.plantId != null && pot.durationMillis > 0) {
        ((nowMillis - pot.plantedAtMillis).toFloat() / pot.durationMillis).coerceIn(0f, 1f)
    } else 0f

    val isReady = pot.plantId != null && progress >= 1f

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SoftCream),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(175.dp)
            .onGloballyPositioned { coords ->
                cardPosition = coords.positionInRoot()
            }
            .clickable {
                when {
                    pot.plantId == null -> onEmptyClick()
                    isReady -> onReadyClick(Offset(cardPosition.x + 80f, cardPosition.y + 40f))
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isReady -> GoldenHarvest.copy(alpha = 0.2f)
                            pot.plantId != null -> MossGreen.copy(alpha = 0.14f)
                            else -> TerracottaClay.copy(alpha = 0.12f)
                        }
                    )
            ) {
                val emoji = when {
                    pot.plantId == null -> "🪴"
                    isReady -> plant?.emoji ?: "🌺"
                    progress > 0.5f -> "🌿"
                    else -> "🌱"
                }

                if (pot.plantId != null) {
                    SwayingPlant(isReady = isReady) {
                        Text(text = emoji, fontSize = 34.sp)
                    }
                } else {
                    Text(text = emoji, fontSize = 34.sp)
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = plant?.name ?: "Пустой горшок",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = DeepForest
                )

                Spacer(modifier = Modifier.height(6.dp))

                when {
                    pot.plantId == null -> {
                        Text(
                            text = "Нажми, чтобы посадить",
                            fontSize = 11.sp,
                            color = TerracottaClay
                        )
                    }
                    isReady -> {
                        Text(
                            text = "✨ Готово! Собрать",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldenHarvest
                        )
                    }
                    else -> {
                        val remSeconds = ((pot.durationMillis - (nowMillis - pot.plantedAtMillis)) / 1000L).coerceAtLeast(0)
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = MossGreen,
                            trackColor = MossGreen.copy(alpha = 0.2f)
                        )
                        Text(
                            text = "Осталось ${remSeconds}с",
                            fontSize = 11.sp,
                            color = MutedInk.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}
