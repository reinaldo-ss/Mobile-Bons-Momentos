package com.bonsmomentos.entregador.ui

import android.content.Intent
import android.os.Bundle
import android.text.format.DateUtils
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.bonsmomentos.entregador.App
import com.bonsmomentos.entregador.R
import com.bonsmomentos.entregador.data.SyncResult
import com.bonsmomentos.entregador.databinding.ActivityMainBinding
import com.bonsmomentos.entregador.sync.SyncScheduler
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val app get() = application as App
    private val repo get() = app.repository

    private var rotuloSync = ""
    private var qtdPendentes = 0

    private val adapter = PedidoAdapter { pedido ->
        lifecycleScope.launch {
            repo.marcarEntregue(pedido.id)                      // salva no aparelho na hora
            SyncScheduler.sincronizarAgora(this@MainActivity)   // envia quando houver rede
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = ContextCompat.getColor(this, R.color.bm_creme)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvOla.text = "Bem-Vindo, " + app.session.nome()
        binding.rvPedidos.layoutManager = LinearLayoutManager(this)
        binding.rvPedidos.adapter = adapter
        binding.swipe.setColorSchemeResources(R.color.bm_terracota)
        binding.swipe.setOnRefreshListener { sincronizar() }
        binding.btnSair.setOnClickListener { sair() }

        // A lista sempre vem do banco local (funciona offline)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    repo.pedidos.collect { lista ->
                        adapter.submitList(lista)
                        binding.tvVazio.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
                launch {
                    repo.qtdPendentes.collect { qtd ->
                        qtdPendentes = qtd
                        atualizarInfo()
                    }
                }
            }
        }
        atualizarRotuloSync()
    }

    override fun onStart() {
        super.onStart()
        sincronizar()
    }

    private fun sincronizar() {
        lifecycleScope.launch {
            binding.swipe.isRefreshing = true
            when (repo.sincronizar()) {
                SyncResult.NAO_AUTORIZADO -> sair()
                SyncResult.FALHA_REDE -> {
                    rotuloSync = "Offline — dados salvos no aparelho"
                    atualizarInfo()
                }
                SyncResult.SUCESSO -> atualizarRotuloSync()
            }
            binding.swipe.isRefreshing = false
        }
    }

    private fun atualizarRotuloSync() {
        val t = app.session.ultimaSync()
        rotuloSync = if (t == 0L) "Ainda não sincronizado"
        else "Sincronizado " + DateUtils.getRelativeTimeSpanString(t)
        atualizarInfo()
    }

    private fun atualizarInfo() {
        val pendentes = if (qtdPendentes > 0) " • $qtdPendentes aguardando envio" else ""
        binding.tvSyncInfo.text = rotuloSync + pendentes
    }

    private fun sair() {
        lifecycleScope.launch {
            SyncScheduler.cancelarTudo(this@MainActivity)
            repo.logout()
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            finish()
        }
    }
}
