package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.RecordingRed
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun TimerDisplay(
    isRecording: Boolean,
    isPaused: Boolean,
    formattedTime: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "blink")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotBlink"
    )

    Column(
        modifier = modifier.testTag("timer_display_container"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Status Badge
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(DarkSurfaceVariant)
                .border(
                    width = 1.dp,
                    color = when {
                        isRecording && !isPaused -> RecordingRed.copy(alpha = 0.5f)
                        isRecording && isPaused -> StatusAmber.copy(alpha = 0.5f)
                        else -> Color(0xFF374151)
                    },
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 14.dp, vertical = 6.dp)
                .testTag("recording_status_badge"),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .alpha(if (isRecording && !isPaused) dotAlpha else 1f)
                    .background(
                        when {
                            isRecording && !isPaused -> RecordingRed
                            isRecording && isPaused -> StatusAmber
                            else -> StatusGreen
                        }
                    )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = when {
                    isRecording && !isPaused -> "RECORDING"
                    isRecording && isPaused -> "PAUSED"
                    else -> "STANDBY"
                },
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                ),
                color = when {
                    isRecording && !isPaused -> RecordingRed
                    isRecording && isPaused -> StatusAmber
                    else -> TextSecondary
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Large Elapsed Time Monospace Text
        Text(
            text = formattedTime,
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 46.sp,
                fontWeight = FontWeight.Light,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp
            ),
            color = if (isRecording && !isPaused) TextPrimary else TextSecondary,
            modifier = Modifier.testTag("timer_text")
        )

        Spacer(modifier = Modifier.height(6.dp))

        AnimatedVisibility(
            visible = isRecording,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Text(
                text = "Audio format: AAC • 44.1 kHz • 128 kbps",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary.copy(alpha = 0.8f)
            )
        }
    }
}
