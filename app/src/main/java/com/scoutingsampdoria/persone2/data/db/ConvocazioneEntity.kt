package com.scoutingsampdoria.persone2.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "convocazioni")
data class ConvocazioneEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val categoria: String,
    val data: String? = null,
    val ora: String? = null,
    @ColumnInfo(name = "ora_convocazione") val oraConvocazione: String? = null,
    val impianto: String? = null,
    @ColumnInfo(name = "squadra_casa") val squadraCasa: String? = null,
    @ColumnInfo(name = "squadra_ospite") val squadraOspite: String? = null,
    val modulo: String? = null,
    val note: String? = null,
    @ColumnInfo(name = "creato_il", defaultValue = "CURRENT_TIMESTAMP")
    val creatoIl: String? = null,
    @ColumnInfo(name = "aggiornato_il", defaultValue = "CURRENT_TIMESTAMP")
    val aggiornatoIl: String? = null,
)
