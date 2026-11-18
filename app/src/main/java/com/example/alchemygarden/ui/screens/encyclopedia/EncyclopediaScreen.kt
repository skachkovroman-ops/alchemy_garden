package com.example.alchemygarden.ui.screens.encyclopedia

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alchemygarden.ui.theme.*
import com.example.alchemygarden.ui.viewmodel.GameViewModel

@Composable
fun EncyclopediaScreen(viewModel: GameViewModel) {
    val state by viewModel.playerState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "📖 Энциклопедия и Склад",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = DeepForest
        )

        Spacer(modifier = Modifier.height(12.dp))

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = SoftCream,
            contentColor = MossGreen
        ) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Гербарий") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Бестиарий") })
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedTab == 0) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(viewModel.config.plants) { plant ->
                    val isDiscovered = plant.id in state.discoveredPlants || (state.seeds[plant.id] ?: 0) > 0
                    val harvestCount = state.harvest[plant.id] ?: 0
                    val seedCount = state.seeds[plant.id] ?: 0

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SoftCream),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(if (isDiscovered) plant.emoji else "❓", fontSize = 28.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = if (isDiscovered) plant.name else "Неизвестное растение",
                                        fontWeight = FontWeight.Bold,
                                        color = DeepForest
                                    )
                                    Text(
                                        text = if (isDiscovered) plant.description else "Исследуй новые локации на карте",
                                        fontSize = 12.sp,
                                        color = MutedInk.copy(alpha = 0.7f)
                                    )
                                }
                            }
                            if (isDiscovered) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Урожай: $harvestCount", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MossGreen)
                                    Text("Семена: $seedCount", fontSize = 11.sp, color = TerracottaClay)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(viewModel.config.homunculi) { spirit ->
                    val isHatched = spirit.id in state.hatchedHomunculi
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SoftCream),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(if (isHatched) spirit.emoji else "🥚", fontSize = 28.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (isHatched) spirit.name else "Спящий дух",
                                    fontWeight = FontWeight.Bold,
                                    color = DeepForest
                                )
                                Text(
                                    text = if (isHatched) spirit.description else "Пробуди из яйца в оранжерее",
                                    fontSize = 12.sp,
                                    color = MutedInk.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
