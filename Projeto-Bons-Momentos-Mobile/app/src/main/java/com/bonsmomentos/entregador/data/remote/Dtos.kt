package com.bonsmomentos.entregador.data.remote

import com.bonsmomentos.entregador.data.local.PedidoEntity
import com.bonsmomentos.entregador.data.local.StatusPedido

data class MotoboyDto(
    val id: Long = 0,
    val nome: String? = null,
    val cpf: String? = null,
    val senha: String? = null
)

data class ReceitaJoinDto(val nome: String? = null)
data class UsuarioJoinDto(val cep: String? = null)

data class StatusUpdateRequest(val status: String)

data class PedidoTabelaDto(
    val id: Long = 0,
    val id_motoboy: Long? = null,
    val endereco_entrega: String? = null,
    val opcao_pagamento: String? = null,
    val status: String? = null,
    val receitas: ReceitaJoinDto? = null,
    val usuarios: UsuarioJoinDto? = null
) {
    fun paraEntity(): PedidoEntity {
        val end = endereco_entrega.orEmpty()
        val cepExtraido = Regex("[0-9]{5}-?[0-9]{3}").find(end)?.value
            ?: usuarios?.cep.orEmpty()

        return PedidoEntity(
            id = id,
            produto = receitas?.nome.orEmpty(),
            pagamento = opcao_pagamento.orEmpty(),
            cep = cepExtraido,
            endereco = end,
            status = status?.trim()?.uppercase()?.replace(' ', '_') ?: StatusPedido.PENDENTE,
            syncPendente = false,
            atualizadoEm = System.currentTimeMillis()
        )
    }
}