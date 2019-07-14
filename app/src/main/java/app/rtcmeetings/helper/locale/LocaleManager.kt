package app.rtcmeetings.helper.locale

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.LocaleList
import android.text.TextUtils
import app.rtcmeetings.helper.preferences.PreferencesManager
import java.util.*

class LocaleManager {

    companion object {
        fun setLocale(c: Context): Context {
            val language = PreferencesManager.applicationLanguage
            return if (!TextUtils.isEmpty(language)) updateResources(c, language) else c
        }

        fun setNewLocale(c: Context, language: String): Boolean {
            if (language == getLanguage(c)) return false
            persistLanguage(language)
            updateResources(c, language)

            return true
        }

        @SuppressLint("ApplySharedPref")
        private fun persistLanguage(language: String) {
            PreferencesManager.applicationLanguage = language
        }

        private fun updateResources(context: Context, language: String): Context {
            var context = context
            val res = context.resources
            val configuration = res.configuration
            val newLocale = Locale(language)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                configuration.setLocale(newLocale)
                val localeList = LocaleList(newLocale)
                LocaleList.setDefault(localeList)
                configuration.locales = localeList
                context = context.createConfigurationContext(configuration)
                res.updateConfiguration(configuration, res.displayMetrics)
            } else {
                Locale.setDefault(newLocale)
                configuration.setLocale(newLocale)
                context = context.createConfigurationContext(configuration)
                res.updateConfiguration(configuration, res.displayMetrics)
            }

            return context
        }

        fun getLocale(context: Context): Locale {
            val config = context.resources.configuration
            return if (Build.VERSION.SDK_INT >= 24) config.locales.get(0) else config.locale
        }

        fun getLanguage(context: Context): String {
            return getLocale(context).language
        }
    }
}