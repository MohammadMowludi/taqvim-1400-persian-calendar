package com.byagowi.persiancalendar.ui.preferences.interfacecalendar

import android.Manifest
import android.animation.ValueAnimator
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import com.byagowi.persiancalendar.DEFAULT_ISLAMIC_OFFSET
import com.byagowi.persiancalendar.DEFAULT_WEEK_ENDS
import com.byagowi.persiancalendar.DEFAULT_WEEK_START
import com.byagowi.persiancalendar.PREF_APP_LANGUAGE
import com.byagowi.persiancalendar.PREF_ASTRONOMICAL_FEATURES
import com.byagowi.persiancalendar.PREF_EASTERN_GREGORIAN_ARABIC_MONTHS
import com.byagowi.persiancalendar.PREF_HOLIDAY_TYPES
import com.byagowi.persiancalendar.PREF_ISLAMIC_OFFSET
import com.byagowi.persiancalendar.PREF_PERSIAN_DIGITS
import com.byagowi.persiancalendar.PREF_SHOW_DEVICE_CALENDAR_EVENTS
import com.byagowi.persiancalendar.PREF_SHOW_WEEK_OF_YEAR_NUMBER
import com.byagowi.persiancalendar.PREF_THEME
import com.byagowi.persiancalendar.PREF_WEEK_ENDS
import com.byagowi.persiancalendar.PREF_WEEK_START
import com.byagowi.persiancalendar.PREF_WIDGET_IN_24
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.ui.preferences.PREF_DESTINATION
import com.byagowi.persiancalendar.ui.preferences.build
import com.byagowi.persiancalendar.ui.preferences.clickable
import com.byagowi.persiancalendar.ui.preferences.dialogTitle
import com.byagowi.persiancalendar.ui.preferences.interfacecalendar.calendarsorder.showCalendarPreferenceDialog
import com.byagowi.persiancalendar.ui.preferences.multiSelect
import com.byagowi.persiancalendar.ui.preferences.section
import com.byagowi.persiancalendar.ui.preferences.singleSelect
import com.byagowi.persiancalendar.ui.preferences.summary
import com.byagowi.persiancalendar.ui.preferences.switch
import com.byagowi.persiancalendar.ui.preferences.title
import com.byagowi.persiancalendar.ui.utils.askForCalendarPermission
import com.byagowi.persiancalendar.utils.Theme
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.formatNumber
import com.byagowi.persiancalendar.utils.isIslamicOffsetExpired
import com.byagowi.persiancalendar.utils.language
import com.byagowi.persiancalendar.utils.weekDays

class InterfaceCalendarFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val context = context ?: return
        val destination = arguments?.getString(PREF_DESTINATION)
        if (destination == PREF_HOLIDAY_TYPES) showHolidaysTypesDialog(context)

        preferenceScreen = preferenceManager.createPreferenceScreen(context).build {
            section(R.string.pref_interface) {
                clickable(onClick = { showLanguagePreferenceDialog(context) }) {
                    if (destination == PREF_APP_LANGUAGE) title = "Language"
                    else title(R.string.language)
                }
                singleSelect(
                    PREF_THEME,
                    Theme.values().map { getString(it.title) },
                    Theme.values().map { it.key },
                    Theme.SYSTEM_DEFAULT.key
                ) {
                    title(R.string.select_skin)
                    dialogTitle(R.string.select_skin)
                    summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
                }
                switch(PREF_EASTERN_GREGORIAN_ARABIC_MONTHS, false) {
                    if (language.isArabic) {
                        title = "السنة الميلادية بالاسماء الشرقية"
                        summary = "كانون الثاني، شباط، آذار، …"
                    } else isVisible = false
                }
                switch(PREF_PERSIAN_DIGITS, true) {
                    title(R.string.persian_digits)
                    summary(R.string.enable_persian_digits)
                    // Don't even show the option as the option is pointless for non Arabic script locales
                    if (!language.isArabicScript) isVisible = false
                }
            }
            section(R.string.calendar) {
                // Mark the rest of options as advanced
                initialExpandedChildrenCount = 6
                clickable(onClick = { showHolidaysTypesDialog(context) }) {
                    title(R.string.events)
                    summary(R.string.events_summary)
                }
                switch(PREF_SHOW_DEVICE_CALENDAR_EVENTS, false) {
                    title(R.string.show_device_calendar_events)
                    summary(R.string.show_device_calendar_events_summary)
                    setOnPreferenceChangeListener { _, _ ->
                        val activity = activity ?: return@setOnPreferenceChangeListener false
                        isChecked = if (ActivityCompat.checkSelfPermission(
                                activity, Manifest.permission.READ_CALENDAR
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            activity.askForCalendarPermission()
                            false
                        } else {
                            !isChecked
                        }
                        false
                    }
                }
                clickable(onClick = {
                    showCalendarPreferenceDialog(context, onEmpty = {
                        // Easter egg when empty result is rejected
                        val view = view?.rootView ?: return@showCalendarPreferenceDialog
                        ValueAnimator.ofFloat(0f, 360f).also {
                            it.duration = 3000L
                            it.interpolator = AccelerateDecelerateInterpolator()
                            it.addUpdateListener { value ->
                                view.rotation = value.animatedValue as Float
                            }
                        }.start()
                    })
                }) {
                    title(R.string.calendars_priority)
                    summary(R.string.calendars_priority_summary)
                }
                switch(PREF_ASTRONOMICAL_FEATURES, false) {
                    title(R.string.astronomical_info)
                    summary(R.string.astronomical_info_summary)
                }
                switch(PREF_SHOW_WEEK_OF_YEAR_NUMBER, false) {
                    title(R.string.week_of_year)
                    summary(R.string.week_of_year_summary)
                }
                switch(PREF_WIDGET_IN_24, true) {
                    title(R.string.clock_in_24)
                    summary(R.string.showing_clock_in_24)
                }
                run { // reset Islamic offset if is already expired
                    val appPrefs = context.appPrefs
                    if (PREF_ISLAMIC_OFFSET in appPrefs && isIslamicOffsetExpired(appPrefs))
                        appPrefs.edit { putString(PREF_ISLAMIC_OFFSET, DEFAULT_ISLAMIC_OFFSET) }
                }
                singleSelect(
                    PREF_ISLAMIC_OFFSET,
                    // One is formatted with locale's numerals and the other used for keys isn't
                    (-2..2).map { formatNumber(it.toString()) }, (-2..2).map { it.toString() },
                    DEFAULT_ISLAMIC_OFFSET
                ) {
                    title(R.string.islamic_offset)
                    summary(R.string.islamic_offset_summary)
                    dialogTitle(R.string.islamic_offset)
                }
                val weekDaysValues = (0..6).map { it.toString() }
                singleSelect(PREF_WEEK_START, weekDays, weekDaysValues, DEFAULT_WEEK_START) {
                    title(R.string.week_start)
                    dialogTitle(R.string.week_start_summary)
                    summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
                }
                multiSelect(PREF_WEEK_ENDS, weekDays, weekDaysValues, DEFAULT_WEEK_ENDS) {
                    title(R.string.week_ends)
                    summary(R.string.week_ends_summary)
                    dialogTitle(R.string.week_ends_summary)
                }
            }
        }
    }
}
