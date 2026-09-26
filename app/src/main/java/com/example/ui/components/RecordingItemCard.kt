package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Forward5
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay5
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AudioRecording
import com.example.player.PlaybackState
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.PlaybackCyan
import com.example.ui.theme.PlaybackCyanGlow
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordingItemCard(
    recording: AudioRecording,
    playbackState: PlaybackState,
    onTogglePlayPause: () -> Unit,
    onSeek: (Int) -> Unit,
    onRewind: () -> Unit,
    onForward: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCurrentTrack = playbackState.currentFile?.absolutePath == recording.file.absolutePath
    val isPlayingThis = isCurrentTrack && playbackState.isPlaying
    val isPausedThis = isCurrentTrack && playbackState.isPaused
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 1.dp,
                color = if (isCurrentTrack) PlaybackCyan.copy(alpha = 0.5f) else DarkSurfaceBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .testTag("recording_item_${recording.id}"),
        color = if (isCurrentTrack) DarkSurfaceVariant else DarkSurface,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Play / Pause Circle Button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (isCurrentTrack) PlaybackCyan else DarkSurfaceVariant)
                        .border(1.dp, if (isCurrentTrack) PlaybackCyan else Color(0xFF374151), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = onTogglePlayPause,
                        modifier = Modifier.testTag("play_pause_button_${recording.id}")
                    ) {
                        Icon(
                            imageVector = if (isPlayingThis) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlayingThis) "Pause" else "Play",
                            tint = if (isCurrentTrack) Color(0xFF0C1920) else Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Title and Metadata
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = recording.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        ),
                        color = if (isCurrentTrack) PlaybackCyan else TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = recording.formattedDuration,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = TextSecondary
                        )
                        Text(
                            text = " • ",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextTertiary
                        )
                        Text(
                            text = recording.formattedSize,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextTertiary
                        )
                    }
                    Text(
                        text = recording.formattedDate,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = TextTertiary
                    )
                }

                // Options Menu Button
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.testTag("more_options_button_${recording.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More Options",
                            tint = TextSecondary
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(DarkSurfaceVariant)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Rename", color = TextPrimary) },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = TextSecondary)
                            },
                            onClick = {
                                showMenu = false
                                onRename()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Share", color = TextPrimary) },
                            leadingIcon = {
                                Icon(Icons.Default.Share, contentDescription = null, tint = TextSecondary)
                            },
                            onClick = {
                                showMenu = false
                                onShare()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = Color(0xFFFF5252)) },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFFF5252))
                            },
                            onClick = {
                                showMenu = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            // Expanded Playback Controls when active
            AnimatedVisibility(
                visible = isCurrentTrack,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    // Scrubbing Slider
                    Slider(
                        value = playbackState.currentPositionMs.toFloat(),
                        onValueChange = { onSeek(it.toInt()) },
                        valueRange = 0f..playbackState.totalDurationMs.toFloat().coerceAtLeast(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = PlaybackCyan,
                            activeTrackColor = PlaybackCyan,
                            inactiveTrackColor = Color(0xFF2C3240)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(20.dp)
                            .testTag("playback_slider_${recording.id}")
                    )

                    // Current position and total duration
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = playbackState.formattedCurrentTime,
                            style = MaterialTheme.typography.labelSmall,
                            color = PlaybackCyan
                        )
                        Text(
                            text = playbackState.formattedTotalTime,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }

                    // Replay / Forward Skip Buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onRewind,
                            modifier = Modifier.testTag("rewind_5s_${recording.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Replay5,
                                contentDescription = "Rewind 5 seconds",
                                tint = TextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        IconButton(
                            onClick = onForward,
                            modifier = Modifier.testTag("forward_5s_${recording.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Forward5,
                                contentDescription = "Forward 5 seconds",
                                tint = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}
