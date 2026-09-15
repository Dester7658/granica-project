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

        val entryMatch = Regex("^Въезд\\s+в\\s+(.+?)\\s+из\\s+(.+?)(?:\\s*\\(\\d+\\))?$").find(text)
        val exitMatch = Regex("^Выезд\\s+из\\s+(.+?)\\s+в\\s+(.+?)(?:\\s*\\(\\d+\\))?$").find(text)

        val normalized = when {
            entryMatch != null -> {
                val countryTo = translateCountryName(entryMatch.groupValues[1], selectedLanguage)
                val countryFrom = translateCountryName(entryMatch.groupValues[2], selectedLanguage)
                if (countryTo == entryMatch.groupValues[1] || countryFrom == entryMatch.groupValues[2]) {
                    text
                } else {
                    routeTemplateForLanguage("entry", countryTo, countryFrom, selectedLanguage) + suffixFrom(raw)
                }
            }
            exitMatch != null -> {
                val countryFrom = translateCountryName(exitMatch.groupValues[1], selectedLanguage)
                val countryTo = translateCountryName(exitMatch.groupValues[2], selectedLanguage)
                if (countryFrom == exitMatch.groupValues[1] || countryTo == exitMatch.groupValues[2]) {
                    text
                } else {
                    routeTemplateForLanguage("exit", countryFrom, countryTo, selectedLanguage) + suffixFrom(raw)
                }
            }
            else -> text
        }

        return normalized
    }

    private fun routeTemplateForLanguage(type: String, countryA: String, countryB: String, locale: String): String = when (locale) {
        "en" -> if (type == "entry") "Entry to $countryA from $countryB" else "Exit from $countryA to $countryB"
        "uk" -> if (type == "entry") "В'їзд до $countryA з $countryB" else "В'їзд з $countryA до $countryB"
        "be" -> if (type == "entry") "Уезд у $countryA з $countryB" else "Выхад з $countryA у $countryB"
        "de" -> if (type == "entry") "Einreise nach $countryA aus $countryB" else "Ausfahrt aus $countryA nach $countryB"
        "fr" -> if (type == "entry") "Entrée en $countryA depuis $countryB" else "Sortie de $countryA vers $countryB"
        "es" -> if (type == "entry") "Entrada a $countryA desde $countryB" else "Salida de $countryA hacia $countryB"
        "cs" -> if (type == "entry") "Vjezd do $countryA z $countryB" else "Výjezd z $countryA do $countryB"
        "pl" -> if (type == "entry") "Wejazd do $countryA z $countryB" else "Wyjazd z $countryA do $countryB"
        "ro" -> if (type == "entry") "Intrare în $countryA din $countryB" else "Ieșire din $countryA spre $countryB"
        "sr", "hr" -> if (type == "entry") "Ulazak u $countryA iz $countryB" else "Izlaz iz $countryA u $countryB"
        "hu" -> if (type == "entry") "Belépés $countryA-ba $countryB-ből" else "Kilépés $countryA-ból $countryB-be"
        else -> if (type == "entry") "Entry to $countryA from $countryB" else "Exit from $countryA to $countryB"
    }

    private fun translateCountryName(name: String, locale: String): String {
        val lower = name.trim()
        if (lower.isBlank()) return lower

        val map = mapOf(
            "Хорватия" to mapOf("en" to "Croatia", "uk" to "Хорватія", "be" to "Харватыя", "de" to "Kroatien", "hu" to "Horvátország", "sr" to "Hrvatska", "hr" to "Hrvatska", "fr" to "Croatie", "es" to "Croacia", "cs" to "Chorvatsko", "pl" to "Chorwacja", "ro" to "Croația"),
            "Сербия" to mapOf("en" to "Serbia", "uk" to "Сербія", "be" to "Сербія", "de" to "Serbien", "hu" to "Szerbia", "sr" to "Srbija", "hr" to "Srbija", "fr" to "Serbie", "es" to "Serbia", "cs" to "Srbsko", "pl" to "Serbia", "ro" to "Serbia"),
            "Черногория" to mapOf("en" to "Montenegro", "uk" to "Чорногорія", "be" to "Чарнагорыя", "de" to "Montenegro", "hu" to "Montenegró", "sr" to "Crna Gora", "hr" to "Crna Gora", "fr" to "Monténégro", "es" to "Montenegro", "cs" to "Černá Hora", "pl" to "Czarnogóra", "ro" to "Muntenegru"),
            "Босния" to mapOf("en" to "Bosnia", "uk" to "Боснія", "be" to "Баснія", "de" to "Bosnien", "hu" to "Bosznia", "sr" to "Bosna", "hr" to "Bosna", "fr" to "Bosnie", "es" to "Bosnia", "cs" to "Bosna", "pl" to "Bośnia", "ro" to "Bosnia"),
            "Венгрия" to mapOf("en" to "Hungary", "uk" to "Угорщина", "be" to "Венгрыя", "de" to "Ungarn", "hu" to "Magyarország", "sr" to "Mađarska", "hr" to "Mađarska", "fr" to "Hongrie", "es" to "Hungría", "cs" to "Maďarsko", "pl" to "Węgry", "ro" to "Ungaria"),
            "Румыния" to mapOf("en" to "Romania", "uk" to "Румунія", "be" to "Румынія", "de" to "Rumänien", "hu" to "Románia", "sr" to "Rumunija", "hr" to "Rumunjska", "fr" to "Roumanie", "es" to "Rumanía", "cs" to "Rumunsko", "pl" to "Rumunia", "ro" to "România"),
            "Болгария" to mapOf("en" to "Bulgaria", "uk" to "Болгарія", "be" to "Балгарыя", "de" to "Bulgarien", "hu" to "Bulgária", "sr" to "Bugarska", "hr" to "Bugarska", "fr" to "Bulgarie", "es" to "Bulgaria", "cs" to "Bulharsko", "pl" to "Bułgaria", "ro" to "Bulgaria"),
            "Турция" to mapOf("en" to "Turkey", "uk" to "Туреччина", "be" to "Турцыя", "de" to "Türkei", "hu" to "Törökország", "sr" to "Turska", "hr" to "Turska", "fr" to "Turquie", "es" to "Turquía", "cs" to "Turecko", "pl" to "Turcja", "ro" to "Turcia"),
            "Украина" to mapOf("en" to "Ukraine", "uk" to "Україна", "be" to "Украіна", "de" to "Ukraine", "hu" to "Ukrajna", "sr" to "Ukrajina", "hr" to "Ukrajina", "fr" to "Ukraine", "es" to "Ucrania", "cs" to "Ukrajina", "pl" to "Ukraina", "ro" to "Ucraina"),
            "Молдова" to mapOf("en" to "Moldova", "uk" to "Молдова", "be" to "Малдова", "de" to "Moldau", "hu" to "Moldova", "sr" to "Moldavija", "hr" to "Moldavija", "fr" to "Moldavie", "es" to "Moldavia", "cs" to "Moldavsko", "pl" to "Mołdawia", "ro" to "Moldova"),
            "Словакия" to mapOf("en" to "Slovakia", "uk" to "Словаччина", "be" to "Славакія", "de" to "Slowakei", "hu" to "Szlovákia", "sr" to "Slovačka", "hr" to "Slovačka", "fr" to "Slovaquie", "es" to "Eslovaquia", "cs" to "Slovensko", "pl" to "Słowacja", "ro" to "Slovacia"),
            "Словения" to mapOf("en" to "Slovenia", "uk" to "Словенія", "be" to "Славенія", "de" to "Slowenien", "hu" to "Szlovénia", "sr" to "Slovenija", "hr" to "Slovenija", "fr" to "Slovénie", "es" to "Eslovenia", "cs" to "Slovinsko", "pl" to "Słowenia", "ro" to "Slovenia"),
            "Греция" to mapOf("en" to "Greece", "uk" to "Греція", "be" to "Грэцыя", "de" to "Griechenland", "hu" to "Görögország", "sr" to "Grčka", "hr" to "Grčka", "fr" to "Grèce", "es" to "Grecia", "cs" to "Řecko", "pl" to "Grecja", "ro" to "Grecia"),
            "Австрия" to mapOf("en" to "Austria", "uk" to "Австрія", "be" to "Аўстрыя", "de" to "Österreich", "hu" to "Ausztria", "sr" to "Austrija", "hr" to "Austrija", "fr" to "Autriche", "es" to "Austria", "cs" to "Rakousko", "pl" to "Austria", "ro" to "Austria"),
            "Италия" to mapOf("en" to "Italy", "uk" to "Італія", "be" to "Італія", "de" to "Italien", "hu" to "Olaszország", "sr" to "Italija", "hr" to "Italija", "fr" to "Italie", "es" to "Italia", "cs" to "Itálie", "pl" to "Włochy", "ro" to "Italia"),
            "Франция" to mapOf("en" to "France", "uk" to "Франція", "be" to "Францыя", "de" to "Frankreich", "hu" to "Franciaország", "sr" to "Francuska", "hr" to "Francuska", "fr" to "France", "es" to "Francia", "cs" to "Francie", "pl" to "Francja", "ro" to "Franța"),
            "Германия" to mapOf("en" to "Germany", "uk" to "Німеччина", "be" to "Германія", "de" to "Deutschland", "hu" to "Németország", "sr" to "Nemačka", "hr" to "Njemačka", "fr" to "Allemagne", "es" to "Alemania", "cs" to "Německo", "pl" to "Niemcy", "ro" to "Germania")
        )

        val direct = map[lower]?.get(locale)
        if (direct != null) return direct

        val normalized = lower.replace("ё", "е")
        val alt = map[normalized]?.get(locale)
        if (alt != null) return alt

        return lower
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
            "minutes" -> "min"
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
            "minutes" -> "хв"
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
        "be" -> when (key) {
            "app_name" -> "Granica"
            "from" -> "Адкуль"
            "to" -> "Куды"
            "routes" -> "Марышруты"
            "favorites" -> "Улюбенае"
            "settings" -> "Налады"
            "choose_origin" -> "Выберыце краіну выезду"
            "choose_origin_short" -> "Адкуль"
            "choose_destination" -> "Выберыце краіну прызначэння"
            "choose_destination_short" -> "Куды"
            "choose_border" -> "Выберыце пераход"
            "choose_border_short" -> "Пераходы"
            "toggle_cameras" -> "Адключыць камеры"
            "language" -> "Мова інтэрфейсу"
            "apply" -> "Прымяніць"
            "dark_mode" -> "Цёмны рэжым"
            "camera_off" -> "Камеры адключаны"
            "camera_on" -> "Камеры ўключаны"
            "view_borders" -> "Прагляд межаў"
            "origin_header" -> "Адкуль пачынаем?"
            "origin_subtitle" -> "Выберыце краіну выезду"
            "origin_details" -> "Далей паказваем краіны, з якімі ёсць мяжа і камеры"
            "destination_header" -> "Куды можна ехаць?"
            "destination_subtitle" -> "Выберыце краіну прыездку"
            "crossings_header" -> "Пераходы і камеры"
            "crossings_subtitle" -> "Выберыце кропку пераходу"
            "count" -> "шт"
            "crossing_count" -> "пераходаў"
            "camera_count" -> "камер"
            "all_cameras" -> "Камеры на мяжы"
            "wait_time" -> "Час чакання"
            "minutes" -> "хв"
            "camera_disabled" -> "Камера адключана"
            "tap_to_enable" -> "Націсніце, каб уключыць"
            "open_fullscreen" -> "Адкрыць на ўвесь экран"
            "settings_subtitle" -> "Зручнасць і перавагі"
            "cameras_hint" -> "Калі ўключана, камеры застаюцца адключанымі па змаўчанні; калі адключана, патокі адкрываюцца адразу."
            "dark_mode_hint" -> "Лепш для начнога выкарыстання і менш нагружае вочы."
            "language_hint" -> "Выберыце мову, якая вам найбольш зручная."
            "empty_country_title" -> "Пакуль няма правераных камер для межаў гэтай краіны"
            "empty_country_desc" -> "Мы дадаем толькі сапраўдныя правераныя крыніцы — гэтая краіна ўсё яшчэ ў чарзе на даданне."
            else -> key
        }
        "de" -> when (key) {
            "app_name" -> "Granica"
            "from" -> "Von"
            "to" -> "Nach"
            "routes" -> "Routen"
            "favorites" -> "Favoriten"
            "settings" -> "Einstellungen"
            "choose_origin" -> "Abfahrtsland wählen"
            "choose_origin_short" -> "Von"
            "choose_destination" -> "Zielland wählen"
            "choose_destination_short" -> "Nach"
            "choose_border" -> "Grenzübergang wählen"
            "choose_border_short" -> "Grenzübergänge"
            "toggle_cameras" -> "Kameras deaktivieren"
            "language" -> "Sprache der Oberfläche"
            "apply" -> "Anwenden"
            "dark_mode" -> "Dunkelmodus"
            "camera_off" -> "Kameras deaktiviert"
            "camera_on" -> "Kameras aktiviert"
            "view_borders" -> "Grenzen anzeigen"
            "origin_header" -> "Wo starten wir?"
            "origin_subtitle" -> "Wählen Sie Ihr Abfahrtsland"
            "origin_details" -> "Als Nächstes zeigen wir Länder mit verfügbaren Grenzen und Kameras"
            "destination_header" -> "Wohin können Sie fahren?"
            "destination_subtitle" -> "Wählen Sie das Zielland"
            "crossings_header" -> "Grenzübergänge und Kameras"
            "crossings_subtitle" -> "Wählen Sie einen Übergang"
            "count" -> "Anzahl"
            "crossing_count" -> "Übergänge"
            "camera_count" -> "Kameras"
            "all_cameras" -> "Kameras an der Grenze"
            "wait_time" -> "Wartezeit"
            "minutes" -> "Min."
            "camera_disabled" -> "Kamera deaktiviert"
            "tap_to_enable" -> "Zum Aktivieren tippen"
            "open_fullscreen" -> "Im Vollbild öffnen"
            "settings_subtitle" -> "Komfort & Einstellungen"
            "cameras_hint" -> "Wenn aktiviert, bleiben Kameras standardmäßig aus; wenn deaktiviert, öffnen Streams sofort."
            "dark_mode_hint" -> "Besser für die Nacht und angenehmer für die Augen."
            "language_hint" -> "Wählen Sie die Sprache, die für Sie am angenehmsten ist."
            "empty_country_title" -> "Für dieses Land gibt es noch keine verifizierten Kameras"
            "empty_country_desc" -> "Wir fügen nur echte geprüfte Quellen hinzu — dieses Land wartet noch auf Ergänzung."
            else -> key
        }
        "hu" -> when (key) {
            "app_name" -> "Granica"
            "from" -> "Honnan"
            "to" -> "Hova"
            "routes" -> "Útvonalak"
            "favorites" -> "Kedvencek"
            "settings" -> "Beállítások"
            "choose_origin" -> "Válassza ki az indulási országot"
            "choose_origin_short" -> "Honnan"
            "choose_destination" -> "Válassza ki a célországot"
            "choose_destination_short" -> "Hova"
            "choose_border" -> "Válasszon határátkelőt"
            "choose_border_short" -> "Átkelőhelyek"
            "toggle_cameras" -> "Kamerák letiltása"
            "language" -> "Felület nyelve"
            "apply" -> "Alkalmaz"
            "dark_mode" -> "Sötét mód"
            "camera_off" -> "Kamerák kikapcsolva"
            "camera_on" -> "Kamerák bekapcsolva"
            "view_borders" -> "Határok megtekintése"
            "origin_header" -> "Honnan kezdjük?"
            "origin_subtitle" -> "Válassza ki az indulási országot"
            "origin_details" -> "Ezután megmutatjuk azokat az országokat, amelyekben határ és kamerák is vannak"
            "destination_header" -> "Hova lehet utazni?"
            "destination_subtitle" -> "Válassza ki a célországot"
            "crossings_header" -> "Átkelőhelyek és kamerák"
            "crossings_subtitle" -> "Válasszon egy átkelési pontot"
            "count" -> "db"
            "crossing_count" -> "átkelő"
            "camera_count" -> "kamera"
            "all_cameras" -> "Kamerák a határon"
            "wait_time" -> "Várakozási idő"
            "minutes" -> "perc"
            "camera_disabled" -> "Kamera kikapcsolva"
            "tap_to_enable" -> "Érintse meg a bekapcsoláshoz"
            "open_fullscreen" -> "Megnyitás teljes képernyőn"
            "settings_subtitle" -> "Kényelem és beállítások"
            "cameras_hint" -> "Ha bekapcsolva van, a kamerák alapértelmezetten ki vannak kapcsolva; ha ki van kapcsolva, a stream azonnal megnyílik."
            "dark_mode_hint" -> "Jobb az éjszakai használathoz és kíméli a szemet."
            "language_hint" -> "Válassza ki azt a nyelvet, amely a legkényelmesebb az Önnek."
            "empty_country_title" -> "Még nincs ellenőrzött kamera ehhez az országhoz"
            "empty_country_desc" -> "Csak valós, ellenőrzött forrásokat adunk hozzá — ez az ország még vár a bővítésre."
            else -> key
        }
        "sr" -> when (key) {
            "app_name" -> "Granica"
            "from" -> "Odakle"
            "to" -> "Gde"
            "routes" -> "Rute"
            "favorites" -> "Omiljeno"
            "settings" -> "Podešavanja"
            "choose_origin" -> "Izaberite zemlju polaska"
            "choose_origin_short" -> "Odakle"
            "choose_destination" -> "Izaberite zemlju odredišta"
            "choose_destination_short" -> "Gde"
            "choose_border" -> "Izaberite prelaz"
            "choose_border_short" -> "Prelazi"
            "toggle_cameras" -> "Isključi kamere"
            "language" -> "Jezik interfejsa"
            "apply" -> "Primeni"
            "dark_mode" -> "Tamni režim"
            "camera_off" -> "Kamere isključene"
            "camera_on" -> "Kamere uključene"
            "view_borders" -> "Pogledaj granice"
            "origin_header" -> "Odakle počinjemo?"
            "origin_subtitle" -> "Izaberite zemlju polaska"
            "origin_details" -> "Sledeće ćemo prikazati zemlje sa dostupnim granicama i kamerama"
            "destination_header" -> "Gde možete ići?"
            "destination_subtitle" -> "Izaberite zemlju odredišta"
            "crossings_header" -> "Prelazi i kamere"
            "crossings_subtitle" -> "Izaberite tačku prelaza"
            "count" -> "kom"
            "crossing_count" -> "prelaza"
            "camera_count" -> "kamera"
            "all_cameras" -> "Kamere na granici"
            "wait_time" -> "Vreme čekanja"
            "minutes" -> "min"
            "camera_disabled" -> "Kamera isključena"
            "tap_to_enable" -> "Dodirnite da uključite"
            "open_fullscreen" -> "Otvori preko celog ekrana"
            "settings_subtitle" -> "Udobnost i podešavanja"
            "cameras_hint" -> "Ako je uključeno, kamere su po defaultu isključene; ako je isključeno, stream se otvara odmah."
            "dark_mode_hint" -> "Bolje za noćnu upotrebu i lakše za oči."
            "language_hint" -> "Izaberite jezik koji vam je najprijatniji."
            "empty_country_title" -> "Još nema proverenih kamera za ovu zemlju"
            "empty_country_desc" -> "Dodajemo samo stvarne proverene izvore — ova zemlja još čeka da se doda."
            else -> key
        }
        "hr" -> when (key) {
            "app_name" -> "Granica"
            "from" -> "Odakle"
            "to" -> "Gdje"
            "routes" -> "Rute"
            "favorites" -> "Favoriti"
            "settings" -> "Postavke"
            "choose_origin" -> "Odaberite zemlju polaska"
            "choose_origin_short" -> "Odakle"
            "choose_destination" -> "Odaberite zemlju odredišta"
            "choose_destination_short" -> "Gdje"
            "choose_border" -> "Odaberite prijelaz"
            "choose_border_short" -> "Prijelazi"
            "toggle_cameras" -> "Isključi kamere"
            "language" -> "Jezik sučelja"
            "apply" -> "Primijeni"
            "dark_mode" -> "Tamni način"
            "camera_off" -> "Kamere isključene"
            "camera_on" -> "Kamere uključene"
            "view_borders" -> "Pogledaj granice"
            "origin_header" -> "Odakle počinjemo?"
            "origin_subtitle" -> "Odaberite zemlju polaska"
            "origin_details" -> "U nastavku prikazat ćemo zemlje s dostupnim granicama i kamerama"
            "destination_header" -> "Gdje možete ići?"
            "destination_subtitle" -> "Odaberite zemlju odredišta"
            "crossings_header" -> "Prijelazi i kamere"
            "crossings_subtitle" -> "Odaberite točku prijelaza"
            "count" -> "kom"
            "crossing_count" -> "prijelaza"
            "camera_count" -> "kamera"
            "all_cameras" -> "Kamere na granici"
            "wait_time" -> "Vrijeme čekanja"
            "minutes" -> "min"
            "camera_disabled" -> "Kamera isključena"
            "tap_to_enable" -> "Dodirnite za uključivanje"
            "open_fullscreen" -> "Otvori preko cijelog ekrana"
            "settings_subtitle" -> "Udobnost i postavke"
            "cameras_hint" -> "Ako je uključeno, kamere su po zadano isključene; ako je isključeno, stream se odmah otvara."
            "dark_mode_hint" -> "Bolje za noćnu upotrebu i lakše za oči."
            "language_hint" -> "Odaberite jezik koji vam je najugodniji."
            "empty_country_title" -> "Još nema provjerenih kamera za ovu zemlju"
            "empty_country_desc" -> "Dodajemo samo stvarne provjerene izvore — ova zemlja još uvijek čeka da se doda."
            else -> key
        }
        "fr" -> when (key) {
            "app_name" -> "Granica"
            "from" -> "Depuis"
            "to" -> "Vers"
            "routes" -> "Routes"
            "favorites" -> "Favoris"
            "settings" -> "Paramètres"
            "choose_origin" -> "Choisissez le pays de départ"
            "choose_origin_short" -> "Depuis"
            "choose_destination" -> "Choisissez le pays de destination"
            "choose_destination_short" -> "Vers"
            "choose_border" -> "Choisissez un passage"
            "choose_border_short" -> "Passages"
            "toggle_cameras" -> "Désactiver les caméras"
            "language" -> "Langue de l'interface"
            "apply" -> "Appliquer"
            "dark_mode" -> "Mode sombre"
            "camera_off" -> "Caméras désactivées"
            "camera_on" -> "Caméras activées"
            "view_borders" -> "Voir les frontières"
            "origin_header" -> "D'où commençons-nous ?"
            "origin_subtitle" -> "Choisissez votre pays de départ"
            "origin_details" -> "Ensuite, nous montrerons les pays avec frontières et caméras disponibles"
            "destination_header" -> "Où pouvez-vous aller ?"
            "destination_subtitle" -> "Choisissez le pays de destination"
            "crossings_header" -> "Passages et caméras"
            "crossings_subtitle" -> "Choisissez un point de passage"
            "count" -> "nb"
            "crossing_count" -> "passages"
            "camera_count" -> "caméras"
            "all_cameras" -> "Caméras à la frontière"
            "wait_time" -> "Temps d'attente"
            "minutes" -> "min"
            "camera_disabled" -> "Caméra désactivée"
            "tap_to_enable" -> "Appuyez pour activer"
            "open_fullscreen" -> "Ouvrir en plein écran"
            "settings_subtitle" -> "Confort & préférences"
            "cameras_hint" -> "Si activé, les caméras sont désactivées par défaut ; si désactivé, les flux s'ouvrent immédiatement."
            "dark_mode_hint" -> "Mieux pour une utilisation nocturne et plus agréable pour les yeux."
            "language_hint" -> "Choisissez la langue qui vous convient le mieux."
            "empty_country_title" -> "Aucune caméra vérifiée n'est encore disponible pour ce pays"
            "empty_country_desc" -> "Nous ajoutons uniquement des sources réelles vérifiées — ce pays est encore en attente d'ajout."
            else -> key
        }
        "es" -> when (key) {
            "app_name" -> "Granica"
            "from" -> "Desde"
            "to" -> "Hacia"
            "routes" -> "Rutas"
            "favorites" -> "Favoritos"
            "settings" -> "Ajustes"
            "choose_origin" -> "Elige el país de salida"
            "choose_origin_short" -> "Desde"
            "choose_destination" -> "Elige el país de destino"
            "choose_destination_short" -> "Hacia"
            "choose_border" -> "Elige un paso"
            "choose_border_short" -> "Pasos"
            "toggle_cameras" -> "Desactivar cámaras"
            "language" -> "Idioma de la interfaz"
            "apply" -> "Aplicar"
            "dark_mode" -> "Modo oscuro"
            "camera_off" -> "Cámaras desactivadas"
            "camera_on" -> "Cámaras activadas"
            "view_borders" -> "Ver fronteras"
            "origin_header" -> "¿Desde dónde empezamos?"
            "origin_subtitle" -> "Elige tu país de salida"
            "origin_details" -> "A continuación mostraremos países con fronteras y cámaras disponibles"
            "destination_header" -> "¿A dónde puedes ir?"
            "destination_subtitle" -> "Elige el país de destino"
            "crossings_header" -> "Pasos y cámaras"
            "crossings_subtitle" -> "Elige un punto de paso"
            "count" -> "ud"
            "crossing_count" -> "pasos"
            "camera_count" -> "cámaras"
            "all_cameras" -> "Cámaras en la frontera"
            "wait_time" -> "Tiempo de espera"
            "minutes" -> "min"
            "camera_disabled" -> "Cámara desactivada"
            "tap_to_enable" -> "Toca para activar"
            "open_fullscreen" -> "Abrir a pantalla completa"
            "settings_subtitle" -> "Comodidad y preferencias"
            "cameras_hint" -> "Si está activado, las cámaras permanecen apagadas por defecto; si está desactivado, los flujos se abren de inmediato."
            "dark_mode_hint" -> "Mejor para uso nocturno y más cómodo para la vista."
            "language_hint" -> "Elige el idioma que te resulte más cómodo."
            "empty_country_title" -> "Todavía no hay cámaras verificadas para este país"
            "empty_country_desc" -> "Solo añadimos fuentes reales verificadas — este país aún está en espera de añadirse."
            else -> key
        }
        "cs" -> when (key) {
            "app_name" -> "Granica"
            "from" -> "Odkud"
            "to" -> "Kam"
            "routes" -> "Trasy"
            "favorites" -> "Oblíbené"
            "settings" -> "Nastavení"
            "choose_origin" -> "Vyberte země původu"
            "choose_origin_short" -> "Odkud"
            "choose_destination" -> "Vyberte cílovou zemi"
            "choose_destination_short" -> "Kam"
            "choose_border" -> "Vyberte přechod"
            "choose_border_short" -> "Přechody"
            "toggle_cameras" -> "Vypnout kamery"
            "language" -> "Jazyk rozhraní"
            "apply" -> "Použít"
            "dark_mode" -> "Tmavý režim"
            "camera_off" -> "Kamery vypnuty"
            "camera_on" -> "Kamery zapnuty"
            "view_borders" -> "Zobrazit hranice"
            "origin_header" -> "Odkud začínáme?"
            "origin_subtitle" -> "Vyberte zemi odjezdu"
            "origin_details" -> "Dále zobrazíme země s dostupnými hranicemi a kamerami"
            "destination_header" -> "Kam můžete jet?"
            "destination_subtitle" -> "Vyberte cílovou zemi"
            "crossings_header" -> "Přechody a kamery"
            "crossings_subtitle" -> "Vyberte bod přechodu"
            "count" -> "ks"
            "crossing_count" -> "přechodů"
            "camera_count" -> "kamer"
            "all_cameras" -> "Kamery na hranici"
            "wait_time" -> "Čas čekání"
            "minutes" -> "min"
            "camera_disabled" -> "Kamera vypnuta"
            "tap_to_enable" -> "Klepnutím zapnete"
            "open_fullscreen" -> "Otevřít na celou obrazovku"
            "settings_subtitle" -> "Pohodlí a preference"
            "cameras_hint" -> "Pokud je povoleno, kamery jsou ve výchozím nastavení vypnuté; pokud je zakázáno, stream se otevře okamžitě."
            "dark_mode_hint" -> "Lepší pro noční použití a méně zatěžuje oči."
            "language_hint" -> "Vyberte jazyk, který je pro vás nejpohodlnější."
            "empty_country_title" -> "Pro tuto zemi zatím neexistují ověřené kamery"
            "empty_country_desc" -> "Přidáváme pouze skutečné ověřené zdroje — tato země ještě čeká na přidání."
            else -> key
        }
        "pl" -> when (key) {
            "app_name" -> "Granica"
            "from" -> "Skąd"
            "to" -> "Dokąd"
            "routes" -> "Trasy"
            "favorites" -> "Ulubione"
            "settings" -> "Ustawienia"
            "choose_origin" -> "Wybierz kraj wyjazdu"
            "choose_origin_short" -> "Skąd"
            "choose_destination" -> "Wybierz kraj docelowy"
            "choose_destination_short" -> "Dokąd"
            "choose_border" -> "Wybierz przejście"
            "choose_border_short" -> "Przejścia"
            "toggle_cameras" -> "Wyłącz kamery"
            "language" -> "Język interfejsu"
            "apply" -> "Zastosuj"
            "dark_mode" -> "Tryb ciemny"
            "camera_off" -> "Kamery wyłączone"
            "camera_on" -> "Kamery włączone"
            "view_borders" -> "Pokaż granice"
            "origin_header" -> "Skąd zaczynamy?"
            "origin_subtitle" -> "Wybierz kraj wyjazdu"
            "origin_details" -> "Dalej pokażemy kraje z dostępnymi granicami i kamerami"
            "destination_header" -> "Dokąd można jechać?"
            "destination_subtitle" -> "Wybierz kraj docelowy"
            "crossings_header" -> "Przejścia i kamery"
            "crossings_subtitle" -> "Wybierz punkt przejścia"
            "count" -> "szt"
            "crossing_count" -> "przejść"
            "camera_count" -> "kamer"
            "all_cameras" -> "Kamery na granicy"
            "wait_time" -> "Czas oczekiwania"
            "minutes" -> "min"
            "camera_disabled" -> "Kamera wyłączona"
            "tap_to_enable" -> "Dotknij, aby włączyć"
            "open_fullscreen" -> "Otwórz na pełnym ekranie"
            "settings_subtitle" -> "Komfort i preferencje"
            "cameras_hint" -> "Jeśli włączone, kamery są domyślnie wyłączone; jeśli wyłączone, strumienie otwierają się od razu."
            "dark_mode_hint" -> "Lepsze do nocnego użytkowania i mniej obciąża oczy."
            "language_hint" -> "Wybierz język, który jest dla Ciebie najbardziej wygodny."
            "empty_country_title" -> "Dla tego kraju nie ma jeszcze zweryfikowanych kamer"
            "empty_country_desc" -> "Dodajemy tylko rzeczywiste, zweryfikowane źródła — ten kraj nadal czeka na dodanie."
            else -> key
        }
        "ro" -> when (key) {
            "app_name" -> "Granica"
            "from" -> "De unde"
            "to" -> "Unde"
            "routes" -> "Rute"
            "favorites" -> "Favorite"
            "settings" -> "Setări"
            "choose_origin" -> "Alegeți țara de plecare"
            "choose_origin_short" -> "De unde"
            "choose_destination" -> "Alegeți țara de destinație"
            "choose_destination_short" -> "Unde"
            "choose_border" -> "Alegeți trecerea"
            "choose_border_short" -> "Treceri"
            "toggle_cameras" -> "Oprește camerele"
            "language" -> "Limba interfeței"
            "apply" -> "Aplică"
            "dark_mode" -> "Mod întunecat"
            "camera_off" -> "Camere oprite"
            "camera_on" -> "Camere pornite"
            "view_borders" -> "Vezi frontierele"
            "origin_header" -> "De unde începem?"
            "origin_subtitle" -> "Alege țara de plecare"
            "origin_details" -> "În continuare vom arăta țările cu granițe și camere disponibile"
            "destination_header" -> "Unde puteți merge?"
            "destination_subtitle" -> "Alegeți țara de destinație"
            "crossings_header" -> "Treceri și camere"
            "crossings_subtitle" -> "Alegeți un punct de trecere"
            "count" -> "buc"
            "crossing_count" -> "treceri"
            "camera_count" -> "camere"
            "all_cameras" -> "Camere la frontieră"
            "wait_time" -> "Timp de așteptare"
            "minutes" -> "min"
            "camera_disabled" -> "Cameră oprită"
            "tap_to_enable" -> "Atinge pentru a activa"
            "open_fullscreen" -> "Deschide pe ecran complet"
            "settings_subtitle" -> "Confort și preferințe"
            "cameras_hint" -> "Dacă este activat, camerele rămân oprite în mod implicit; dacă este dezactivat, fluxurile se deschid imediat."
            "dark_mode_hint" -> "Mai bun pentru utilizare nocturnă și mai ușor pentru ochi."
            "language_hint" -> "Alegeți limba care vă este cea mai confortabilă."
            "empty_country_title" -> "Încă nu există camere verificate pentru această țară"
            "empty_country_desc" -> "Adăugăm doar surse reale verificate — această țară mai este în așteptare pentru a fi adăugată."
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
            "minutes" -> "мин"
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
