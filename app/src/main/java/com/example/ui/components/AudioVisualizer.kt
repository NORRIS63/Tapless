package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.PlaybackCyan
import com.example.ui.theme.RecordingRed

@Composable
fun AudioVisualizer(
    isRecording: Boolean,
    isPaused: Boolean,
    amplitudes: List<Float>,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val idlePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "idleWave"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
    ) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f
        val barCount = 36
        val barWidth = (width / (barCount * 1.5f)).coerceAtLeast(3f)
        val space = (width - (barCount * barWidth)) / (barCount - 1)

        val gradient = Brush.verticalGradient(
            colors = if (isRecording && !isPaused) {
                listOf(RecordingRed, PlaybackCyan)
            } else if (isPaused) {
                listOf(Color(0xFFF59E0B), Color(0x66F59E0B))
            } else {
                listOf(Color(0xFF4B5563), Color(0xFF1F2937))
            }
        )

        for (i in 0 until barCount) {
            val ampValue = if (isRecording && !isPaused) {
                val index = amplitudes.size - barCount + i
                if (index in amplitudes.indices) {
                    amplitudes[index]
                } else {
                    0.05f
                }
            } else if (isRecording && isPaused) {
                0.08f
            } else {
                // gentle idle wave
                val sine = (kotlin.math.sin(idlePhase + (i * 0.2f)) + 1f) / 2f
                0.04f + (sine.toFloat() * 0.08f)
            }

            // Height calculation
            val minHeight = 6f
            val barHeight = (ampValue * (height - 8f) + minHeight).coerceIn(minHeight, height)
            val x = i * (barWidth + space)
            val y = centerY - (barHeight / 2f)

            drawRoundRect(
                brush = gradient,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}
