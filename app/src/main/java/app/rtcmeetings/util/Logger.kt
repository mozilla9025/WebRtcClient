package app.rtcmeetings.util

import android.util.Log

inline fun <reified T> T.logi(message: String) = Log.i(T::class.java.simpleName, message)

inline fun <reified T> T.logd(message: String) = Log.d(T::class.java.simpleName, message)

inline fun <reified T> T.logw(message: String) = Log.w(T::class.java.simpleName, message)

inline fun <reified T> T.loge(message: String) = Log.e(T::class.java.simpleName, message)

inline fun <reified T> T.loge(message: String, error: Throwable) = Log.e(T::class.java.simpleName, message, error)