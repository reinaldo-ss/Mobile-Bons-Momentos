package com.bonsmomentos.entregador.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bonsmomentos.entregador.App
import com.bonsmomentos.entregador.databinding.ActivityLoginBinding
import com.bonsmomentos.entregador.sync.SyncScheduler

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if ((application as App).session.estaLogado()) {
            irParaPrincipal(); return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnEntrar.setOnClickListener {
            viewModel.entrar(binding.etCpf.text.toString(), binding.etSenha.text.toString())
        }

        viewModel.estado.observe(this) { estado ->
            binding.btnEntrar.isEnabled = estado !is LoginState.Carregando
            binding.btnEntrar.text = if (estado is LoginState.Carregando) "Entrando..." else "Entrar"
            binding.tvErro.visibility =
                if (estado is LoginState.Erro) View.VISIBLE else View.GONE

            when (estado) {
                is LoginState.Erro -> binding.tvErro.text = estado.mensagem
                LoginState.Sucesso -> {
                    SyncScheduler.agendarPeriodico(this)
                    SyncScheduler.sincronizarAgora(this)
                    irParaPrincipal()
                }
                else -> Unit
            }
        }
    }

    private fun irParaPrincipal() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}