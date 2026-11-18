package com.example.alchemygarden.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.graphicsLayer
import androidx.compose.ui.graphics.TransformOrigin

@Composable
fun SwayingPlant(
    modifier: Modifier = Modifier,
    isReady: Boolean = false,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "plant_sway")

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = -3.5f,
        targetValue = 3.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isReady) 1200 else 1800,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )

    val scaleEffect by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = modifier.graphicsLayer(
            transformOrigin = TransformOrigin(0.5f, 1.0f),
            rotationZ = if (isReady) rotationAngle * 1.25f else rotationAngle,
            scaleX = if (isReady) scaleEffect else 1.0f,
            scaleY = if (isReady) scaleEffect else 1.0f
        )
    ) {
        content()
    }
}
