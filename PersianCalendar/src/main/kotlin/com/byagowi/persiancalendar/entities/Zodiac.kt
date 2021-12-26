package com.byagowi.persiancalendar.entities

import android.content.Context
import android.os.Build
import androidx.annotation.StringRes
import com.byagowi.persiancalendar.R
import com.cepmuvakkit.times.posAlgo.Ecliptic
import io.github.persiancalendar.calendar.PersianDate

enum class Zodiac(
    val endOfRange: Double, private val emoji: String, @StringRes private val title: Int
) {
    ARIES(33.18, "♈", R.string.aries),
    TAURUS(51.16, "♉", R.string.taurus),
    GEMINI(93.44, "♊", R.string.gemini),
    CANCER(119.48, "♋", R.string.cancer),
    LEO(135.30, "♌", R.string.leo),
    VIRGO(173.34, "♍", R.string.virgo),
    LIBRA(224.17, "♎", R.string.libra),
    SCORPIO(242.57, "♏", R.string.scorpio),
    SAGITTARIUS(271.26, "♐", R.string.sagittarius),
    CAPRICORN(302.49, "♑", R.string.capricorn),
    AQUARIUS(311.72, "♒", R.string.aquarius),
    PISCES(348.58, "♓", R.string.pisces);

    fun format(context: Context, withEmoji: Boolean) = buildString {
        if (withEmoji && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) append("$emoji ")
        append(context.getString(title))
    }

    val centerOfRange
        get() = (((values().getOrNull(ordinal - 1)?.endOfRange) ?: PISCES.endOfRange - 360) +
                endOfRange) / 2

    companion object {
        fun fromPersianCalendar(persianDate: PersianDate) =
            values().getOrNull(persianDate.month - 1) ?: ARIES

        // https://github.com/janczer/goMoonPhase/blob/0363844/MoonPhase.go#L363
        fun fromEcliptic(ecliptic: Ecliptic) =
            values().firstOrNull { ecliptic.λ < it.endOfRange } ?: ARIES
    }
}
