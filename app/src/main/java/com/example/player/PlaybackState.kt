package com.example.player

import java.io.File
import java.util.Locale

data class PlaybackState(
    val currentFile: File? = null,
    val isPlaying: Boolean = false,
    val isPaused: Boolean = false,
    val currentPositionMs: Int = 0,
    val totalDurationMs: Int = 0
) {
    val progress: Float
        get() = if (totalDurationMs > 0) {
            (currentPositionMs.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }

    val formattedCurrentTime: String
        get() = formatMs(currentPositionMs)

    val formattedTotalTime: String
        get() = formatMs(totalDurationMs)

    private fun formatMs(ms: Int): String {
        val totalSecs = (ms / 1000).coerceAtLeast(0)
        val mins = totalSecs / 60
        val secs = totalSecs % 60
        return String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
    }
}
