package com.scoutingsampdoria.persone2.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface ConvocazioneGiocatoreDao {

    /** Ritorna la lista dei giocatori (o caselle vuote) per una convocazione,
     *  arricchita con cognome/nome/ruolo del giocatore. */
    @Query("""
        SELECT cg.id AS id, cg.convocazione_id AS convocazione_id,
               cg.persona_id AS persona_id, cg.numero AS numero,
               cg.ordine AS ordine, cg.posizione_campo AS posizione_campo,
               p.cognome AS cognome, p.nome AS nome, p.ruolo AS ruolo
        FROM convocazione_giocatori cg
        LEFT JOIN persone p ON cg.persona_id = p.id
        WHERE cg.convocazione_id = :convocazioneId
        ORDER BY cg.ordine ASC
    """)
    suspend fun perConvocazione(convocazioneId: Int): List<ConvocazioneGiocatoreArricchito>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserisci(g: ConvocazioneGiocatoreEntity): Long

    @Query("DELETE FROM convocazione_giocatori WHERE convocazione_id = :convocazioneId")
    suspend fun eliminaPerConvocazione(convocazioneId: Int)

    @Transaction
    suspend fun sostituisciTutti(convocazioneId: Int, nuovi: List<ConvocazioneGiocatoreEntity>) {
        eliminaPerConvocazione(convocazioneId)
        nuovi.forEach { inserisci(it) }
    }
}

/** Proiezione arricchita (JOIN con persone) usata solo in lettura. */
data class ConvocazioneGiocatoreArricchito(
    val id: Int,
    @androidx.room.ColumnInfo(name = "convocazione_id") val convocazioneId: Int,
    @androidx.room.ColumnInfo(name = "persona_id") val personaId: Int?,
    val numero: Int?,
    val ordine: Int,
    @androidx.room.ColumnInfo(name = "posizione_campo") val posizioneCampo: Int?,
    val cognome: String?,
    val nome: String?,
    val ruolo: String?,
)
