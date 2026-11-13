package com.bonsmomentos.entregador.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Query

interface ApiService {

    @GET("rest/v1/motoboy?select=id,nome,cpf,senha")
    suspend fun listarMotoboys(): List<MotoboyDto>

    @GET("rest/v1/pedidos?select=id,id_motoboy,endereco_entrega,opcao_pagamento,status,receitas(nome),usuarios(cep)")
    suspend fun buscarPedidosDoMotoboy(
        @Query("id_motoboy") filtroMotoboy: String
    ): List<PedidoTabelaDto>

    @PATCH("rest/v1/pedidos")
    suspend fun atualizarStatusPedido(
        @Query("id") filtroId: String,
        @Body body: StatusUpdateRequest
    ): Response<Unit>
}