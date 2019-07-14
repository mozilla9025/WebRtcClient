package app.rtcmeetings.helper.preferences

import android.annotation.SuppressLint
import android.preference.PreferenceManager
import android.text.TextUtils
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import app.rtcmeetings.app.ApplicationLoader

internal object PreferencesHelper {

    @SuppressLint("ApplySharedPref")
    operator fun <T> set(@NonNull key: String, @Nullable value: T?) {
        if (TextUtils.isEmpty(key)) {
            throw NullPointerException("Key must not be null! (key = $key), (value = $value)")
        }
        val edit = PreferenceManager.getDefaultSharedPreferences(ApplicationLoader.get()).edit()
        if (value == null || value is String && (value as String).isEmpty()) {
            clearKey(key)
            return
        }
        if (value is String) {
            edit.putString(key, value as String?)
        } else if (value is Int) {
            edit.putInt(key, (value as Int?)!!)
        } else if (value is Double) {
            edit.putLong(key, java.lang.Double.doubleToRawLongBits((value as Double?)!!))
        } else if (value is Long) {
            edit.putLong(key, (value as Long?)!!)
        } else if (value is Boolean) {
            edit.putBoolean(key, (value as Boolean?)!!)
        } else if (value is Float) {
            edit.putFloat(key, (value as Float?)!!)
        } else {
            edit.putString(key, value.toString())
        }
        edit.commit()
    }

    @Nullable
    fun getString(@NonNull key: String): String? {
        return getString(key, null)
    }

    fun getString(@NonNull key: String, defaultValue: String?): String? {
        return PreferenceManager.getDefaultSharedPreferences(ApplicationLoader.get()).getString(key, defaultValue)
    }

    @JvmOverloads
    fun getBoolean(@NonNull key: String, defaultValue: Boolean? = null): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(ApplicationLoader.get()).getBoolean(key, defaultValue!!)
    }

    @JvmOverloads
    fun getInt(@NonNull key: String, defaultValue: Int = -1): Int {
        return PreferenceManager.getDefaultSharedPreferences(ApplicationLoader.get()).getInt(key, defaultValue)
    }

    @JvmOverloads
    fun getLong(@NonNull key: String, defaultValue: Long = -1): Long {
        return PreferenceManager.getDefaultSharedPreferences(ApplicationLoader.get()).getLong(key, defaultValue)
    }

    @JvmOverloads
    fun getFloat(@NonNull key: String, defaultValue: Float = -1f): Float {
        return PreferenceManager.getDefaultSharedPreferences(ApplicationLoader.get()).getFloat(key, defaultValue)
    }

    fun getDouble(@NonNull key: String, defaultValue: Double?): Double {
        return java.lang.Double.longBitsToDouble(PreferenceManager.getDefaultSharedPreferences(ApplicationLoader.get())
                .getLong(key, java.lang.Double.doubleToLongBits(defaultValue!!)))
    }

    fun clearKey(@NonNull key: String) {
        PreferenceManager.getDefaultSharedPreferences(ApplicationLoader.get()).edit().remove(key).apply()
    }

    fun isExist(@NonNull key: String): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(ApplicationLoader.get()).contains(key)
    }

    fun clearPrefs() {
        PreferenceManager.getDefaultSharedPreferences(ApplicationLoader.get()).edit().clear().apply()
    }
}
