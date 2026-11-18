package com.example.alchemygarden.ui.components

import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.isActive
import kotlin.random.Random

data class Particle(
    val x: Float,
    val y: Float,
    val radius: Float,
    val speedY: Float,
    val speedX: Float,
    val alpha: Float,
    val color: Color
)

@Composable
fun ParticleBackground(
    modifier: Modifier = Modifier,
    particleCount: Int = 25,
    particleColor: Color = Color(0xFFE6A132)
) {
    var particles by remember { mutableStateOf<List<Particle>>(emptyList()) }
    var canvasWidth by remember { mutableFloatStateOf(1080f) }
    var canvasHeight by remember { mutableFloatStateOf(1920f) }

    LaunchedEffect(Unit) {
        var lastTime = System.currentTimeMillis()
        while (isActive) {
            withInfiniteAnimationFrameMillis { currentTime ->
                val dt = ((currentTime - lastTime) / 1000f).coerceIn(0f, 0.1f)
                lastTime = currentTime

                if (particles.isNotEmpty()) {
                    particles = particles.map { p ->
                        var newY = p.y + (p.speedY * 60f * dt)
                        var newX = p.x + (p.speedX * 60f * dt)

                        if (newY < 0f) {
                            newY = canvasHeight
                            newX = Random.nextFloat() * canvasWidth
                        }
                        p.copy(x = newX, y = newY)
                    }
                }
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        canvasWidth = size.width
        canvasHeight = size.height

        if (particles.isEmpty() && size.width > 0f && size.height > 0f) {
            particles = List(particleCount) {
                Particle(
                    x = Random.nextFloat() * size.width,
                    y = Random.nextFloat() * size.height,
                    radius = Random.nextFloat() * 5f + 2f,
                    speedY = -(Random.nextFloat() * 0.6f + 0.2f),
                    speedX = (Random.nextFloat() - 0.5f) * 0.3f,
                    alpha = Random.nextFloat() * 0.35f + 0.12f,
                    color = particleColor
                )
            }
        }

        particles.forEach { p ->
            drawCircle(
                color = p.color.copy(alpha = p.alpha),
                radius = p.radius,
                center = Offset(p.x, p.y)
            )
        }
    }
}
