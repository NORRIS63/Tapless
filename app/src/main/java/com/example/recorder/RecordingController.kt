package com.example.recorder

import android.content.Context
import android.content.Intent
import android.os.Build
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

object RecordingController {
    private val _recordingState = MutableStateFlow(RecordingState())
    val recordingState: StateFlow<RecordingState> = _recordingState.asStateFlow()

    private val _recordingFinishedEvent = MutableSharedFlow<File>(extraBufferCapacity = 1)
    val recordingFinishedEvent: SharedFlow<File> = _recordingFinishedEvent.asSharedFlow()

    fun updateState(transform: (RecordingState) -> RecordingState) {
        _recordingState.value = transform(_recordingState.value)
    }

    fun notifyRecordingFinished(file: File) {
        _recordingFinishedEvent.tryEmit(file)
    }

    fun startRecording(context: Context): Boolean {
        return try {
            val intent = Intent(context, RecordingService::class.java).apply {
                action = RecordingService.ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            true
        } catch (e: Throwable) {
            android.util.Log.e("RecordingController", "Failed to start recording service", e)
            _recordingState.value = _recordingState.value.copy(
                isRecording = false,
                error = "Android blocked background start: ${e.localizedMessage ?: "Restricted by system"}"
            )
            false
        }
    }

    fun stopRecording(context: Context) {
        val intent = Intent(context, RecordingService::class.java).apply {
            action = RecordingService.ACTION_STOP
        }
        context.startService(intent)
    }

    fun pauseRecording(context: Context) {
        val intent = Intent(context, RecordingService::class.java).apply {
            action = RecordingService.ACTION_PAUSE
        }
        context.startService(intent)
    }

    fun resumeRecording(context: Context) {
        val intent = Intent(context, RecordingService::class.java).apply {
            action = RecordingService.ACTION_RESUME
        }
        context.startService(intent)
    }

    fun clearError() {
        _recordingState.value = _recordingState.value.copy(error = null)
    }
}
