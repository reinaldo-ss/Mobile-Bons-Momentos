package com.bonsmomentos.entregador.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

object StatusPedido {
    const val PENDENTE = "PENDENTE"
    const val EM_ROTA = "EM_ROTA"
    const val ENTREGUE = "ENTREGUE"
}

@Entity(tableName = "pedidos")
data class PedidoEntity(
    @PrimaryKey val id: Long,
    val produto: String,
    val pagamento: String,
    val cep: String,
    val endereco: String,
    val status: String,
    /** true = alteração feita offline que ainda não foi enviada ao servidor */
    val syncPendente: Boolean = false,
    val atualizadoEm: Long = System.currentTimeMillis()
)
