package app.rtcmeetings.network

import retrofit2.http.POST

interface CallApi {

    @POST("")
    fun startCall()

}