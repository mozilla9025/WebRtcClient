package app.rtcmeetings.network.ws

import io.socket.client.IO
import io.socket.client.Socket
import java.net.URISyntaxException

class WsClient(
    host: String,
    query: SocketQuery
) : AbstractSocketClient(host) {

    init {
        this.opts.query = query.format()
    }

    override fun connect() {
        socket?.let {
            reconnect()
        } ?: try {
            socket = IO.socket(host, opts)
            socket!!.connect()
        } catch (e: URISyntaxException) {
            throw RuntimeException(e)
        }
    }

    override fun reconnect() {
        socket = socket!!.connect()
    }

    override fun close() {
        socket?.disconnect()
        socket = null
    }

    override fun getSocket(): Socket? {
        return socket
    }

    companion object {
        var socket: Socket? = null
    }
}
