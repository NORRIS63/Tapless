package com.example.player

import android.media.MediaPlayer
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

class AudioPlayer {

    private val playerScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var mediaPlayer: MediaPlayer? = null
    private var progressJob: Job? = null

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    fun play(file: File) {
        if (!file.exists() || file.length() == 0L) {
            Log.e(TAG, "File does not exist or is empty: ${file.absolutePath}")
            return
        }

        // If the same file is currently paused, simply resume
        val currentState = _playbackState.value
        if (currentState.currentFile?.absolutePath == file.absolutePath && currentState.isPaused) {
            resume()
            return
        }

        stop()

        try {
            val player = MediaPlayer()
            player.setDataSource(file.absolutePath)
            player.prepare()
            val duration = player.duration

            player.setOnCompletionListener {
                stopProgressUpdates()
                _playbackState.value = _playbackState.value.copy(
                    isPlaying = false,
                    isPaused = false,
                    currentPositionMs = 0
                )
            }

            player.setOnErrorListener { _, what, extra ->
                Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                stop()
                true
            }

            player.start()
            mediaPlayer = player

            _playbackState.value = PlaybackState(
                currentFile = file,
                isPlaying = true,
                isPaused = false,
                currentPositionMs = 0,
                totalDurationMs = duration
            )

            startProgressUpdates()
        } catch (e: Exception) {
            Log.e(TAG, "Error playing audio file", e)
            stop()
        }
    }

    fun pause() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
                stopProgressUpdates()
                _playbackState.value = _playbackState.value.copy(
                    isPlaying = false,
                    isPaused = true,
                    currentPositionMs = mediaPlayer?.currentPosition ?: _playbackState.value.currentPositionMs
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error pausing audio player", e)
        }
    }

    fun resume() {
        try {
            mediaPlayer?.let { player ->
                player.start()
                _playbackState.value = _playbackState.value.copy(
                    isPlaying = true,
                    isPaused = false
                )
                startProgressUpdates()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error resuming audio player", e)
        }
    }

    fun stop() {
        stopProgressUpdates()
        try {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.stop()
                }
                player.reset()
                player.release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping audio player", e)
        } finally {
            mediaPlayer = null
            _playbackState.value = PlaybackState()
        }
    }

    fun seekTo(positionMs: Int) {
        try {
            val target = positionMs.coerceIn(0, _playbackState.value.totalDurationMs)
            mediaPlayer?.seekTo(target)
            _playbackState.value = _playbackState.value.copy(currentPositionMs = target)
        } catch (e: Exception) {
            Log.e(TAG, "Error seeking", e)
        }
    }

    fun forward(ms: Int = 5000) {
        val current = mediaPlayer?.currentPosition ?: _playbackState.value.currentPositionMs
        val target = (current + ms).coerceAtMost(_playbackState.value.totalDurationMs)
        seekTo(target)
    }

    fun rewind(ms: Int = 5000) {
        val current = mediaPlayer?.currentPosition ?: _playbackState.value.currentPositionMs
        val target = (current - ms).coerceAtLeast(0)
        seekTo(target)
    }

    private fun startProgressUpdates() {
        stopProgressUpdates()
        progressJob = playerScope.launch {
            while (isActive) {
                mediaPlayer?.let { player ->
                    if (player.isPlaying) {
                        val pos = try { player.currentPosition } catch (e: Exception) { 0 }
                        _playbackState.value = _playbackState.value.copy(currentPositionMs = pos)
                    }
                }
                delay(100)
            }
        }
    }

    private fun stopProgressUpdates() {
        progressJob?.cancel()
        progressJob = null
    }

    fun release() {
        stop()
        playerScope.cancel()
    }

    companion object {
        private const val TAG = "AudioPlayer"
    }
}
