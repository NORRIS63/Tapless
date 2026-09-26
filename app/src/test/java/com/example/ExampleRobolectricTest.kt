package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.AudioRecording
import com.example.recorder.RecordingState
import com.example.shortcut.ShortcutPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read app name string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        val tagline = context.getString(R.string.app_tagline)
        assertEquals("Tapless", appName)
        assertEquals("One press. Every word.", tagline)
    }

    @Test
    fun `shortcut strings are accessible from resources`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val shortcutTitle = context.getString(R.string.shortcut_setup_title)
        val accessibilityName = context.getString(R.string.accessibility_service_name)
        assertTrue(shortcutTitle.isNotBlank())
        assertTrue(accessibilityName.isNotBlank())
    }

    @Test
    fun `shortcut preferences store and update state correctly`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = ShortcutPreferences.getInstance(context)

        prefs.setShortcutEnabled(false)
        assertFalse(prefs.isShortcutEnabled.value)

        prefs.setShortcutEnabled(true)
        assertTrue(prefs.isShortcutEnabled.value)
    }

    @Test
    fun `audio recording format helpers work correctly`() {
        // Test duration formatting
        assertEquals("00:00", AudioRecording.formatDuration(0))
        assertEquals("00:45", AudioRecording.formatDuration(45000))
        assertEquals("02:15", AudioRecording.formatDuration(135000))
        assertEquals("01:00:00", AudioRecording.formatDuration(3600000))

        // Test file size formatting
        assertEquals("0 B", AudioRecording.formatFileSize(0))
        assertEquals("500 B", AudioRecording.formatFileSize(500))
        assertEquals("500 KB", AudioRecording.formatFileSize(500 * 1024))
        assertEquals("2.5 MB", AudioRecording.formatFileSize((2.5 * 1024 * 1024).toLong()))
    }

    @Test
    fun `recording state formatted elapsed time works correctly`() {
        val stateZero = RecordingState(elapsedSeconds = 0)
        assertEquals("00:00", stateZero.formattedElapsedTime)

        val stateMinute = RecordingState(elapsedSeconds = 65)
        assertEquals("01:05", stateMinute.formattedElapsedTime)

        val stateHour = RecordingState(elapsedSeconds = 3661)
        assertEquals("01:01:01", stateHour.formattedElapsedTime)
    }
}
