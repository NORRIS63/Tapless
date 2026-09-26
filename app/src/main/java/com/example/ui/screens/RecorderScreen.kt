package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.components.AudioVisualizer
import com.example.ui.components.DeleteConfirmDialog
import com.example.ui.components.PermissionRationaleDialog
import com.example.ui.components.RecordingControls
import com.example.ui.components.RecordingItemCard
import com.example.ui.components.RenameDialog
import com.example.ui.components.TimerDisplay
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.PlaybackCyan
import com.example.ui.theme.RecordingRed
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.RecorderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecorderScreen(
    viewModel: RecorderViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val recordingState by viewModel.recordingState.collectAsStateWithLifecycle()
    val playbackState by viewModel.playbackState.collectAsStateWithLifecycle()
    val recordings by viewModel.filteredRecordings.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val recordingToDelete by viewModel.recordingToDelete.collectAsStateWithLifecycle()
    val recordingToRename by viewModel.recordingToRename.collectAsStateWithLifecycle()
    val renameText by viewModel.renameInputText.collectAsStateWithLifecycle()
    val toastMsg by viewModel.toastMessage.collectAsStateWithLifecycle()
    val isShortcutEnabled by viewModel.isShortcutEnabled.collectAsStateWithLifecycle()
    val isServiceActive by viewModel.isAccessibilityServiceActive.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var showPermissionRationale by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.refreshAccessibilityServiceStatus(context)
    }

    LaunchedEffect(toastMsg) {
        toastMsg?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissToastMessage()
        }
    }

    // Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val recordAudioGranted = permissions[Manifest.permission.RECORD_AUDIO] == true
        if (recordAudioGranted) {
            viewModel.startRecording(context)
        } else {
            showPermissionRationale = true
        }
    }

    fun checkAndStartRecording() {
        val permissionsToRequest = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (permissionsToRequest.isEmpty()) {
            viewModel.startRecording(context)
        } else {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("recorder_screen"),
        containerColor = DarkBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = TextPrimary
                        )
                        Text(
                            text = stringResource(R.string.app_tagline),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 0.3.sp
                            ),
                            color = PlaybackCyan
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = DarkBackground
                ),
                actions = {
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.SHORTCUT_SETUP) },
                        modifier = Modifier.testTag("shortcut_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeDown,
                            contentDescription = "Volume Shortcut Settings",
                            tint = if (isShortcutEnabled && isServiceActive) PlaybackCyan else TextSecondary
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("recordings_lazy_column"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Shortcut status banner
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(12.dp))
                        .clickable { viewModel.navigateTo(AppScreen.SHORTCUT_SETUP) }
                        .testTag("shortcut_status_banner"),
                    color = DarkSurface,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeDown,
                                contentDescription = null,
                                tint = if (isShortcutEnabled && isServiceActive) PlaybackCyan else TextTertiary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isShortcutEnabled && isServiceActive) {
                                    "Volume Shortcut Active (2x Vol- to record)"
                                } else if (isShortcutEnabled) {
                                    "Volume Shortcut: Tap to finish setup"
                                } else {
                                    "Hardware Shortcut: Off"
                                },
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = if (isShortcutEnabled && isServiceActive) PlaybackCyan else TextSecondary
                            )
                        }
                        Text(
                            text = "Settings ›",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = PlaybackCyan
                        )
                    }
                }
            }

            // Error banner if any
            if (recordingState.error != null) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("error_banner"),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF3B1214)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = RecordingRed
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = recordingState.error ?: "",
                                color = Color(0xFFFFDAD6),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { viewModel.clearRecordingError() }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss error",
                                    tint = Color(0xFFFFDAD6)
                                )
                            }
                        }
                    }
                }
            }

            // STUDIO RECORDING CONSOLE (Deck)
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(24.dp))
                        .testTag("recording_studio_deck"),
                    color = DarkSurface,
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp, bottom = 12.dp, start = 16.dp, end = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Status & Timer
                        TimerDisplay(
                            isRecording = recordingState.isRecording,
                            isPaused = recordingState.isPaused,
                            formattedTime = recordingState.formattedElapsedTime
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Audio Waveform Visualizer
                        AudioVisualizer(
                            isRecording = recordingState.isRecording,
                            isPaused = recordingState.isPaused,
                            amplitudes = recordingState.amplitudes,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Big tactile Record / Stop controls
                        RecordingControls(
                            isRecording = recordingState.isRecording,
                            isPaused = recordingState.isPaused,
                            onStartRecording = { checkAndStartRecording() },
                            onStopRecording = { viewModel.stopRecording(context) },
                            onPauseRecording = { viewModel.pauseRecording(context) },
                            onResumeRecording = { viewModel.resumeRecording(context) }
                        )

                        Text(
                            text = if (recordingState.isRecording) {
                                stringResource(R.string.stop_recording)
                            } else {
                                stringResource(R.string.start_recording)
                            },
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = if (recordingState.isRecording) RecordingRed else TextSecondary,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                }
            }

            // SECTION HEADER: SAVED RECORDINGS & SEARCH
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.recordings_title),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(DarkSurfaceVariant)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${recordings.size}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = PlaybackCyan
                            )
                        }
                    }

                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = PlaybackCyan,
                            strokeWidth = 2.dp
                        )
                    }
                }
            }

            // Search filter field
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Search by filename…", color = TextTertiary, fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear search", tint = TextSecondary)
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkSurface,
                        unfocusedContainerColor = DarkSurface,
                        focusedBorderColor = PlaybackCyan.copy(alpha = 0.7f),
                        unfocusedBorderColor = DarkSurfaceBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_recordings_input")
                )
            }

            // Empty State
            if (recordings.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp)
                            .testTag("empty_state_container"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(DarkSurfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = null,
                                    tint = TextTertiary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = if (searchQuery.isNotEmpty()) "No matching recordings found" else stringResource(R.string.no_recordings),
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                        }
                    }
                }
            } else {
                // List of recordings
                items(
                    items = recordings,
                    key = { it.id }
                ) { item ->
                    RecordingItemCard(
                        recording = item,
                        playbackState = playbackState,
                        onTogglePlayPause = { viewModel.togglePlayPause(item) },
                        onSeek = { viewModel.seekPlayback(it) },
                        onRewind = { viewModel.rewindPlayback() },
                        onForward = { viewModel.forwardPlayback() },
                        onRename = { viewModel.promptRename(item) },
                        onDelete = { viewModel.promptDelete(item) },
                        onShare = {
                            val intent = viewModel.getShareIntent(item.file)
                            context.startActivity(android.content.Intent.createChooser(intent, "Share Recording"))
                        }
                    )
                }
            }
        }
    }

    // Delete confirmation dialog
    recordingToDelete?.let { recording ->
        DeleteConfirmDialog(
            recording = recording,
            onConfirm = { viewModel.executeDelete() },
            onDismiss = { viewModel.dismissDeleteDialog() }
        )
    }

    // Rename dialog
    recordingToRename?.let {
        RenameDialog(
            currentName = renameText,
            onNameChange = { viewModel.updateRenameInput(it) },
            onConfirm = { viewModel.executeRename() },
            onDismiss = { viewModel.dismissRenameDialog() }
        )
    }

    // Permission rationale dialog
    if (showPermissionRationale) {
        PermissionRationaleDialog(
            onGrantClick = {
                showPermissionRationale = false
                val permissions = mutableListOf(Manifest.permission.RECORD_AUDIO)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissions.add(Manifest.permission.POST_NOTIFICATIONS)
                }
                permissionLauncher.launch(permissions.toTypedArray())
            },
            onDismiss = { showPermissionRationale = false }
        )
    }
}
