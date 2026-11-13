package com.bonsmomentos.entregador.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object SyncScheduler {
    private const val PERIODICO = "sync_periodico"
    private const val IMEDIATO = "sync_imediato"

    private val comRede = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    /** Sincroniza a cada 15 min (mínimo do WorkManager) quando houver internet. */
    fun agendarPeriodico(ctx: Context) {
        val req = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(comRede).build()
        WorkManager.getInstance(ctx)
            .enqueueUniquePeriodicWork(PERIODICO, ExistingPeriodicWorkPolicy.KEEP, req)
    }

    /** Dispara assim que houver rede — se estiver offline, fica na fila até voltar. */
    fun sincronizarAgora(ctx: Context) {
        val req = OneTimeWorkRequestBuilder<SyncWorker>().setConstraints(comRede).build()
        WorkManager.getInstance(ctx)
            .enqueueUniqueWork(IMEDIATO, ExistingWorkPolicy.APPEND_OR_REPLACE, req)
    }

    fun cancelarTudo(ctx: Context) {
        WorkManager.getInstance(ctx).cancelUniqueWork(PERIODICO)
        WorkManager.getInstance(ctx).cancelUniqueWork(IMEDIATO)
    }
}
