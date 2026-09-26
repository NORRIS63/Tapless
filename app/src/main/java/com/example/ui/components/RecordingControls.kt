package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.RecordingRed
import com.example.ui.theme.RecordingRedGlow
import com.example.ui.theme.StatusAmber

@Composable
fun RecordingControls(
    isRecording: Boolean,
    isPaused: Boolean,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onPauseRecording: () -> Unit,
    onResumeRecording: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    val infiniteTransition = rememberInfiniteTransition(label = "ringPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseRing"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Pause / Resume Button (Visible only when recording)
        AnimatedVisibility(
            visible = isRecording,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            Surface(
                modifier = Modifier
                    .padding(end = 28.dp)
                    .size(54.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color(0xFF374151), CircleShape),
                color = DarkSurfaceVariant,
                shape = CircleShape
            ) {
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (isPaused) onResumeRecording() else onPauseRecording()
                    },
                    modifier = Modifier.testTag(
                        if (isPaused) "resume_recording_button" else "pause_recording_button"
                    )
                ) {
                    Icon(
                        imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = if (isPaused) "Resume recording" else "Pause recording",
                        tint = if (isPaused) StatusAmber else Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        // Main Record / Stop Action Button
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(104.dp)
        ) {
            // Glowing pulse ring when active
            if (isRecording && !isPaused) {
                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(RecordingRedGlow)
                )
            }

            if (!isRecording) {
                // START RECORDING BUTTON
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFFF5252),
                                    RecordingRed,
                                    Color(0xFFCC1F1A)
                                )
                            )
                        )
                        .border(3.dp, Color(0x66FFFFFF), CircleShape)
                        .clickable(
                            role = Role.Button,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = false, radius = 44.dp)
                        ) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onStartRecording()
                        }
                        .testTag("start_recording_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Start Recording",
                        tint = Color.White,
                        modifier = Modifier.size(38.dp)
                    )
                }
            } else {
                // STOP RECORDING BUTTON
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFE53935),
                                    Color(0xFFB71C1C)
                                )
                            )
                        )
                        .border(3.dp, Color(0x88FFCDD2), CircleShape)
                        .clickable(
                            role = Role.Button,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = false, radius = 44.dp)
                        ) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onStopRecording()
                        }
                        .testTag("stop_recording_button"),
                    contentAlignment = Alignment.Center
                ) {
                    // Square Stop icon
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White)
                    )
                }
            }
        }

        // Placeholder spacer or symmetrical spacing when pause button is visible
        if (isRecording) {
            Box(
                modifier = Modifier
                    .padding(start = 28.dp)
                    .size(54.dp)
            )
        }
    }
}
