package com.granica.app

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

class AppSettings(context: Context) {
    private val prefs = context.getSharedPreferences("granica_settings", Context.MODE_PRIVATE)

    var disableCameras: Boolean
        get() = prefs.getBoolean(KEY_DISABLE_CAMERAS, true)
        set(value) = prefs.edit().putBoolean(KEY_DISABLE_CAMERAS, value).apply()

    var selectedLanguage: String
        get() = prefs.getString(KEY_LANGUAGE, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
        set(value) = prefs.edit().putString(KEY_LANGUAGE, value).apply()

    fun availableLanguages(): List<LanguageOption> = listOf(
        LanguageOption("ru", "Русский"),
        LanguageOption("uk", "Українська"),
        LanguageOption("be", "Беларуская"),
        LanguageOption("en", "English"),
        LanguageOption("de", "Deutsch"),
        LanguageOption("hu", "Magyar"),
        LanguageOption("sr", "Srpski"),
        LanguageOption("hr", "Hrvatski"),
        LanguageOption("fr", "Français"),
        LanguageOption("es", "Español"),
        LanguageOption("cs", "Čeština"),
        LanguageOption("pl", "Polski"),
        LanguageOption("ro", "Română")
    )

    fun applySelectedLanguage(context: Context) {
        val locale = Locale(selectedLanguage)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    data class LanguageOption(val code: String, val label: String)

    companion object {
        private const val KEY_DISABLE_CAMERAS = "disable_cameras"
        private const val KEY_LANGUAGE = "selected_language"
        const val DEFAULT_LANGUAGE = "ru"
    }
}
