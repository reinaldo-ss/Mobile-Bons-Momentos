package com.bonsmomentos.entregador.data.remote

import com.bonsmomentos.entregador.BuildConfig
import com.bonsmomentos.entregador.data.SessionManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    fun create(session: SessionManager): ApiService {
        val log = HttpLoggingInterceptor().apply {
            redactHeader("Authorization")
            redactHeader("apikey")
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC
            else HttpLoggingInterceptor.Level.NONE
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(log)
            .addInterceptor(Interceptor { chain ->
                val req = chain.request().newBuilder()
                    .header("apikey", BuildConfig.SUPABASE_KEY)
                // Só envia Authorization se for uma chave JWT (começa com "eyJ")
                if (BuildConfig.SUPABASE_KEY.startsWith("eyJ")) {
                    req.header("Authorization", "Bearer ${BuildConfig.SUPABASE_KEY}")
                }
                chain.proceed(req.build())
            })
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}