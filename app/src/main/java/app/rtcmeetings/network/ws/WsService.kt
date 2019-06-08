package app.rtcmeetings.network.ws

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import app.rtcmeetings.BuildConfig
import app.rtcmeetings.data.AuthStorage
import app.rtcmeetings.domain.usecase.CheckAuthUseCase
import app.rtcmeetings.util.loge
import app.rtcmeetings.webrtc.CallEvent
import dagger.android.AndroidInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.socket.client.Socket
import io.socket.emitter.Emitter
import javax.inject.Inject

class WsService : Service() {

    @Inject
    lateinit var checkAuthUseCase: CheckAuthUseCase
    @Inject
    lateinit var authStorage: AuthStorage

    private val disposables = CompositeDisposable()
    private val binder = LocalBinder()

    private var wsClient: WsClient? = null
    private var socket: Socket? = null

    inner class LocalBinder : Binder() {
        internal val service: WsService
            get() = this@WsService
    }

    private val handler = Handler()

    private val connectionRunnable = Runnable {
        if (!socket?.connected()!!) {
            closeConnection()
            createConnection()
        }
        checkConnection()
    }

    private val onCall = Emitter.Listener {
        runCatching {
            CallEvent.onCall(this@WsService, it[0].toString())
        }.onFailure { loge(it) }
    }

    private val onAccept = Emitter.Listener {
        runCatching {
            CallEvent.onAccept(this@WsService, it[0].toString())
        }.onFailure { loge(it) }
    }

    private val onDecline = Emitter.Listener {
        runCatching {
            CallEvent.onDecline(this@WsService, it[0].toString())
        }.onFailure { loge(it) }
    }

    private val onCancel = Emitter.Listener {
        runCatching {
            CallEvent.onCancel(this@WsService, it[0].toString())
        }.onFailure { loge(it) }
    }

    private val onMiss = Emitter.Listener {
        runCatching {
            CallEvent.onMiss(this@WsService, it[0].toString())
        }.onFailure { loge(it) }
    }

    private val onFinish = Emitter.Listener {
        runCatching {
            CallEvent.onFinish(this@WsService, it[0].toString())
        }.onFailure { loge(it) }
    }

    private val onIce = Emitter.Listener {
        runCatching {
            CallEvent.onIce(this@WsService, it[0].toString())
        }.onFailure { loge(it) }
    }

    private val onVideoToggle = Emitter.Listener {
        runCatching {
            CallEvent.onVideoToggle(this@WsService, it[0].toString())
        }.onFailure { loge(it) }
    }

    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        AndroidInjection.inject(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CONNECT -> connect()
            ACTION_DISCONNECT -> disconnect()
        }

        return START_STICKY
    }

    private fun connect() {
        disposables.add(
            checkAuthUseCase.execute()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    when (it) {
                        true -> createConnection()
                        else -> closeConnection()
                    }
                }, {
                    loge(it)
                    closeConnection()
                })
        )
        checkConnection()
    }

    private fun disconnect() {
        handler.removeCallbacks(connectionRunnable)
        closeConnection()
    }

    private fun createConnection() {
        wsClient = WsClient(
            BuildConfig.WS_URL,
            SocketQuery("token", authStorage.getRawToken())
        )

        wsClient!!.connect()
        wsClient!!.getSocket()?.let {
            socket = it
        }
        socket!!.run {
            on(WsEvent.NEW_CALL, onCall)
            on(WsEvent.ACCEPT, onAccept)
            on(WsEvent.DECLINE, onDecline)
            on(WsEvent.CANCEL, onCancel)
            on(WsEvent.MISS, onMiss)
            on(WsEvent.FINISH, onFinish)
            on(WsEvent.ICE_EXCHANGE, onIce)
            on(WsEvent.VIDEO_TOGGLE, onVideoToggle)
        }
    }

    private fun closeConnection() {
        socket?.run {
            off(WsEvent.NEW_CALL, onCall)
            off(WsEvent.ACCEPT, onAccept)
            off(WsEvent.DECLINE, onDecline)
            off(WsEvent.CANCEL, onCancel)
            off(WsEvent.MISS, onMiss)
            off(WsEvent.FINISH, onFinish)
            off(WsEvent.ICE_EXCHANGE, onIce)
            off(WsEvent.VIDEO_TOGGLE, onVideoToggle)
        }
        wsClient?.close()
        wsClient = null
    }

    private fun checkConnection() {
        handler.postDelayed(connectionRunnable, 5000L)
    }

    companion object {
        fun connect(context: Context) {
            val intent = Intent(context, WsService::class.java).apply {
                action = ACTION_CONNECT
            }
            context.startService(intent)
        }

        fun disconnect(context: Context) {
            val intent = Intent(context, WsService::class.java).apply {
                action = ACTION_DISCONNECT
            }
            context.startService(intent)
        }

        fun emit(context: Context, json: String) {
            val intent = Intent(context, WsService::class.java).apply {
                action = ACTION_EMIT
                putExtra(EXTRA_JSON, json)
            }
            context.startService(intent)
        }

        const val ACTION_CONNECT = "action_connect_ws"
        const val ACTION_DISCONNECT = "action_disconnect_ws"
        const val ACTION_EMIT = "action_emit"

        const val EXTRA_JSON = "extra_json"
    }
}