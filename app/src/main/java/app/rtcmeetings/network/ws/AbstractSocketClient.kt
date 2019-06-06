package app.rtcmeetings.network.ws

import io.socket.client.IO
import io.socket.client.Socket
import io.socket.engineio.client.transports.WebSocket

abstract class AbstractSocketClient(protected var host: String) {
    protected var opts: IO.Options = IO.Options().apply {
        reconnection = true
        reconnectionDelay = 1000
        reconnectionDelayMax = 1000
        reconnectionAttempts = 10
        forceNew = true
        transports = arrayOf(WebSocket.NAME)
    }

    abstract fun connect()

    abstract fun close()

    abstract fun reconnect()

    abstract fun getSocket(): Socket?
}
