package com.example.alchemygarden.ui.screens.laboratory

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
import com.example.alchemygarden.ui.theme.*
import com.example.alchemygarden.ui.viewmodel.GameViewModel

@Composable
fun LaboratoryScreen(viewModel: GameViewModel) {
    val state by viewModel.playerState.collectAsState()
    val now by viewModel.nowMillis.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "⚗️ Лаборатория",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = DeepForest
        )
        Text(
            text = "Создавай припасы для путешествий",
            fontSize = 13.sp,
            color = MutedInk.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Статус котла
        val activeCraft = state.activeCraft
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SoftCream),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (activeCraft == null) {
                    Text("🔥 Алхимический котел свободен", fontWeight = FontWeight.SemiBold, color = DeepForest)
                    Text("Выбери рецепт ниже, чтобы начать варку.", fontSize = 12.sp, color = MutedInk.copy(alpha = 0.7f))
                } else {
                    val recipe = viewModel.config.recipes.find { it.id == activeCraft.recipeId }
                    val progress = ((now - activeCraft.startedAtMillis).toFloat() / activeCraft.durationMillis).coerceIn(0f, 1f)
                    val isDone = progress >= 1f

                    Text(
                        text = "${recipe?.emoji ?: "🧪"} Варится: ${recipe?.name ?: ""}",
                        fontWeight = FontWeight.Bold,
                        color = DeepForest
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (isDone) {
                        Button(
                            onClick = { viewModel.collectCraft() },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldenHarvest),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Забрать готовый припас ✨")
                        }
                    } else {
                        val rem = ((activeCraft.durationMillis - (now - activeCraft.startedAtMillis)) / 1000L).coerceAtLeast(0)
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = TerracottaClay
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Осталось ${rem} сек...", fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Рецепты припасов", fontWeight = FontWeight.Bold, color = DeepForest, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(viewModel.config.recipes) { recipe ->
                val canCraft = activeCraft == null && recipe.ingredients.all { (id, req) ->
                    (state.harvest[id] ?: 0) >= req
                }
                val ownedCount = state.supplies[recipe.id] ?: 0

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SoftCream),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(recipe.emoji, fontSize = 26.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(recipe.name, fontWeight = FontWeight.Bold, color = DeepForest)
                                    Text(recipe.description, fontSize = 12.sp, color = MutedInk.copy(alpha = 0.7f))
                                }
                            }
                            Text("В сумке: $ownedCount", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MossGreen)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                recipe.ingredients.forEach { (plantId, req) ->
                                    val plant = viewModel.config.plants.find { it.id == plantId }
                                    val have = state.harvest[plantId] ?: 0
                                    Text(
                                        text = "${plant?.emoji ?: "🌿"} ${plant?.name ?: plantId}: $have / $req",
                                        fontSize = 12.sp,
                                        color = if (have >= req) MossGreen else TerracottaClay
                                    )
                                }
                            }

                            Button(
                                onClick = { viewModel.startCraft(recipe.id) },
                                enabled = canCraft,
                                colors = ButtonDefaults.buttonColors(containerColor = MossGreen)
                            ) {
                                Text("Сварить (${recipe.craftTimeSeconds}с)")
                            }
                        }
                    }
                }
            }
        }
    }
}
