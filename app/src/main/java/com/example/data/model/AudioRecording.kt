package com.example.data.model

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AudioRecording(
    val id: String,
    val file: File,
    val title: String,
    val durationMs: Long,
    val sizeBytes: Long,
    val createdAt: Long
) {
    val formattedDuration: String
        get() = formatDuration(durationMs)

    val formattedSize: String
        get() = formatFileSize(sizeBytes)

    val formattedDate: String
        get() {
            val sdf = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
            return sdf.format(Date(createdAt))
        }

    companion object {
        fun formatDuration(durationMs: Long): String {
            val totalSeconds = (durationMs / 1000).coerceAtLeast(0)
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60
            return if (hours > 0) {
                String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
            }
        }

        fun formatFileSize(bytes: Long): String {
            if (bytes <= 0) return "0 B"
            val kb = bytes / 1024.0
            val mb = kb / 1024.0
            return when {
                mb >= 1.0 -> String.format(Locale.getDefault(), "%.1f MB", mb)
                kb >= 1.0 -> String.format(Locale.getDefault(), "%.0f KB", kb)
                else -> "$bytes B"
            }
        }
    }
}
