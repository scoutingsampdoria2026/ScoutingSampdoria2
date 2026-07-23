package com.scoutingsampdoria.persone2.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "persone")
data class PersonaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val cognome: String,
    val nome: String,
    @ColumnInfo(name = "data_nascita") val dataNascita: String? = null,
    val regione: String? = null,
    val societa: String? = null,
    val ruolo: String? = null,
    val matricola: String? = null,
    @ColumnInfo(name = "quick_report") val quickReport: String? = null,
    /** JSON serializzato dei campi extra (custom). Es. {"CATEGORIA":"UNDER 13","RATING":"4"} */
    val extra: String? = null,
    @ColumnInfo(name = "creato_il", defaultValue = "CURRENT_TIMESTAMP")
    val creatoIl: String? = null,
    @ColumnInfo(name = "aggiornato_il", defaultValue = "CURRENT_TIMESTAMP")
    val aggiornatoIl: String? = null,
)
