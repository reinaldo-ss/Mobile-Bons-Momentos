package com.bonsmomentos.entregador.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PedidoDao {

    /** Tela "Pedidos disponíveis": tudo que ainda não foi entregue. */
    @Query("SELECT * FROM pedidos WHERE status != 'ENTREGUE' ORDER BY id")
    fun observarDisponiveis(): Flow<List<PedidoEntity>>

    @Query("SELECT COUNT(*) FROM pedidos WHERE syncPendente = 1")
    fun observarQtdPendentes(): Flow<Int>

    @Query("SELECT COUNT(*) FROM pedidos")
    suspend fun contar(): Int

    @Query("SELECT * FROM pedidos WHERE syncPendente = 1")
    suspend fun pendentes(): List<PedidoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(lista: List<PedidoEntity>)

    @Query("UPDATE pedidos SET status = :status, syncPendente = 1, atualizadoEm = :ts WHERE id = :id")
    suspend fun atualizarStatusLocal(id: Long, status: String, ts: Long)

    /** Só limpa a flag se o registro não mudou desde o envio (evita perder edição concorrente). */
    @Query("UPDATE pedidos SET syncPendente = 0 WHERE id = :id AND atualizadoEm = :ts")
    suspend fun marcarSincronizada(id: Long, ts: Long)

    @Query("DELETE FROM pedidos WHERE syncPendente = 0 AND id NOT IN (:idsRemotos)")
    suspend fun removerAusentes(idsRemotos: List<Long>)

    @Query("DELETE FROM pedidos")
    suspend fun limparTudo()
}
