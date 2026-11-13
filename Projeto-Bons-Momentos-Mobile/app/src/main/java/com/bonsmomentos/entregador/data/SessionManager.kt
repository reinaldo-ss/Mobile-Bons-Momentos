package com.bonsmomentos.entregador.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SessionManager(context: Context) {

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "sessao_segura",
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun salvar(token: String, refreshToken: String?, id: Long, nome: String) {
        prefs.edit()
            .putString(K_TOKEN, token)
            .putString(K_REFRESH, refreshToken)
            .putLong(K_ID, id)
            .putString(K_NOME, nome)
            .apply()
    }

    fun token(): String? = prefs.getString(K_TOKEN, null)
    fun id(): Long = prefs.getLong(K_ID, 0L)
    fun nome(): String = prefs.getString(K_NOME, "") ?: ""
    fun estaLogado() = id() > 0L

    fun registrarSync(timestamp: Long) = prefs.edit().putLong(K_ULTIMA_SYNC, timestamp).apply()
    fun ultimaSync(): Long = prefs.getLong(K_ULTIMA_SYNC, 0L)

    fun limpar() = prefs.edit().clear().apply()

    private companion object {
        const val K_TOKEN = "token"
        const val K_REFRESH = "refresh_token"
        const val K_ID = "id"
        const val K_NOME = "nome"
        const val K_ULTIMA_SYNC = "ultima_sync"
    }
}