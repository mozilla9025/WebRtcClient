package app.rtcmeetings.helper.locale

import app.rtcmeetings.R


enum class ApplicationLanguage(val languageCode: String, val languageRes: Int, private val isDefault: Boolean = false) {

    EN("en", R.string.application_language_english),
    CN("cn", R.string.application_language_russian, true);


    companion object {

        val languagesStringValues: Array<Int?>
            get() {
                val languages = arrayOfNulls<Int>(ApplicationLanguage.values().size)

                for (i in languages.indices) {
                    languages[i] = ApplicationLanguage.values()[i].languageRes
                }
                return languages
            }

        fun getLanguageByCode(code: String): ApplicationLanguage? {
            for (language in values()) {
                if (language.languageCode.equals(code, ignoreCase = true)) {
                    return language
                }
            }
            return null
        }

        val defaultLanguage: ApplicationLanguage
            get() {
                for (language in values()) {
                    if (language.isDefault) {
                        return language
                    }
                }
                return ApplicationLanguage.EN
            }
    }
}