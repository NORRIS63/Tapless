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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DarkDivider
import com.example.ui.theme.RecordingRed
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.TealAccent

@Composable
fun AudioVisualizer(
    isRecording: Boolean,
    isPaused: Boolean,
    amplitudes: List<Float>,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "visualizerIdle")
    val idlePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "idlePhase"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
    ) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f
        val barCount = 28
        val barWidth = 3f.coerceAtLeast((width / (barCount * 2.2f)))
        val totalBarsWidth = barCount * barWidth
        val space = ((width - totalBarsWidth) / (barCount - 1)).coerceAtLeast(2f)

        val activeColor = when {
            isRecording && !isPaused -> RecordingRed
            isRecording && isPaused -> StatusAmber
            else -> DarkDivider
        }

        for (i in 0 until barCount) {
            val ampValue = if (isRecording && !isPaused) {
                val index = amplitudes.size - barCount + i
                if (index in amplitudes.indices) {
                    amplitudes[index]
                } else {
                    0.04f
                }
            } else if (isRecording && isPaused) {
                0.08f
            } else {
                // Subtle calm baseline
                val sine = (kotlin.math.sin(idlePhase + (i * 0.22f)) + 1f) / 2f
                0.03f + (sine.toFloat() * 0.05f)
            }

            val minHeight = 4f
            val barHeight = (ampValue * (height - 6f) + minHeight).coerceIn(minHeight, height)
            val x = i * (barWidth + space)
            val y = centerY - (barHeight / 2f)

            drawRoundRect(
                color = if (isRecording) activeColor else Color(0xFF282B34),
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}
