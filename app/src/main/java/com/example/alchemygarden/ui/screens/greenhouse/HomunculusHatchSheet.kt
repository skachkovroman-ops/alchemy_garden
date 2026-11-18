package com.example.alchemygarden.ui.screens.greenhouse

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alchemygarden.ui.theme.*
import com.example.alchemygarden.ui.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomunculusHatchSheet(
    viewModel: GameViewModel,
    onDismiss: () -> Unit
) {
    val state by viewModel.playerState.collectAsState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SoftCream
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "✨ Духи-помощники",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepForest
                )
                Button(
                    onClick = { viewModel.hatchEgg() },
                    enabled = state.unhatchedEggs > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = GoldenHarvest)
                ) {
                    Text("Пробудить яйцо (${state.unhatchedEggs})")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(viewModel.config.homunculi) { spirit ->
                    val isHatched = spirit.id in state.hatchedHomunculi
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isHatched) WarmParchment else WarmParchment.copy(alpha = 0.45f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isHatched) spirit.emoji else "❓",
                                fontSize = 28.sp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (isHatched) spirit.name else "Неизвестный дух",
                                    fontWeight = FontWeight.Bold,
                                    color = DeepForest
                                )
                                Text(
                                    text = if (isHatched) spirit.description else "Найдите яйцо в экспедиции",
                                    fontSize = 12.sp,
                                    color = MutedInk.copy(alpha = 0.75f)
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
