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

    fun localizedRouteLabel(raw: String): String {
        val text = raw.trim()
        if (text.isBlank()) return text

        val entryMatch = Regex("^Въезд\\s+в\\s+(.+?)\\s+из\\s+(.+?)(?:\\s*\\(\\d+\\))?$")
            .find(text)
        val exitMatch = Regex("^Выезд\\s+из\\s+(.+?)\\s+в\\s+(.+?)(?:\\s*\\(\\d+\\))?$")
            .find(text)

        return when (selectedLanguage) {
            "en" -> when {
                entryMatch != null -> "Entry to ${entryMatch.groupValues[1]} from ${entryMatch.groupValues[2]}${suffixFrom(raw)}"
                exitMatch != null -> "Exit from ${exitMatch.groupValues[1]} to ${exitMatch.groupValues[2]}${suffixFrom(raw)}"
                else -> raw
            }
            "uk" -> when {
                entryMatch != null -> "В'їзд до ${entryMatch.groupValues[1]} з ${entryMatch.groupValues[2]}${suffixFrom(raw)}"
                exitMatch != null -> "В'їзд з ${exitMatch.groupValues[1]} до ${exitMatch.groupValues[2]}${suffixFrom(raw)}"
                else -> raw
            }
            else -> raw
        }
    }

    private fun suffixFrom(raw: String): String {
        val match = Regex("(\\s*\\(\\d+\\))$").find(raw.trim())
        return match?.value.orEmpty()
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
            "choose_origin_short" -> "From"
            "choose_destination" -> "Choose destination country"
            "choose_destination_short" -> "To"
            "choose_border" -> "Choose crossing"
            "choose_border_short" -> "Crossings"
            "toggle_cameras" -> "Disable cameras"
            "language" -> "Interface language"
            "apply" -> "Apply"
            "dark_mode" -> "Dark mode"
            "camera_off" -> "Cameras disabled"
            "camera_on" -> "Cameras enabled"
            "view_borders" -> "View borders"
            "origin_header" -> "Where do we start?"
            "origin_subtitle" -> "Choose your departure country"
            "origin_details" -> "Next we will show countries with available borders and cameras"
            "destination_header" -> "Where can you go?"
            "destination_subtitle" -> "Choose destination country"
            "crossings_header" -> "Crossings and cameras"
            "crossings_subtitle" -> "Choose a crossing point"
            "count" -> "count"
            "crossing_count" -> "crossings"
            "camera_count" -> "cameras"
            "all_cameras" -> "Cameras on the border"
            "wait_time" -> "Waiting time"
            "camera_disabled" -> "Camera disabled"
            "tap_to_enable" -> "Tap to enable"
            "open_fullscreen" -> "Open full screen"
            "settings_subtitle" -> "Comfort & preferences"
            "cameras_hint" -> "If enabled, cameras stay off by default; if disabled, streams open immediately."
            "dark_mode_hint" -> "Better for night use and easier on the eyes."
            "language_hint" -> "Use the language that feels most comfortable for you."
            "empty_country_title" -> "No verified cameras are available for this country yet"
            "empty_country_desc" -> "We add only real checked sources — this country is still waiting to be added."
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
            "choose_origin_short" -> "Звідки"
            "choose_destination" -> "Виберіть країну призначення"
            "choose_destination_short" -> "Куди"
            "choose_border" -> "Виберіть перехід"
            "choose_border_short" -> "Переходи"
            "toggle_cameras" -> "Вимкнути камери"
            "language" -> "Мова інтерфейсу"
            "apply" -> "Застосувати"
            "dark_mode" -> "Темний режим"
            "camera_off" -> "Камери вимкнено"
            "camera_on" -> "Камери увімкнено"
            "view_borders" -> "Переглянути кордони"
            "origin_header" -> "Звідки починаємо?"
            "origin_subtitle" -> "Виберіть країну виїзду"
            "origin_details" -> "Далі покажемо країни, з якими є кордон і камери"
            "destination_header" -> "Куди можна їхати?"
            "destination_subtitle" -> "Виберіть країну призначення"
            "crossings_header" -> "Переходи та камери"
            "crossings_subtitle" -> "Виберіть точку перетину"
            "count" -> "к-ть"
            "crossing_count" -> "переходів"
            "camera_count" -> "камер"
            "all_cameras" -> "Камери на кордоні"
            "wait_time" -> "Час очікування"
            "camera_disabled" -> "Камера вимкнена"
            "tap_to_enable" -> "Натисніть, щоб увімкнути"
            "open_fullscreen" -> "Відкрити на весь екран"
            "settings_subtitle" -> "Комфорт і налаштування"
            "cameras_hint" -> "Якщо увімкнено, камери залишаються вимкненими за замовчуванням; якщо вимкнено, потоки відкриваються одразу."
            "dark_mode_hint" -> "Краще для нічного використання і менш навантажує очі."
            "language_hint" -> "Виберіть мову, яка вам найбільше комфортна."
            "empty_country_title" -> "Поки немає перевірених камер для кордонів цієї країни"
            "empty_country_desc" -> "Ми додаємо лише реальні перевірені джерела — ця країна ще в черзі на додавання."
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
            "choose_origin_short" -> "Откуда"
            "choose_destination" -> "Выберите страну назначения"
            "choose_destination_short" -> "Куда"
            "choose_border" -> "Выберите переход"
            "choose_border_short" -> "Переходы"
            "toggle_cameras" -> "Выключение камер"
            "language" -> "Язык интерфейса"
            "apply" -> "Применить"
            "dark_mode" -> "Тёмный режим"
            "camera_off" -> "Камера выключена"
            "camera_on" -> "Камера включена"
            "view_borders" -> "Смотреть границы"
            "origin_header" -> "Откуда начинаем"
            "origin_subtitle" -> "Выберите страну выезда"
            "origin_details" -> "Дальше покажем страны, с которыми есть граница и камеры"
            "destination_header" -> "Куда можно ехать"
            "destination_subtitle" -> "Выберите страну назначения"
            "crossings_header" -> "Переходы и камеры"
            "crossings_subtitle" -> "Выберите точку пересечения"
            "count" -> "шт"
            "crossing_count" -> "переходов"
            "camera_count" -> "камер"
            "all_cameras" -> "Камеры на границе"
            "wait_time" -> "Время ожидания"
            "camera_disabled" -> "Камера выключена"
            "tap_to_enable" -> "Нажмите, чтобы включить"
            "open_fullscreen" -> "Открыть во весь экран"
            "settings_subtitle" -> "Комфорт и предпочтения"
            "cameras_hint" -> "Если включено, камеры остаются выключенными по умолчанию; если выключено, потоки открываются сразу."
            "dark_mode_hint" -> "Лучше для ночного использования и легче для глаз."
            "language_hint" -> "Выберите язык, который вам наиболее комфортен."
            "empty_country_title" -> "Пока нет проверенных камер для границ этой страны"
            "empty_country_desc" -> "Мы добавляем только реальные, проверенные источники — эта страна ещё в очереди на добавление."
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
