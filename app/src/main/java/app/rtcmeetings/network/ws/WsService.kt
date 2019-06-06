package app.rtcmeetings.network.ws

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import app.rtcmeetings.BuildConfig
import app.rtcmeetings.data.AuthStorage
import app.rtcmeetings.domain.usecase.CheckAuthUseCase
import app.rtcmeetings.util.loge
import dagger.android.AndroidInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.socket.client.Socket
import javax.inject.Inject

class WsService : Service() {

    @Inject
    lateinit var checkAuthUseCase: CheckAuthUseCase
    @Inject
    lateinit var authStorage: AuthStorage

    private val disposables = CompositeDisposable()
    private val binder = LocalBinder()

    private lateinit var wsClient: WsClient
    private lateinit var socket: Socket

    inner class LocalBinder : Binder() {
        internal val service: WsService
            get() = this@WsService
    }

    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        AndroidInjection.inject(this)
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
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    private fun createConnection() {
        wsClient = WsClient(
            BuildConfig.WS_URL,
            SocketQuery("token", authStorage.getRawToken())
        )

        wsClient.connect()
        wsClient.getSocket()?.let {
            socket = it
        }
    }

    private fun closeConnection() {

    }
}