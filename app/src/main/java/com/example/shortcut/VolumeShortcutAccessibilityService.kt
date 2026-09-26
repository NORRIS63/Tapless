package com.example.shortcut

import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.pm.PackageManager
import android.os.SystemClock
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import androidx.core.content.ContextCompat
import com.example.R
import com.example.recorder.RecordingController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class VolumeShortcutAccessibilityService : AccessibilityService() {

    private var lastVolumeDownUptimeMs: Long = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        val info = serviceInfo ?: AccessibilityServiceInfo()
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        info.flags = info.flags or AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS
        info.notificationTimeout = 100
        serviceInfo = info

        _isServiceActive.value = true
        Log.d(TAG, "VolumeShortcutAccessibilityService connected and filtering key events")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Not used for key filtering
    }

    override fun onInterrupt() {
        Log.w(TAG, "VolumeShortcutAccessibilityService interrupted")
    }

    override fun onKeyEvent(event: KeyEvent?): Boolean {
        if (event == null) return false

        // Only listen for Volume Down button presses
        if (event.keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            // Only process initial press down, ignoring long-press key repeats
            if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
                val isEnabled = ShortcutPreferences.getInstance(this).isShortcutEnabled.value
                if (isEnabled) {
                    val now = SystemClock.uptimeMillis()
                    val diff = now - lastVolumeDownUptimeMs

                    // Deliberate trigger: two presses within 1.5 seconds (1500 ms)
                    if (diff in 50..1500) {
                        Log.d(TAG, "Deliberate Volume-Down double press detected (delta=${diff}ms)")
                        // Reset to avoid repeated/runaway triggers
                        lastVolumeDownUptimeMs = 0L
                        handleShortcutTrigger()
                    } else {
                        lastVolumeDownUptimeMs = now
                    }
                }
            }
        }

        // Return false to avoid blocking system volume adjustment unnecessarily
        return false
    }

    private fun handleShortcutTrigger() {
        val currentState = RecordingController.recordingState.value

        if (currentState.isRecording) {
            // STOP RECORDING ACTION
            Log.d(TAG, "Shortcut: Stopping active recording")
            RecordingController.stopRecording(this)
            ShortcutUtils.vibrateFeedback(this, longArrayOf(0, 100))
            ShortcutUtils.emitTriggerEvent("Stopped recording via Volume shortcut")
        } else {
            // START RECORDING ACTION
            Log.d(TAG, "Shortcut: Starting recording attempt")

            // 1. Verify Microphone permission
            val hasMicPermission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasMicPermission) {
                Log.w(TAG, "Microphone permission missing when volume shortcut triggered")
                ShortcutUtils.vibrateFeedback(this, longArrayOf(0, 80, 80, 80, 80, 80))
                ShortcutUtils.showBlockedNotification(
                    this,
                    getString(R.string.notification_blocked_title),
                    getString(R.string.notification_permission_needed_desc)
                )
                ShortcutUtils.emitTriggerEvent("Microphone permission required")
                return
            }

            // 2. Attempt starting the microphone foreground service
            val success = RecordingController.startRecording(this)
            if (success) {
                Log.d(TAG, "Recording service started successfully via shortcut")
                // Distinct double-haptic pulse confirmation
                ShortcutUtils.vibrateFeedback(this, longArrayOf(0, 80, 100, 80))
                ShortcutUtils.emitTriggerEvent("Started recording via Volume shortcut")
            } else {
                Log.e(TAG, "Microphone service start was blocked by Android system")
                // Error triple-haptic pulse
                ShortcutUtils.vibrateFeedback(this, longArrayOf(0, 80, 80, 80, 80, 80))
                ShortcutUtils.showBlockedNotification(
                    this,
                    getString(R.string.notification_blocked_title),
                    getString(R.string.notification_blocked_desc)
                )
                ShortcutUtils.emitTriggerEvent("Background start blocked by Android")
            }
        }
    }

    override fun onDestroy() {
        _isServiceActive.value = false
        super.onDestroy()
        Log.d(TAG, "VolumeShortcutAccessibilityService destroyed")
    }

    companion object {
        private const val TAG = "VolumeShortcutService"

        private val _isServiceActive = MutableStateFlow(false)
        val isServiceActive: StateFlow<Boolean> = _isServiceActive.asStateFlow()
    }
}
