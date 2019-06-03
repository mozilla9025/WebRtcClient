package app.rtcmeetings.network

import app.rtcmeetings.data.AuthStorage

class AccessTokenManager(private val authStorage: AuthStorage) {
    fun getAccessToken(): String =
        authStorage.getRawToken()
}