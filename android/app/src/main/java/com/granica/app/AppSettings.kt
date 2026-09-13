package com.granica.app

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

class AppSettings(context: Context) {
    private val prefs = context.getSharedPreferences("granica_settings", Context.MODE_PRIVATE)

    var disableCameras: Boolean
        get() = prefs.getBoolean(KEY_DISABLE_CAMERAS, true)
        set(value) = prefs.edit().putBoolean(KEY_DISABLE_CAMERAS, value).apply()

    var darkModeEnabled: Boolean
        get() = prefs.getBoolean(KEY_DARK_MODE, false)
        set(value) = prefs.edit().putBoolean(KEY_DARK_MODE, value).apply()

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

    fun localizedCountryName(countryCode: String, fallback: String = countryCode): String {
        val code = countryCode.trim().uppercase()
        if (code.isBlank()) return fallback

        val locale = Locale(selectedLanguage)
        val countryLocale = when (code) {
            "XK" -> Locale("sq", "XK")
            else -> Locale("", code)
        }

        return try {
            val display = countryLocale.getDisplayCountry(locale)
            if (display.isNullOrBlank() || display == code) fallback else display
        } catch (_: Exception) {
            fallback
        }
    }

    fun uiText(key: String): String = when (selectedLanguage) {
        "en" -> when (key) {
            "app_name" -> "Granica"
            "from" -> "From"
            "to" -> "To"
            "routes" -> "Routes"
            "favorites" -> "Favorites"
            "settings" -> "Settings"
            "choose_origin" -> "Choose departure country"
            "choose_destination" -> "Choose destination country"
            "choose_border" -> "Choose crossing"
            "toggle_cameras" -> "Disable cameras"
            "language" -> "Interface language"
            "apply" -> "Apply"
            "dark_mode" -> "Dark mode"
            "camera_off" -> "Cameras disabled"
            "camera_on" -> "Cameras enabled"
            else -> key
        }
        "uk" -> when (key) {
            "app_name" -> "Granica"
            "from" -> "Звідки"
            "to" -> "Куди"
            "routes" -> "Маршрути"
            "favorites" -> "Вибране"
            "settings" -> "Налаштування"
            "choose_origin" -> "Виберіть країну виїзду"
            "choose_destination" -> "Виберіть країну призначення"
            "choose_border" -> "Виберіть перехід"
            "toggle_cameras" -> "Вимкнути камери"
            "language" -> "Мова інтерфейсу"
            "apply" -> "Застосувати"
            "dark_mode" -> "Темний режим"
            "camera_off" -> "Камери вимкнено"
            "camera_on" -> "Камери увімкнено"
            else -> key
        }
        else -> when (key) {
            "app_name" -> "Granica"
            "from" -> "Откуда"
            "to" -> "Куда"
            "routes" -> "Переходы"
            "favorites" -> "Избранное"
            "settings" -> "Настройки"
            "choose_origin" -> "Выберите страну выезда"
            "choose_destination" -> "Выберите страну назначения"
            "choose_border" -> "Выберите переход"
            "toggle_cameras" -> "Выключение камер"
            "language" -> "Язык интерфейса"
            "apply" -> "Применить"
            "dark_mode" -> "Тёмный режим"
            "camera_off" -> "Камера выключена"
            "camera_on" -> "Камера включена"
            else -> key
        }
    }

    data class LanguageOption(val code: String, val label: String)

    companion object {
        private const val KEY_DISABLE_CAMERAS = "disable_cameras"
        private const val KEY_DARK_MODE = "dark_mode_enabled"
        private const val KEY_LANGUAGE = "selected_language"
        const val DEFAULT_LANGUAGE = "ru"
    }
}
