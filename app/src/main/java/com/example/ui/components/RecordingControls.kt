package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.RecordingRed
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.TealAccent
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

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
    val recordInteractionSource = remember { MutableInteractionSource() }
    val isPressed by recordInteractionSource.collectIsPressedAsState()
    val buttonScale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = tween(120),
        label = "recordButtonScale"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Secondary control: Pause / Resume button (only visible when recording)
            AnimatedVisibility(
                visible = isRecording,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Box(
                    modifier = Modifier
                        .padding(end = 24.dp)
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(DarkSurfaceVariant),
                    contentAlignment = Alignment.Center
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
                            contentDescription = if (isPaused) "Resume" else "Pause",
                            tint = if (isPaused) StatusAmber else TextPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Main Record / Stop Circular Button
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .scale(buttonScale)
                    .clip(CircleShape)
                    .background(if (isRecording) RecordingRed else TealAccent)
                    .clickable(
                        role = Role.Button,
                        interactionSource = recordInteractionSource,
                        indication = ripple(bounded = false, radius = 40.dp)
                    ) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (isRecording) onStopRecording() else onStartRecording()
                    }
                    .testTag(if (isRecording) "stop_recording_button" else "start_recording_button"),
                contentAlignment = Alignment.Center
            ) {
                if (!isRecording) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Start Recording",
                        tint = Color(0xFF101114),
                        modifier = Modifier.size(32.dp)
                    )
                } else {
                    // Restrained rounded square stop icon
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White)
                    )
                }
            }

            // Balancing placeholder when pause button is visible
            if (isRecording) {
                Box(
                    modifier = Modifier
                        .padding(start = 24.dp)
                        .size(46.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Clear recording state label below the button
        Text(
            text = when {
                isRecording && isPaused -> "Recording paused"
                isRecording -> stringResource(R.string.stop_recording)
                else -> stringResource(R.string.start_recording)
            },
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp
            ),
            color = if (isRecording) RecordingRed else TextSecondary,
            modifier = Modifier.padding(bottom = 4.dp)
        )
    }
}
