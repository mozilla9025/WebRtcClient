package app.rtcmeetings.network

import okhttp3.Interceptor
import okhttp3.Response

class HeaderInterceptor(
        private val token: String
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response = chain.run {
        proceed(
                chain.request()
                        .newBuilder()
                        .addHeader("Authorization", "Bearer $token")
                        .build()
        )
    }
}