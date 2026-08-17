package com.reflex.tr.game.ibrh.ui.game

import android.content.Context
import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

/**
 * The app picks its language in-app rather than following the system locale, so strings resolve
 * against a context built for the selected [AppLanguage] instead of the ambient one.
 */

/** Returns a [Context] whose resources resolve against the in-app [language]. */
internal fun Context.localizedContext(language: AppLanguage): Context {
    val locale = Locale.forLanguageTag(language.code)
    val configuration = Configuration(resources.configuration)
    configuration.setLocale(locale)
    return createConfigurationContext(configuration)
}

/** [androidx.compose.ui.res.stringResource] equivalent that honours the in-app [selectedLanguage]. */
@Composable
internal fun localizedStringResource(
    @StringRes id: Int,
    selectedLanguage: AppLanguage,
    vararg args: Any
): String {
    val context = LocalContext.current
    val localized = remember(context, selectedLanguage) { context.localizedContext(selectedLanguage) }
    return if (args.isEmpty()) {
        localized.getString(id)
    } else {
        localized.getString(id, *args)
    }
}
