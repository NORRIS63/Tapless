package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.RecordingRed
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusInactive
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun TimerDisplay(
    isRecording: Boolean,
    isPaused: Boolean,
    formattedTime: String,
    isAvailable: Boolean = true,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "recordingBlink")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(700),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotAlpha"
    )

    Column(
        modifier = modifier.testTag("timer_display_container"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Small, restrained status indicator
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(DarkSurfaceVariant)
                .padding(horizontal = 10.dp, vertical = 4.dp)
                .testTag("recording_status_badge"),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val statusColor = when {
                !isAvailable -> StatusInactive
                isRecording && !isPaused -> RecordingRed
                isRecording && isPaused -> StatusAmber
                else -> StatusGreen
            }

            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .alpha(if (isRecording && !isPaused) dotAlpha else 1f)
                    .background(statusColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = when {
                    !isAvailable -> "UNAVAILABLE"
                    isRecording && !isPaused -> "RECORDING"
                    isRecording && isPaused -> "PAUSED"
                    else -> "READY"
                },
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    letterSpacing = 0.8.sp
                ),
                color = when {
                    !isAvailable -> TextTertiary
                    isRecording && !isPaused -> RecordingRed
                    isRecording && isPaused -> StatusAmber
                    else -> TextSecondary
                }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Simple recording timer in crisp monospace font
        Text(
            text = formattedTime,
            style = MaterialTheme.typography.displayMedium.copy(
                fontSize = 40.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            ),
            color = if (isRecording && !isPaused) TextPrimary else TextSecondary,
            modifier = Modifier.testTag("timer_text")
        )
    }
}
