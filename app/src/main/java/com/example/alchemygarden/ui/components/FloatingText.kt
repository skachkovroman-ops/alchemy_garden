package com.example.alchemygarden.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import com.example.alchemygarden.ui.theme.GoldenHarvest
import kotlin.math.roundToInt
import kotlin.random.Random

data class FloatingTextItem(
    val id: Long = System.currentTimeMillis() + Random.nextLong(10000),
    val text: String,
    val x: Float,
    val y: Float
)

@Composable
fun FloatingLabel(
    item: FloatingTextItem,
    onFinished: (Long) -> Unit
) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(item.id) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing)
        )
        onFinished(item.id)
    }

    val offsetY = -progress.value * 90f
    val alphaAnim = if (progress.value > 0.5f) {
        1f - (progress.value - 0.5f) * 2f
    } else {
        1f
    }

    Text(
        text = item.text,
        color = GoldenHarvest,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .offset {
                IntOffset(
                    x = item.x.roundToInt(),
                    y = (item.y + offsetY).roundToInt()
                )
            }
            .alpha(alphaAnim.coerceIn(0f, 1f))
    )
}

@Composable
fun FloatingOverlayContainer(
    content: @Composable (showPopup: (String, Float, Float) -> Unit) -> Unit
) {
    var floatingTexts by remember { mutableStateOf(listOf<FloatingTextItem>()) }

    Box(modifier = Modifier.fillMaxSize()) {
        content { text, startX, startY ->
            floatingTexts = floatingTexts + FloatingTextItem(text = text, x = startX, y = startY)
        }

        floatingTexts.forEach { item ->
            FloatingLabel(
                item = item,
                onFinished = { id ->
                    floatingTexts = floatingTexts.filterNot { it.id == id }
                }
            )
        }
    }
}
