package com.scoutingsampdoria.persone2.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "convocazione_giocatori",
    foreignKeys = [
        ForeignKey(
            entity = ConvocazioneEntity::class,
            parentColumns = ["id"],
            childColumns = ["convocazione_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PersonaEntity::class,
            parentColumns = ["id"],
            childColumns = ["persona_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("convocazione_id"),
        Index("persona_id")
    ]
)
data class ConvocazioneGiocatoreEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "convocazione_id") val convocazioneId: Int,
    @ColumnInfo(name = "persona_id") val personaId: Int? = null,
    val numero: Int? = null,
    val ordine: Int,
    @ColumnInfo(name = "posizione_campo") val posizioneCampo: Int? = null,
)
