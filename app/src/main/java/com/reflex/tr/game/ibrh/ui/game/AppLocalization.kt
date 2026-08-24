package com.reflex.tr.game.ibrh.ui.game

import android.content.Context
import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

/** The app picks its language in-app, so strings resolve against this, not the system locale. */

internal fun Context.localizedContext(language: AppLanguage): Context {
    val locale = Locale.forLanguageTag(language.code)
    val configuration = Configuration(resources.configuration)
    configuration.setLocale(locale)
    return createConfigurationContext(configuration)
}

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
