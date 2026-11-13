package com.bonsmomentos.entregador.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bonsmomentos.entregador.App
import kotlinx.coroutines.launch

sealed class LoginState {
    object Ocioso : LoginState()
    object Carregando : LoginState()
    object Sucesso : LoginState()
    data class Erro(val mensagem: String) : LoginState()
}

class LoginViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = (app as App).repository
    val estado = MutableLiveData<LoginState>(LoginState.Ocioso)

    fun entrar(cpf: String, senha: String) {
        val cpfLimpo = cpf.filter { it.isDigit() }
        if (cpfLimpo.length !in 10..11) {
            estado.value = LoginState.Erro("Informe um CPF válido"); return
        }
        if (senha.isEmpty()) {
            estado.value = LoginState.Erro("Informe a senha"); return
        }
        estado.value = LoginState.Carregando
        viewModelScope.launch {
            repo.login(cpfLimpo, senha).fold(
                onSuccess = { estado.value = LoginState.Sucesso },
                onFailure = { estado.value = LoginState.Erro(it.message ?: "Falha no login") }
            )
        }
    }
}