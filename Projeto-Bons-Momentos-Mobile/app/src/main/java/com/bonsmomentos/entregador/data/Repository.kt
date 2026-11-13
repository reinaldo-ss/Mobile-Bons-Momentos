package com.bonsmomentos.entregador.data

import com.bonsmomentos.entregador.BuildConfig
import com.bonsmomentos.entregador.data.local.PedidoDao
import com.bonsmomentos.entregador.data.local.PedidoEntity
import com.bonsmomentos.entregador.data.local.StatusPedido
import com.bonsmomentos.entregador.data.remote.ApiService
import com.bonsmomentos.entregador.data.remote.StatusUpdateRequest
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.HttpException
import java.io.IOException

enum class SyncResult { SUCESSO, FALHA_REDE, NAO_AUTORIZADO }

class Repository(
    private val api: ApiService,
    private val dao: PedidoDao,
    private val session: SessionManager
) {
    val pedidos = dao.observarDisponiveis()
    val qtdPendentes = dao.observarQtdPendentes()
    private val mutex = Mutex()

    suspend fun login(cpf: String, senha: String): Result<Unit> {
        if (BuildConfig.MODO_DEMO) {
            session.salvar("token-demo", null, 1L, "Entregador Demo")
            return Result.success(Unit)
        }
        return try {
            val cpfLimpo = cpf.filter { it.isDigit() }
            val motoboys = api.listarMotoboys()

            if (motoboys.isEmpty()) {
                session.limpar()
                return Result.failure(
                    Exception("O banco retornou 0 motoboys. Verifique se a RLS está ativa na tabela motoboy.")
                )
            }

            val peloCpf = motoboys.firstOrNull { m ->
                m.cpf.orEmpty().filter { it.isDigit() } == cpfLimpo
            }

            if (peloCpf == null) {
                session.limpar()
                val listaCpfs = motoboys.joinToString(", ") { it.cpf.orEmpty() }
                return Result.failure(Exception("CPF não encontrado. CPFs no banco: [$listaCpfs]"))
            }

            if (peloCpf.senha.orEmpty().trim() != senha.trim()) {
                session.limpar()
                return Result.failure(Exception("CPF encontrado (${peloCpf.nome}), mas a senha digitada não confere."))
            }

            session.salvar("sessao-${peloCpf.id}", null, peloCpf.id, peloCpf.nome ?: "Entregador")
            Result.success(Unit)
        } catch (e: HttpException) {
            session.limpar()
            val erroBody = e.response()?.errorBody()?.string()?.take(120).orEmpty()
            Result.failure(Exception("Erro HTTP ${e.code()}: $erroBody"))
        } catch (e: IOException) {
            session.limpar()
            Result.failure(Exception("Sem conexão com o servidor"))
        }
    }

    suspend fun marcarEntregue(id: Long) =
        dao.atualizarStatusLocal(id, StatusPedido.ENTREGUE, System.currentTimeMillis())

    suspend fun sincronizar(): SyncResult = mutex.withLock {
        if (BuildConfig.MODO_DEMO) return@withLock sincronizarDemo()
        val idMotoboy = session.id()
        if (idMotoboy <= 0L) return@withLock SyncResult.NAO_AUTORIZADO

        try {
            for (e in dao.pendentes()) {
                val resp = api.atualizarStatusPedido(
                    filtroId = "eq.${e.id}",
                    body = StatusUpdateRequest(status = "Entregue")
                )
                val codigo = resp.code()
                when {
                    resp.isSuccessful -> dao.marcarSincronizada(e.id, e.atualizadoEm)
                    codigo == 401 -> return@withLock SyncResult.NAO_AUTORIZADO
                    codigo in 400..499 && codigo != 408 && codigo != 429 ->
                        dao.marcarSincronizada(e.id, e.atualizadoEm)
                    else -> return@withLock SyncResult.FALHA_REDE
                }
            }

            val todosDoMotoboy = api.buscarPedidosDoMotoboy("eq.$idMotoboy")
            val disponiveis = todosDoMotoboy.filter {
                it.status?.trim()?.equals("Saiu para entrega", ignoreCase = true) == true
            }

            val aindaPendentes = dao.pendentes().map { it.id }.toSet()
            dao.upsertAll(disponiveis.filter { it.id !in aindaPendentes }.map { it.paraEntity() })
            dao.removerAusentes(disponiveis.map { it.id })
            session.registrarSync(System.currentTimeMillis())
            SyncResult.SUCESSO
        } catch (e: HttpException) {
            if (e.code() == 401) SyncResult.NAO_AUTORIZADO else SyncResult.FALHA_REDE
        } catch (e: IOException) {
            SyncResult.FALHA_REDE
        }
    }

    private suspend fun sincronizarDemo(): SyncResult {
        for (e in dao.pendentes()) dao.marcarSincronizada(e.id, e.atualizadoEm)
        if (dao.contar() == 0) dao.upsertAll(pedidosDemo())
        session.registrarSync(System.currentTimeMillis())
        return SyncResult.SUCESSO
    }

    private fun pedidosDemo() = listOf(
        PedidoEntity(1, "Bolo de Chocolate", "PIX", "01001-000",
            "Rua Santos Domingos, Nº 219 - Centro, São Paulo", StatusPedido.PENDENTE),
        PedidoEntity(2, "Torta de Morango", "Dinheiro", "04567-010",
            "Av. Exemplo, Nº 111 - Vila Nova, São Paulo", StatusPedido.PENDENTE),
        PedidoEntity(3, "Caixa de Brigadeiros (12 un.)", "Cartão", "02020-020",
            "Rua das Flores, Nº 45 - Jardim Bonito, São Paulo", StatusPedido.PENDENTE)
    )

    suspend fun logout() {
        session.limpar()
        dao.limparTudo()
    }
}