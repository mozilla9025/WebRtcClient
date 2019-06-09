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
import app.rtcmeetings.network.request.CallRequest
import app.rtcmeetings.util.logd
import app.rtcmeetings.util.loge
import app.rtcmeetings.util.logi
import app.rtcmeetings.webrtc.CallEvent
import com.google.gson.Gson
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
    @Inject
    lateinit var gson: Gson

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
            CallEvent.onCall(this@WsService, it[0].toString(), getSocketId()!!)
        }.onFailure { loge(it) }
    }

    private val onAccept = Emitter.Listener {
        runCatching {
            logd(it[0].toString())
            CallEvent.onAccept(this@WsService, it[0].toString())
        }.onFailure { loge(it) }
    }

    private val onDecline = Emitter.Listener {
        runCatching {
            logd(it[0].toString())
            CallEvent.onDecline(this@WsService, it[0].toString())
        }.onFailure { loge(it) }
    }

    private val onCancel = Emitter.Listener {
        runCatching {
            logd(it[0].toString())
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
            logd(it[0].toString())
            CallEvent.onFinish(this@WsService, it[0].toString())
        }.onFailure { loge(it) }
    }

    private val onIce = Emitter.Listener {
        runCatching {
            logd(it[0].toString())
            CallEvent.onIce(this@WsService, it[0].toString())
        }.onFailure { loge(it) }
    }

    private val onVideoToggle = Emitter.Listener {
        runCatching {
            CallEvent.onVideoToggle(this@WsService, it[0].toString())
        }.onFailure { loge(it) }
    }

    fun getSocketId(): String? {
        return socket?.id()
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
            ACTION_EMIT -> emit(intent)
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

    private fun emit(intent: Intent) {
        val extras = intent.getStringExtra(EXTRA_JSON)
        val callRequest = gson.fromJson<CallRequest>(extras, CallRequest::class.java)
        socket?.emit(callRequest.event, gson.toJson(callRequest))
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