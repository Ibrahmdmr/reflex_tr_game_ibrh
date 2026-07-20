package com.reflex.tr.game.ibrh.ui.game

import android.content.Context
import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

@Composable
internal fun localizedHomeStringResource(
    @StringRes id: Int,
    selectedLanguage: AppLanguage,
    vararg args: Any
): String {
    val context = LocalContext.current
    val localizedContext = remember(context, selectedLanguage) {
        context.createHomeLanguageContext(selectedLanguage)
    }
    return if (args.isEmpty()) {
        localizedContext.getString(id)
    } else {
        localizedContext.getString(id, *args)
    }
}

internal fun Context.createHomeLanguageContext(language: AppLanguage): Context {
    val locale = Locale.forLanguageTag(language.code)
    val configuration = Configuration(resources.configuration)
    configuration.setLocale(locale)
    return createConfigurationContext(configuration)
}
