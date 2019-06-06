package app.rtcmeetings.network.ws

class SocketQuery(private val key: String, private val value: String) {

    fun format(): String = this.key + "=" + this.value
}
