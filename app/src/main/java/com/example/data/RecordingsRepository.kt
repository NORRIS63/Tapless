package com.example.data

import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import com.example.data.model.AudioRecording
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class RecordingsRepository(private val context: Context) {

    private fun getRecordingsDirectory(): File {
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_RECORDINGS)
            ?: File(context.filesDir, "recordings")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    suspend fun getRecordings(): List<AudioRecording> = withContext(Dispatchers.IO) {
        val dir = getRecordingsDirectory()
        val files = dir.listFiles { file ->
            file.isFile && (file.extension.equals("m4a", ignoreCase = true) ||
                    file.extension.equals("mp4", ignoreCase = true) ||
                    file.extension.equals("aac", ignoreCase = true) ||
                    file.extension.equals("3gp", ignoreCase = true))
        } ?: emptyArray()

        files.map { file ->
            val duration = getAudioDurationMs(file)
            AudioRecording(
                id = file.name,
                file = file,
                title = file.nameWithoutExtension,
                durationMs = duration,
                sizeBytes = file.length(),
                createdAt = file.lastModified()
            )
        }.sortedByDescending { it.createdAt }
    }

    private fun getAudioDurationMs(file: File): Long {
        var retriever: MediaMetadataRetriever? = null
        return try {
            retriever = MediaMetadataRetriever()
            retriever.setDataSource(file.absolutePath)
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            durationStr?.toLongOrNull() ?: 0L
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting duration for ${file.name}", e)
            0L
        } finally {
            try {
                retriever?.release()
            } catch (ignored: Exception) {}
        }
    }

    suspend fun deleteRecording(file: File): Boolean = withContext(Dispatchers.IO) {
        try {
            if (file.exists()) {
                file.delete()
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete file ${file.name}", e)
            false
        }
    }

    suspend fun renameRecording(file: File, newName: String): File? = withContext(Dispatchers.IO) {
        val sanitized = newName.trim().replace(Regex("[^a-zA-Z0-9._ -]"), "_")
        if (sanitized.isBlank()) return@withContext null

        val extension = file.extension
        val newFile = File(file.parentFile, "$sanitized.$extension")

        if (newFile.exists()) {
            Log.w(TAG, "Target file already exists: ${newFile.name}")
            return@withContext null
        }

        return@withContext try {
            if (file.renameTo(newFile)) {
                newFile
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to rename file", e)
            null
        }
    }

    fun getShareIntent(file: File): Intent {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        return Intent(Intent.ACTION_SEND).apply {
            type = "audio/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    companion object {
        private const val TAG = "RecordingsRepository"
    }
}
