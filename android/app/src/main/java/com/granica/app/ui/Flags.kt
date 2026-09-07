package com.granica.app.ui

/** Converts a 2-letter ISO country code into a flag emoji using Unicode regional indicators. */
fun countryFlagEmoji(code: String): String {
    if (code.equals("DEMO", ignoreCase = true)) return "\uD83E\uDDEA" // 🧪
    val upper = code.uppercase()
    if (upper.length != 2) return "\uD83C\uDF10" // 🌐 fallback
    val base = 0x1F1E6
    val first = base + (upper[0] - 'A')
    val second = base + (upper[1] - 'A')
    return String(Character.toChars(first)) + String(Character.toChars(second))
}
