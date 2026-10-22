package com.example.sequencebox.data

import android.content.Context
import android.content.SharedPreferences
import com.example.sequencebox.model.AppRoundingMode
import com.example.sequencebox.model.AppSettings
import com.example.sequencebox.model.SequenceType
import com.example.sequencebox.model.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(
    private val prefs: SharedPreferences
) {
    constructor(context: Context) : this(
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    )

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private fun loadSettings(): AppSettings {
        val themeStr = prefs.getString(KEY_THEME, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
        val themeMode = try {
            ThemeMode.valueOf(themeStr)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }

        val dynamicColor = prefs.getBoolean(KEY_DYNAMIC_COLOR, true)
        val haptics = prefs.getBoolean(KEY_HAPTICS, true)
        val animations = prefs.getBoolean(KEY_ANIMATIONS, true)
        val confirmDelete = prefs.getBoolean(KEY_CONFIRM_DELETE, true)
        val decimalPlaces = prefs.getInt(KEY_DECIMAL_PLACES, 10)

        val roundingStr = prefs.getString(KEY_ROUNDING_MODE, AppRoundingMode.HALF_UP.name)
            ?: AppRoundingMode.HALF_UP.name
        val roundingMode = try {
            AppRoundingMode.valueOf(roundingStr)
        } catch (e: Exception) {
            AppRoundingMode.HALF_UP
        }

        return AppSettings(
            themeMode = themeMode,
            dynamicColor = dynamicColor,
            hapticsEnabled = haptics,
            animationsEnabled = animations,
            confirmDelete = confirmDelete,
            decimalPlaces = decimalPlaces,
            roundingMode = roundingMode
        )
    }

    fun updateTheme(themeMode: ThemeMode) {
        prefs.edit().putString(KEY_THEME, themeMode.name).apply()
        _settings.value = _settings.value.copy(themeMode = themeMode)
    }

    fun updateDynamicColor(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DYNAMIC_COLOR, enabled).apply()
        _settings.value = _settings.value.copy(dynamicColor = enabled)
    }

    fun updateHaptics(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HAPTICS, enabled).apply()
        _settings.value = _settings.value.copy(hapticsEnabled = enabled)
    }

    fun updateAnimations(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ANIMATIONS, enabled).apply()
        _settings.value = _settings.value.copy(animationsEnabled = enabled)
    }

    fun updateConfirmDelete(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_CONFIRM_DELETE, enabled).apply()
        _settings.value = _settings.value.copy(confirmDelete = enabled)
    }

    fun updateDecimalPlaces(places: Int) {
        prefs.edit().putInt(KEY_DECIMAL_PLACES, places).apply()
        _settings.value = _settings.value.copy(decimalPlaces = places)
    }

    fun updateRoundingMode(roundingMode: AppRoundingMode) {
        prefs.edit().putString(KEY_ROUNDING_MODE, roundingMode.name).apply()
        _settings.value = _settings.value.copy(roundingMode = roundingMode)
    }

    fun resetSettings() {
        val defaultSettings = AppSettings()
        prefs.edit()
            .putString(KEY_THEME, defaultSettings.themeMode.name)
            .putBoolean(KEY_DYNAMIC_COLOR, defaultSettings.dynamicColor)
            .putBoolean(KEY_HAPTICS, defaultSettings.hapticsEnabled)
            .putBoolean(KEY_ANIMATIONS, defaultSettings.animationsEnabled)
            .putBoolean(KEY_CONFIRM_DELETE, defaultSettings.confirmDelete)
            .putInt(KEY_DECIMAL_PLACES, defaultSettings.decimalPlaces)
            .putString(KEY_ROUNDING_MODE, defaultSettings.roundingMode.name)
            .apply()
        _settings.value = defaultSettings
    }

    fun saveLastInputs(
        type: SequenceType,
        firstTerm: String,
        stepOrRatio: String,
        numberOfTerms: String
    ) {
        prefs.edit()
            .putString(KEY_LAST_TYPE, type.name)
            .putString(KEY_LAST_FIRST_TERM, firstTerm)
            .putString(KEY_LAST_STEP_RATIO, stepOrRatio)
            .putString(KEY_LAST_NUM_TERMS, numberOfTerms)
            .apply()
    }

    fun getLastType(): SequenceType {
        val typeStr = prefs.getString(KEY_LAST_TYPE, SequenceType.ARITHMETIC.name)
            ?: SequenceType.ARITHMETIC.name
        return try {
            SequenceType.valueOf(typeStr)
        } catch (e: Exception) {
            SequenceType.ARITHMETIC
        }
    }

    fun getLastFirstTerm(): String = prefs.getString(KEY_LAST_FIRST_TERM, "") ?: ""
    fun getLastStepOrRatio(): String = prefs.getString(KEY_LAST_STEP_RATIO, "") ?: ""
    fun getLastNumberOfTerms(): String = prefs.getString(KEY_LAST_NUM_TERMS, "10") ?: "10"

    companion object {
        private const val PREFS_NAME = "sequencebox_preferences"
        private const val KEY_THEME = "theme_mode"
        private const val KEY_DYNAMIC_COLOR = "dynamic_color"
        private const val KEY_HAPTICS = "haptics_enabled"
        private const val KEY_ANIMATIONS = "animations_enabled"
        private const val KEY_CONFIRM_DELETE = "confirm_delete"
        private const val KEY_DECIMAL_PLACES = "decimal_places"
        private const val KEY_ROUNDING_MODE = "rounding_mode"
        private const val KEY_LAST_TYPE = "last_sequence_type"
        private const val KEY_LAST_FIRST_TERM = "last_first_term"
        private const val KEY_LAST_STEP_RATIO = "last_step_ratio"
        private const val KEY_LAST_NUM_TERMS = "last_num_terms"
    }
}
