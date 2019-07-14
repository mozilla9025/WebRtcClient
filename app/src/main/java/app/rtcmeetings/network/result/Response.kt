package app.rtcmeetings.network.result

class Response<out T> private constructor(
        val status: Status,
        val data: T?,
        val error: Throwable?) {

    companion object {
        fun <T> loading(): Response<T> {
            return Response(Status.LOADING, null, null)
        }

        fun <T> success(data: T?): Response<T> {
            return Response(Status.SUCCESS, data, null)
        }

        fun <T> failure(error: Throwable?): Response<T> {
            return Response(Status.FAILURE, null, error)
        }
    }
}