package com.byagowi.persiancalendar.ui.settings.interfacecalendar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.byagowi.persiancalendar.AFGHANISTAN_TIMEZONE_ID
import com.byagowi.persiancalendar.IRAN_TIMEZONE_ID
import com.byagowi.persiancalendar.R
import com.byagowi.persiancalendar.entities.Language
import com.byagowi.persiancalendar.global.language
import com.byagowi.persiancalendar.ui.utils.showComposeDialog
import com.byagowi.persiancalendar.utils.appPrefs
import com.byagowi.persiancalendar.utils.saveLanguage
import com.google.accompanist.themeadapter.material3.Mdc3Theme
import java.util.TimeZone

fun showLanguagePreferenceDialog(activity: FragmentActivity) =
    showComposeDialog(activity) { LanguagePreferenceDialog(it) }

@Composable
private fun LanguagePreferenceDialog(closeDialog: () -> Unit) {
    AlertDialog(
        onDismissRequest = { closeDialog() },
        title = { Text(stringResource(R.string.language)) },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = { closeDialog() }) { Text(stringResource(R.string.cancel)) }
        },
        text = {
            val currentLanguage = language
            val languages = Language.entries.let { languages ->
                if (TimeZone.getDefault().id in listOf(IRAN_TIMEZONE_ID, AFGHANISTAN_TIMEZONE_ID))
                    languages else languages.sortedBy { it.code }
            }.let { languages ->
                // Put the current language on top as one might don't know more exist above the current selection
                listOf(currentLanguage) + languages.filter { it != currentLanguage }
            }

            val context = LocalContext.current
            fun onClick(item: Language) {
                if (item != currentLanguage) context.appPrefs.saveLanguage(item)
                closeDialog()
            }
            LazyColumn {
                items(languages) { item ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .clickable { onClick(item) }
                    ) {
                        RadioButton(selected = item == currentLanguage, onClick = { onClick(item) })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(item.nativeName)
                    }
                }
            }
        }
    )
}

@Preview
@Composable
private fun LanguagePreferenceDialogPreview() = Mdc3Theme { LanguagePreferenceDialog {} }
