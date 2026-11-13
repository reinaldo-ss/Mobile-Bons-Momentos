package com.bonsmomentos.entregador.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bonsmomentos.entregador.App
import com.bonsmomentos.entregador.data.SyncResult

class SyncWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        val repo = (applicationContext as App).repository
        return when (repo.sincronizar()) {
            SyncResult.SUCESSO -> Result.success()
            SyncResult.FALHA_REDE -> Result.retry()      // tenta de novo com backoff
            SyncResult.NAO_AUTORIZADO -> Result.failure()
        }
    }
}
