package com.example.recorder

data class RecordingState(
    val isRecording: Boolean = false,
    val isPaused: Boolean = false,
    val elapsedSeconds: Long = 0L,
    val currentFilePath: String? = null,
    val maxAmplitude: Int = 0,
    val amplitudes: List<Float> = emptyList(),
    val error: String? = null
) {
    val formattedElapsedTime: String
        get() {
            val hours = elapsedSeconds / 3600
            val minutes = (elapsedSeconds % 3600) / 60
            val seconds = elapsedSeconds % 60
            return if (hours > 0) {
                String.format(java.util.Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format(java.util.Locale.getDefault(), "%02d:%02d", minutes, seconds)
            }
        }
}
