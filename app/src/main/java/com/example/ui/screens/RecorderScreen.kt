package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.material3.TopAppBar
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
import com.example.ui.theme.DarkDivider
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.RecordingRed
import com.example.ui.theme.TealAccent
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
    var isSearchExpanded by remember { mutableStateOf(false) }

    val hasMicPermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.RECORD_AUDIO
    ) == PackageManager.PERMISSION_GRANTED
    val isRecorderAvailable = hasMicPermission && recordingState.error == null

    LaunchedEffect(Unit) {
        viewModel.refreshAccessibilityServiceStatus(context)
    }

    LaunchedEffect(toastMsg) {
        toastMsg?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissToastMessage()
        }
    }

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
            // 1. Compact top app bar with Tapless name and simple settings icon
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp,
                            letterSpacing = 0.3.sp
                        ),
                        color = TextPrimary
                    )
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.SHORTCUT_SETUP) },
                        modifier = Modifier.testTag("shortcut_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = if (isShortcutEnabled && isServiceActive) TealAccent else TextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("recordings_lazy_column"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Error banner if any
            if (recordingState.error != null) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("error_banner"),
                        color = Color(0xFF261416),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RecordingRed.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = RecordingRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = recordingState.error ?: "",
                                color = TextPrimary,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { viewModel.clearRecordingError() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss error",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // MAIN RECORDER CONSOLE
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(16.dp))
                        .testTag("recording_studio_deck"),
                    color = DarkSurface,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 2 & 3. Small status indicator and simple recording timer
                        TimerDisplay(
                            isRecording = recordingState.isRecording,
                            isPaused = recordingState.isPaused,
                            formattedTime = recordingState.formattedElapsedTime,
                            isAvailable = isRecorderAvailable
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // 3. Clean waveform visualization
                        AudioVisualizer(
                            isRecording = recordingState.isRecording,
                            isPaused = recordingState.isPaused,
                            amplitudes = recordingState.amplitudes,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // 4 & 5. One prominent circular record button and state label
                        RecordingControls(
                            isRecording = recordingState.isRecording,
                            isPaused = recordingState.isPaused,
                            onStartRecording = { checkAndStartRecording() },
                            onStopRecording = { viewModel.stopRecording(context) },
                            onPauseRecording = { viewModel.pauseRecording(context) },
                            onResumeRecording = { viewModel.resumeRecording(context) }
                        )
                    }
                }
            }

            // 6. COMPACT RECORDINGS SECTION HEADER (Recordings count, search icon)
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.recordings_title),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            ),
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkSurfaceVariant)
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${recordings.size}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.sp
                                ),
                                color = TealAccent
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .size(16.dp),
                                color = TealAccent,
                                strokeWidth = 2.dp
                            )
                        }

                        IconButton(
                            onClick = {
                                isSearchExpanded = !isSearchExpanded
                                if (!isSearchExpanded) viewModel.setSearchQuery("")
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (isSearchExpanded) Icons.Default.Close else Icons.Default.Search,
                                contentDescription = if (isSearchExpanded) "Close Search" else "Search",
                                tint = if (isSearchExpanded || searchQuery.isNotEmpty()) TealAccent else TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Clean expandable Search Input
            if (isSearchExpanded || searchQuery.isNotEmpty()) {
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text("Filter recordings…", color = TextTertiary, fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextSecondary, modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkSurface,
                            unfocusedContainerColor = DarkSurface,
                            focusedBorderColor = TealAccent,
                            unfocusedBorderColor = DarkDivider,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_recordings_input")
                    )
                }
            }

            // 8. MINIMAL EMPTY STATE
            if (recordings.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp)
                            .testTag("empty_state_container"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                tint = TextTertiary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = if (searchQuery.isNotEmpty()) "No matching recordings" else "No recordings yet",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp
                                ),
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (searchQuery.isNotEmpty()) "Try a different search term" else "Tap the record button to begin.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = TextTertiary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                // 7. CLEAN LIST OF SAVED RECORDINGS
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

    // Dialogs
    recordingToDelete?.let { recording ->
        DeleteConfirmDialog(
            recording = recording,
            onConfirm = { viewModel.executeDelete() },
            onDismiss = { viewModel.dismissDeleteDialog() }
        )
    }

    recordingToRename?.let {
        RenameDialog(
            currentName = renameText,
            onNameChange = { viewModel.updateRenameInput(it) },
            onConfirm = { viewModel.executeRename() },
            onDismiss = { viewModel.dismissRenameDialog() }
        )
    }

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
