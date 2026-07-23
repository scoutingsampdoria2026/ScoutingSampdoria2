package com.scoutingsampdoria.persone2.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        PersonaEntity::class,
        CampoCustomEntity::class,
        ConvocazioneEntity::class,
        ConvocazioneGiocatoreEntity::class,
        AdminLogEntity::class,
    ],
    version = 1,
    exportSchema = false
)
abstract class ScoutingDatabase : RoomDatabase() {
    abstract fun personaDao(): PersonaDao
    abstract fun campoCustomDao(): CampoCustomDao
    abstract fun convocazioneDao(): ConvocazioneDao
    abstract fun convocazioneGiocatoreDao(): ConvocazioneGiocatoreDao
    abstract fun adminLogDao(): AdminLogDao

    companion object {
        const val NOME_FILE = "scouting.db"

        @Volatile
        private var INSTANCE: ScoutingDatabase? = null

        fun get(context: Context): ScoutingDatabase {
            return INSTANCE ?: synchronized(this) {
                val db = Room.databaseBuilder(
                    context.applicationContext,
                    ScoutingDatabase::class.java,
                    NOME_FILE
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Popolo i campi custom predefiniti alla creazione del DB
                            db.execSQL("INSERT OR IGNORE INTO campi_custom (nome) VALUES ('CATEGORIA')")
                            db.execSQL("INSERT OR IGNORE INTO campi_custom (nome) VALUES ('RATING')")
                        }
                    })
                    .build()
                INSTANCE = db
                db
            }
        }

        /** Utile per il ripristino da backup: forza la ricreazione dell'istanza
         *  dopo aver sostituito il file su disco. */
        fun invalida() {
            synchronized(this) {
                INSTANCE?.close()
                INSTANCE = null
            }
        }

        fun percorsoFile(context: Context): String {
            return context.getDatabasePath(NOME_FILE).absolutePath
        }
    }
}
