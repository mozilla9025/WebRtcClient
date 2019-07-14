package app.rtcmeetings.util

fun String.replaceExtraSpaces(): String {
    return this.trim { it <= ' ' }.trim { it <= ' ' }.replace(" +".toRegex(), " ")
}