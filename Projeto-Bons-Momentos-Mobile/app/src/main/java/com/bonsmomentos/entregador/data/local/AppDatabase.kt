package com.bonsmomentos.entregador.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [PedidoEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pedidoDao(): PedidoDao

    companion object {
        @Volatile private var instancia: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext, AppDatabase::class.java, "bonsmomentos.db"
                )
                    // Fase de desenvolvimento: se a estrutura mudar, recria o banco local.
                    // (Antes de publicar, trocar por migrações reais.)
                    .fallbackToDestructiveMigration()
                    .build().also { instancia = it }
            }
    }
}
