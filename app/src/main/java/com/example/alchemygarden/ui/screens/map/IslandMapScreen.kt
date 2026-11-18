package com.example.alchemygarden.ui.screens.map

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alchemygarden.ui.components.ParticleBackground
import com.example.alchemygarden.ui.theme.*
import com.example.alchemygarden.ui.viewmodel.GameViewModel

@Composable
fun IslandMapScreen(viewModel: GameViewModel) {
    val state by viewModel.playerState.collectAsState()
    val now by viewModel.nowMillis.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        ParticleBackground(particleCount = 18, particleColor = MossGreen)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "🗺️ Карта Острова",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = DeepForest
            )
            Text(
                text = "Исследуй биомы и собирай редкие семена",
                fontSize = 13.sp,
                color = MutedInk.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            val exp = state.activeExpedition
            if (exp != null) {
                val biome = viewModel.config.biomes.find { it.id == exp.biomeId }
                val progress = ((now - exp.startedAtMillis).toFloat() / exp.durationMillis).coerceIn(0f, 1f)
                val isReturned = progress >= 1f

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SoftCream),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "🧭 Экспедиция: ${biome?.name ?: ""}",
                            fontWeight = FontWeight.Bold,
                            color = DeepForest
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        if (isReturned) {
                            Button(
                                onClick = { viewModel.claimExpeditionReward() },
                                colors = ButtonDefaults.buttonColors(containerColor = GoldenHarvest),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Открыть рюкзак с находками 🎒")
                            }
                        } else {
                            val rem = ((exp.durationMillis - (now - exp.startedAtMillis)) / 1000L).coerceAtLeast(0)
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = MossGreen
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Алхимик в пути... Осталось ${rem} сек", fontSize = 12.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(viewModel.config.biomes) { biome ->
                    val isUnlocked = biome.id in state.unlockedBiomes

                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUnlocked) SoftCream else SoftCream.copy(alpha = 0.65f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(if (isUnlocked) biome.emoji else "🌫️", fontSize = 30.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = if (isUnlocked) biome.name else "${biome.name} (Туман)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = DeepForest
                                    )
                                    Text(
                                        text = biome.description,
                                        fontSize = 12.sp,
                                        color = MutedInk.copy(alpha = 0.7f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            if (!isUnlocked) {
                                val canUnlock = biome.unlockCost.all { (pId, cost) ->
                                    (state.harvest[pId] ?: 0) >= cost
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Для рассеивания тумана:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                        biome.unlockCost.forEach { (pId, cost) ->
                                            val plant = viewModel.config.plants.find { it.id == pId }
                                            val have = state.harvest[pId] ?: 0
                                            Text(
                                                "${plant?.emoji ?: "🌿"} ${plant?.name ?: pId}: $have/$cost",
                                                fontSize = 12.sp,
                                                color = if (have >= cost) MossGreen else TerracottaClay
                                            )
                                        }
                                    }
                                    Button(
                                        onClick = { viewModel.unlockBiome(biome.id) },
                                        enabled = canUnlock,
                                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaClay)
                                    ) {
                                        Text("Открыть")
                                    }
                                }
                            } else {
                                val canExplore = exp == null && biome.requiredSupplies.all { (sId, req) ->
                                    (state.supplies[sId] ?: 0) >= req
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Нужные припасы:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                        biome.requiredSupplies.forEach { (sId, req) ->
                                            val rec = viewModel.config.recipes.find { it.id == sId }
                                            val have = state.supplies[sId] ?: 0
                                            Text(
                                                "${rec?.emoji ?: "🎒"} ${rec?.name ?: sId}: $have/$req",
                                                fontSize = 12.sp,
                                                color = if (have >= req) MossGreen else TerracottaClay
                                            )
                                        }
                                    }
                                    Button(
                                        onClick = { viewModel.startExpedition(biome.id) },
                                        enabled = canExplore,
                                        colors = ButtonDefaults.buttonColors(containerColor = MossGreen)
                                    ) {
                                        Text("В поход (${biome.expeditionDurationSeconds}с)")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
