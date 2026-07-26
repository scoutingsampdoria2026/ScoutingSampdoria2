package com.scoutingsampdoria.persone2.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "provini",
    foreignKeys = [
        ForeignKey(
            entity = PersonaEntity::class,
            parentColumns = ["id"],
            childColumns = ["persona_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ConvocazioneEntity::class,
            parentColumns = ["id"],
            childColumns = ["convocazione_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["persona_id"]),
        Index(value = ["convocazione_id"]),
        Index(value = ["persona_id", "convocazione_id"], unique = true),
        Index(value = ["data"])
    ]
)
data class ProvinoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @androidx.room.ColumnInfo(name = "persona_id") val personaId: Int,
    @androidx.room.ColumnInfo(name = "convocazione_id") val convocazioneId: Int,
    val data: String? = null,
    val ora: String? = null,
    val impianto: String? = null,
    val presenza: String? = null,
    val giudizio: Int? = null,
    val note: String? = null,
    @androidx.room.ColumnInfo(name = "creato_il") val creatoIl: String? = null,
    @androidx.room.ColumnInfo(name = "aggiornato_il") val aggiornatoIl: String? = null,
)
