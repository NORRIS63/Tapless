package com.example.shortcut

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ShortcutPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("quick_voice_recorder_prefs", Context.MODE_PRIVATE)

    private val _isShortcutEnabled = MutableStateFlow(
        prefs.getBoolean(KEY_SHORTCUT_ENABLED, true)
    )
    val isShortcutEnabled: StateFlow<Boolean> = _isShortcutEnabled.asStateFlow()

    fun setShortcutEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SHORTCUT_ENABLED, enabled).apply()
        _isShortcutEnabled.value = enabled
    }

    companion object {
        private const val KEY_SHORTCUT_ENABLED = "key_volume_shortcut_enabled"

        @Volatile
        private var INSTANCE: ShortcutPreferences? = null

        fun getInstance(context: Context): ShortcutPreferences {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ShortcutPreferences(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
