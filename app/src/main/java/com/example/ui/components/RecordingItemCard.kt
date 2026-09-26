package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.example.ui.theme.DarkDivider
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.RecordingRed
import com.example.ui.theme.TealAccent
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
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(
                width = 1.dp,
                color = if (isCurrentTrack) TealAccent.copy(alpha = 0.4f) else DarkSurfaceBorder,
                shape = RoundedCornerShape(10.dp)
            )
            .testTag("recording_item_${recording.id}"),
        color = if (isCurrentTrack) DarkSurfaceVariant else DarkSurface,
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Compact Play / Pause Circular Button
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (isPlayingThis) TealAccent else DarkSurfaceVariant)
                        .clickable { onTogglePlayPause() },
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = onTogglePlayPause,
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("play_pause_button_${recording.id}")
                    ) {
                        Icon(
                            imageVector = if (isPlayingThis) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlayingThis) "Pause" else "Play",
                            tint = if (isPlayingThis) Color(0xFF101114) else TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Filename and Metadata
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onTogglePlayPause() }
                ) {
                    Text(
                        text = recording.title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        ),
                        color = if (isCurrentTrack) TealAccent else TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${recording.formattedDuration}  •  ${recording.formattedSize}  •  ${recording.formattedDate}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp
                        ),
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Contextual Overflow Menu
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("more_options_button_${recording.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(DarkSurfaceVariant)
                    ) {
                        DropdownMenuItem(
                            text = { Text(if (isPlayingThis) "Pause" else "Play", color = TextPrimary, fontSize = 14.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (isPlayingThis) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            onClick = {
                                showMenu = false
                                onTogglePlayPause()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Rename", color = TextPrimary, fontSize = 14.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                            },
                            onClick = {
                                showMenu = false
                                onRename()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Share", color = TextPrimary, fontSize = 14.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.Share, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                            },
                            onClick = {
                                showMenu = false
                                onShare()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = RecordingRed, fontSize = 14.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = RecordingRed, modifier = Modifier.size(18.dp))
                            },
                            onClick = {
                                showMenu = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            // Inline Playback Progress when this recording is loaded
            AnimatedVisibility(
                visible = isCurrentTrack,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Slider(
                        value = playbackState.currentPositionMs.toFloat(),
                        onValueChange = { onSeek(it.toInt()) },
                        valueRange = 0f..playbackState.totalDurationMs.toFloat().coerceAtLeast(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = TealAccent,
                            activeTrackColor = TealAccent,
                            inactiveTrackColor = DarkDivider
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(18.dp)
                            .testTag("playback_slider_${recording.id}")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = playbackState.formattedCurrentTime,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = TealAccent
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onRewind,
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("rewind_5s_${recording.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Replay5,
                                    contentDescription = "Rewind 5s",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = onForward,
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("forward_5s_${recording.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Forward5,
                                    contentDescription = "Forward 5s",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Text(
                            text = playbackState.formattedTotalTime,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}
