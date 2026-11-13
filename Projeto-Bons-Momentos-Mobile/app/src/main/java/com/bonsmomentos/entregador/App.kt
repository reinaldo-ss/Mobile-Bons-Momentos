package com.bonsmomentos.entregador

import android.app.Application
import com.bonsmomentos.entregador.data.Repository
import com.bonsmomentos.entregador.data.SessionManager
import com.bonsmomentos.entregador.data.local.AppDatabase
import com.bonsmomentos.entregador.data.remote.ApiClient
import com.bonsmomentos.entregador.sync.SyncScheduler

/** Container simples de dependências (singletons). */
class App : Application() {
    val session by lazy { SessionManager(this) }
    val database by lazy { AppDatabase.get(this) }
    val api by lazy { ApiClient.create(session) }
    val repository by lazy { Repository(api, database.pedidoDao(), session) }

    override fun onCreate() {
        super.onCreate()
        if (session.estaLogado()) SyncScheduler.agendarPeriodico(this)
    }
}
