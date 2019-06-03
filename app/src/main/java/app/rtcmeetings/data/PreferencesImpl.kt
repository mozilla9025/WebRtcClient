package app.rtcmeetings.data

import android.content.SharedPreferences
import io.reactivex.Completable
import io.reactivex.Single

class PreferencesImpl(private val sp: SharedPreferences) : AuthStorage {
    override fun hasToken(): Single<Boolean> {
        return Single.just(sp.contains(TOKEN_KEY) && !sp.getString(TOKEN_KEY, "").isNullOrEmpty())
    }

    override fun getToken(): Single<String> {
        return Single.just(sp.getString(TOKEN_KEY, ""))
    }

    override fun getRawToken(): String {
        return sp.getString(TOKEN_KEY, "")!!
    }

    override fun setToken(token: String): Completable {
        return Completable.fromAction {
            sp.edit().remove(TOKEN_KEY)
                .putString(TOKEN_KEY, token)
                .apply()
        }
    }

    companion object {
        const val PREFERENCES = "webrtc-meetings-preferences"
        private const val TOKEN_KEY = "access_token"
    }
}