package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.RecordingsRepository
import com.example.data.model.AudioRecording
import com.example.player.AudioPlayer
import com.example.player.PlaybackState
import com.example.recorder.RecordingController
import com.example.recorder.RecordingState
import com.example.shortcut.ShortcutPreferences
import com.example.shortcut.ShortcutUtils
import com.example.shortcut.VolumeShortcutAccessibilityService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

enum class AppScreen {
    RECORDER,
    SHORTCUT_SETUP
}

class RecorderViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = RecordingsRepository(application)
    val audioPlayer = AudioPlayer()

    val recordingState: StateFlow<RecordingState> = RecordingController.recordingState
    val playbackState: StateFlow<PlaybackState> = audioPlayer.playbackState

    private val shortcutPreferences = ShortcutPreferences.getInstance(application)
    val isShortcutEnabled: StateFlow<Boolean> = shortcutPreferences.isShortcutEnabled

    private val _currentScreen = MutableStateFlow(AppScreen.RECORDER)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _isAccessibilityServiceActive = MutableStateFlow(false)
    val isAccessibilityServiceActive: StateFlow<Boolean> = _isAccessibilityServiceActive.asStateFlow()

    private val _lastShortcutEvent = MutableStateFlow<String?>(null)
    val lastShortcutEvent: StateFlow<String?> = _lastShortcutEvent.asStateFlow()

    private val _rawRecordings = MutableStateFlow<List<AudioRecording>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _recordingToDelete = MutableStateFlow<AudioRecording?>(null)
    val recordingToDelete: StateFlow<AudioRecording?> = _recordingToDelete.asStateFlow()

    private val _recordingToRename = MutableStateFlow<AudioRecording?>(null)
    val recordingToRename: StateFlow<AudioRecording?> = _recordingToRename.asStateFlow()

    private val _renameInputText = MutableStateFlow("")
    val renameInputText: StateFlow<String> = _renameInputText.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    val filteredRecordings: StateFlow<List<AudioRecording>> = combine(
        _rawRecordings,
        _searchQuery
    ) { recordings, query ->
        if (query.isBlank()) {
            recordings
        } else {
            recordings.filter { it.title.contains(query, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadRecordings()
        observeRecordingFinished()
        observeShortcutEvents()
    }

    private fun observeShortcutEvents() {
        viewModelScope.launch {
            ShortcutUtils.shortcutTriggerEvents.collect { event ->
                _lastShortcutEvent.value = event
            }
        }
    }

    fun setShortcutEnabled(enabled: Boolean) {
        shortcutPreferences.setShortcutEnabled(enabled)
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun refreshAccessibilityServiceStatus(context: Context) {
        val enabled = ShortcutUtils.isAccessibilityServiceEnabled(
            context,
            VolumeShortcutAccessibilityService::class.java
        )
        _isAccessibilityServiceActive.value = enabled
    }

    private fun observeRecordingFinished() {
        viewModelScope.launch {
            RecordingController.recordingFinishedEvent.collect {
                loadRecordings()
                _toastMessage.value = "Recording saved: ${it.name}"
            }
        }
    }

    fun loadRecordings() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val list = repository.getRecordings()
                _rawRecordings.value = list
            } catch (e: Exception) {
                _toastMessage.value = "Failed to load recordings: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun startRecording(context: Context) {
        // If playing audio, stop before recording
        if (playbackState.value.isPlaying) {
            audioPlayer.stop()
        }
        RecordingController.startRecording(context)
    }

    fun stopRecording(context: Context) {
        RecordingController.stopRecording(context)
    }

    fun pauseRecording(context: Context) {
        RecordingController.pauseRecording(context)
    }

    fun resumeRecording(context: Context) {
        RecordingController.resumeRecording(context)
    }

    fun clearRecordingError() {
        RecordingController.clearError()
    }

    // Playback actions
    fun togglePlayPause(recording: AudioRecording) {
        val currentPlay = playbackState.value
        if (currentPlay.currentFile?.absolutePath == recording.file.absolutePath) {
            if (currentPlay.isPlaying) {
                audioPlayer.pause()
            } else {
                audioPlayer.resume()
            }
        } else {
            audioPlayer.play(recording.file)
        }
    }

    fun pausePlayback() {
        audioPlayer.pause()
    }

    fun resumePlayback() {
        audioPlayer.resume()
    }

    fun stopPlayback() {
        audioPlayer.stop()
    }

    fun seekPlayback(positionMs: Int) {
        audioPlayer.seekTo(positionMs)
    }

    fun rewindPlayback() {
        audioPlayer.rewind(5000)
    }

    fun forwardPlayback() {
        audioPlayer.forward(5000)
    }

    // Deletion
    fun promptDelete(recording: AudioRecording) {
        _recordingToDelete.value = recording
    }

    fun dismissDeleteDialog() {
        _recordingToDelete.value = null
    }

    fun executeDelete() {
        val item = _recordingToDelete.value ?: return
        viewModelScope.launch {
            if (playbackState.value.currentFile?.absolutePath == item.file.absolutePath) {
                audioPlayer.stop()
            }
            val success = repository.deleteRecording(item.file)
            if (success) {
                _toastMessage.value = "Recording deleted"
                loadRecordings()
            } else {
                _toastMessage.value = "Failed to delete recording"
            }
            _recordingToDelete.value = null
        }
    }

    // Renaming
    fun promptRename(recording: AudioRecording) {
        _recordingToRename.value = recording
        _renameInputText.value = recording.title
    }

    fun updateRenameInput(text: String) {
        _renameInputText.value = text
    }

    fun dismissRenameDialog() {
        _recordingToRename.value = null
        _renameInputText.value = ""
    }

    fun executeRename() {
        val item = _recordingToRename.value ?: return
        val newTitle = _renameInputText.value.trim()
        if (newTitle.isBlank() || newTitle == item.title) {
            dismissRenameDialog()
            return
        }

        viewModelScope.launch {
            if (playbackState.value.currentFile?.absolutePath == item.file.absolutePath) {
                audioPlayer.stop()
            }
            val newFile = repository.renameRecording(item.file, newTitle)
            if (newFile != null) {
                _toastMessage.value = "Recording renamed"
                loadRecordings()
            } else {
                _toastMessage.value = "Failed to rename: file already exists or invalid name"
            }
            dismissRenameDialog()
        }
    }

    fun getShareIntent(file: File): Intent {
        return repository.getShareIntent(file)
    }

    fun dismissToastMessage() {
        _toastMessage.value = null
    }

    override fun onCleared() {
        audioPlayer.release()
        super.onCleared()
    }
}
