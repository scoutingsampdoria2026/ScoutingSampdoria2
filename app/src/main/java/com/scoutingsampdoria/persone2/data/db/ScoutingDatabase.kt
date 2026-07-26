package com.scoutingsampdoria.persone2.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.json.JSONObject

@Database(
    entities = [
        PersonaEntity::class,
        CampoCustomEntity::class,
        ConvocazioneEntity::class,
        ConvocazioneGiocatoreEntity::class,
        AdminLogEntity::class,
        ProvinoEntity::class,
    ],
    version = 2,
    exportSchema = false
)
abstract class ScoutingDatabase : RoomDatabase() {
    abstract fun personaDao(): PersonaDao
    abstract fun campoCustomDao(): CampoCustomDao
    abstract fun convocazioneDao(): ConvocazioneDao
    abstract fun convocazioneGiocatoreDao(): ConvocazioneGiocatoreDao
    abstract fun adminLogDao(): AdminLogDao
    abstract fun provinoDao(): ProvinoDao

    companion object {
        const val NOME_FILE = "scouting.db"

        @Volatile
        private var INSTANCE: ScoutingDatabase? = null

        /**
         * Migration da v1 a v2:
         *  - Aggiunge il campo custom predefinito STATO
         *  - Assegna STATO = "Segnalato" a tutti i giocatori esistenti che non ce l'hanno
         *  - Crea la tabella provini con relativi indici
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Aggiungo STATO ai campi custom
                db.execSQL("INSERT OR IGNORE INTO campi_custom (nome) VALUES ('STATO')")

                // 2. Aggiorno tutti i giocatori esistenti: se non hanno STATO nell'extra, lo aggiungo
                val cursor = db.query("SELECT id, extra FROM persone")
                val aggiornamenti = mutableListOf<Pair<Int, String>>()
                while (cursor.moveToNext()) {
                    val id = cursor.getInt(0)
                    val extraJson = if (cursor.isNull(1)) null else cursor.getString(1)
                    val extra = if (extraJson.isNullOrBlank()) JSONObject()
                                else try { JSONObject(extraJson) } catch (_: Exception) { JSONObject() }
                    val statoAttuale = extra.optString("STATO", "")
                    if (statoAttuale.isBlank()) {
                        extra.put("STATO", "Segnalato")
                        aggiornamenti.add(id to extra.toString())
                    }
                }
                cursor.close()
                aggiornamenti.forEach { (id, nuovoExtra) ->
                    val stmt = db.compileStatement("UPDATE persone SET extra = ? WHERE id = ?")
                    stmt.bindString(1, nuovoExtra)
                    stmt.bindLong(2, id.toLong())
                    stmt.executeUpdateDelete()
                }

                // 3. Creo la tabella provini
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS provini (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        persona_id INTEGER NOT NULL,
                        convocazione_id INTEGER NOT NULL,
                        data TEXT,
                        ora TEXT,
                        impianto TEXT,
                        presenza TEXT,
                        giudizio INTEGER,
                        note TEXT,
                        creato_il TEXT,
                        aggiornato_il TEXT,
                        FOREIGN KEY (persona_id) REFERENCES persone(id) ON DELETE CASCADE,
                        FOREIGN KEY (convocazione_id) REFERENCES convocazioni(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_provini_persona_id ON provini(persona_id)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_provini_convocazione_id ON provini(convocazione_id)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_provini_persona_id_convocazione_id ON provini(persona_id, convocazione_id)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_provini_data ON provini(data)")
            }
        }

        fun get(context: Context): ScoutingDatabase {
            return INSTANCE ?: synchronized(this) {
                val db = Room.databaseBuilder(
                    context.applicationContext,
                    ScoutingDatabase::class.java,
                    NOME_FILE
                )
                    .addMigrations(MIGRATION_1_2)
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Popolo i campi custom predefiniti alla creazione del DB
                            db.execSQL("INSERT OR IGNORE INTO campi_custom (nome) VALUES ('CATEGORIA')")
                            db.execSQL("INSERT OR IGNORE INTO campi_custom (nome) VALUES ('RATING')")
                            db.execSQL("INSERT OR IGNORE INTO campi_custom (nome) VALUES ('STATO')")
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
