package com.example.recorder

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecordingService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var mediaRecorder: MediaRecorder? = null
    private var currentOutputFile: File? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var timerJob: Job? = null

    private var isRecording = false
    private var isPaused = false
    private var elapsedSeconds = 0L
    private val amplitudeHistory = ArrayDeque<Float>()
    private val maxHistorySize = 40

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startRecording()
            ACTION_STOP -> stopRecording()
            ACTION_PAUSE -> pauseRecording()
            ACTION_RESUME -> resumeRecording()
        }
        return START_NOT_STICKY
    }

    private fun startRecording() {
        if (isRecording) return

        // 1. Immediately start foreground to satisfy Android requirements
        val initNotification = buildNotification("Initializing recorder…")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                initNotification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            )
        } else {
            startForeground(NOTIFICATION_ID, initNotification)
        }

        try {
            // 2. Acquire WakeLock
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "QuickVoiceRecorder::RecordingWakeLock"
            ).apply {
                setReferenceCounted(false)
                acquire(10 * 60 * 60 * 1000L) // 10 hour safeguard max
            }

            // 3. Prepare output file
            val dir = getExternalFilesDir(Environment.DIRECTORY_RECORDINGS) ?: File(filesDir, "recordings")
            if (!dir.exists()) {
                dir.mkdirs()
            }
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val file = File(dir, "REC_$timeStamp.m4a")
            currentOutputFile = file

            // 4. Initialize MediaRecorder
            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(this)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            mediaRecorder = recorder
            isRecording = true
            isPaused = false
            elapsedSeconds = 0L
            amplitudeHistory.clear()

            // 5. Update Notification to active recording state
            updateNotification("Recording… 00:00")

            // 6. Update State
            RecordingController.updateState {
                it.copy(
                    isRecording = true,
                    isPaused = false,
                    elapsedSeconds = 0L,
                    currentFilePath = file.absolutePath,
                    maxAmplitude = 0,
                    amplitudes = emptyList(),
                    error = null
                )
            }

            // 7. Launch Timer & Amplitude sampler
            startTimerAndSampler()

        } catch (e: Exception) {
            Log.e(TAG, "Error starting recording", e)
            cleanUpResources()
            val errorMsg = e.localizedMessage ?: "Unknown error"
            RecordingController.updateState {
                it.copy(
                    isRecording = false,
                    isPaused = false,
                    error = "Failed to start recording: $errorMsg"
                )
            }
            com.example.shortcut.ShortcutUtils.showBlockedNotification(
                this,
                getString(R.string.notification_blocked_title),
                "Microphone access failed: $errorMsg. Tap to open app and record."
            )
            stopForegroundCompat()
            stopSelf()
        }
    }

    private fun startTimerAndSampler() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            var tickCount = 0
            while (isActive && isRecording) {
                delay(100)
                if (!isPaused) {
                    tickCount++

                    // Sample amplitude
                    val amp = try {
                        mediaRecorder?.maxAmplitude ?: 0
                    } catch (e: Exception) {
                        0
                    }
                    val normalized = (amp / 32767f).coerceIn(0f, 1f)
                    if (amplitudeHistory.size >= maxHistorySize) {
                        amplitudeHistory.removeFirst()
                    }
                    amplitudeHistory.addLast(normalized)

                    // Every 1 second (10 ticks of 100ms)
                    if (tickCount % 10 == 0) {
                        elapsedSeconds++
                        val timeStr = formatTime(elapsedSeconds)
                        updateNotification("Recording… $timeStr")
                    }

                    RecordingController.updateState {
                        it.copy(
                            elapsedSeconds = elapsedSeconds,
                            maxAmplitude = amp,
                            amplitudes = amplitudeHistory.toList()
                        )
                    }
                }
            }
        }
    }

    private fun pauseRecording() {
        if (!isRecording || isPaused) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mediaRecorder?.pause()
                isPaused = true
                updateNotification("Paused (${formatTime(elapsedSeconds)})")
                RecordingController.updateState {
                    it.copy(isPaused = true)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error pausing recording", e)
        }
    }

    private fun resumeRecording() {
        if (!isRecording || !isPaused) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mediaRecorder?.resume()
                isPaused = false
                updateNotification("Recording… ${formatTime(elapsedSeconds)}")
                RecordingController.updateState {
                    it.copy(isPaused = false)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error resuming recording", e)
        }
    }

    private fun stopRecording() {
        if (!isRecording) return

        timerJob?.cancel()
        timerJob = null

        var recordedFile = currentOutputFile
        try {
            mediaRecorder?.stop()
        } catch (e: RuntimeException) {
            Log.e(TAG, "stop failed, recording may have been too short", e)
            // If recording was stopped too quickly, delete invalid/corrupt output
            if (recordedFile?.exists() == true && recordedFile.length() < 1000) {
                recordedFile.delete()
                recordedFile = null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during mediaRecorder stop", e)
        } finally {
            cleanUpRecorder()
        }

        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }
        wakeLock = null

        isRecording = false
        isPaused = false

        RecordingController.updateState {
            it.copy(
                isRecording = false,
                isPaused = false,
                elapsedSeconds = 0L,
                currentFilePath = null,
                maxAmplitude = 0,
                amplitudes = emptyList()
            )
        }

        if (recordedFile != null && recordedFile.exists() && recordedFile.length() > 0) {
            RecordingController.notifyRecordingFinished(recordedFile)
        }

        stopForegroundCompat()
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.cancel(NOTIFICATION_ID)
        stopSelf()
    }

    private fun cleanUpRecorder() {
        try {
            mediaRecorder?.reset()
            mediaRecorder?.release()
        } catch (ignored: Exception) {
        }
        mediaRecorder = null
    }

    private fun cleanUpResources() {
        cleanUpRecorder()
        timerJob?.cancel()
        timerJob = null
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }
        wakeLock = null
        isRecording = false
        isPaused = false
    }

    private fun stopForegroundCompat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
    }

    private fun buildNotification(contentText: String): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, RecordingService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_recording_title))
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_mic_notification)
            .setContentIntent(openAppPendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(
                android.R.drawable.ic_media_pause,
                getString(R.string.notification_stop_action),
                stopPendingIntent
            )

        // Add Pause / Resume action if available on Android N+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (isPaused) {
                val resumeIntent = Intent(this, RecordingService::class.java).apply {
                    action = ACTION_RESUME
                }
                val resumePendingIntent = PendingIntent.getService(
                    this,
                    2,
                    resumeIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                builder.addAction(
                    android.R.drawable.ic_media_play,
                    getString(R.string.resume_recording),
                    resumePendingIntent
                )
            } else {
                val pauseIntent = Intent(this, RecordingService::class.java).apply {
                    action = ACTION_PAUSE
                }
                val pausePendingIntent = PendingIntent.getService(
                    this,
                    3,
                    pauseIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                builder.addAction(
                    android.R.drawable.ic_media_pause,
                    getString(R.string.pause_recording),
                    pausePendingIntent
                )
            }
        }

        return builder.build()
    }

    private fun updateNotification(contentText: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, buildNotification(contentText))
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.notification_channel_name)
            val descriptionText = getString(R.string.notification_channel_desc)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setShowBadge(false)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun formatTime(seconds: Long): String {
        val hours = seconds / 3600
        val mins = (seconds % 3600) / 60
        val secs = seconds % 60
        return if (hours > 0) {
            String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, mins, secs)
        } else {
            String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
        }
    }

    override fun onDestroy() {
        cleanUpResources()
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        const val TAG = "RecordingService"
        const val CHANNEL_ID = "voice_recording_service_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START = "com.example.recorder.ACTION_START"
        const val ACTION_STOP = "com.example.recorder.ACTION_STOP"
        const val ACTION_PAUSE = "com.example.recorder.ACTION_PAUSE"
        const val ACTION_RESUME = "com.example.recorder.ACTION_RESUME"
    }
}
