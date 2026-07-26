package com.scoutingsampdoria.persone2.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * Row-di-comodo per la SELECT con JOIN persone e convocazioni (per la UI).
 * I nomi rispecchiano quelli usati nel modello Provino.
 */
data class ProvinoConJoinRow(
    val id: Int,
    @androidx.room.ColumnInfo(name = "persona_id") val personaId: Int,
    @androidx.room.ColumnInfo(name = "convocazione_id") val convocazioneId: Int,
    val data: String?,
    val ora: String?,
    val impianto: String?,
    val presenza: String?,
    val giudizio: Int?,
    val note: String?,
    @androidx.room.ColumnInfo(name = "creato_il") val creatoIl: String?,
    @androidx.room.ColumnInfo(name = "aggiornato_il") val aggiornatoIl: String?,
    val cognome: String?,
    val nome: String?,
    val ruolo: String?,
    val categoria: String?,
    @androidx.room.ColumnInfo(name = "squadra_casa") val squadraCasa: String?,
    @androidx.room.ColumnInfo(name = "squadra_ospite") val squadraOspite: String?,
)

@Dao
interface ProvinoDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun inserisci(provino: ProvinoEntity): Long

    @Query("SELECT * FROM provini WHERE id = :id LIMIT 1")
    suspend fun byId(id: Int): ProvinoEntity?

    @Query("SELECT COUNT(*) FROM provini WHERE persona_id = :personaId")
    suspend fun contaPerPersona(personaId: Int): Int

    /** Elenco provini di una persona, con JOIN convocazione per data corrente. */
    @Query("""
        SELECT p.id, p.persona_id, p.convocazione_id, p.data, p.ora, p.impianto,
               p.presenza, p.giudizio, p.note, p.creato_il, p.aggiornato_il,
               per.cognome, per.nome, per.ruolo,
               c.categoria, c.squadra_casa, c.squadra_ospite
        FROM provini p
        LEFT JOIN persone per ON p.persona_id = per.id
        LEFT JOIN convocazioni c ON p.convocazione_id = c.id
        WHERE p.persona_id = :personaId
        ORDER BY p.data DESC, p.ora DESC, p.id DESC
    """)
    suspend fun perPersonaConJoin(personaId: Int): List<ProvinoConJoinRow>

    /** Dettaglio provino con join per la UI. */
    @Query("""
        SELECT p.id, p.persona_id, p.convocazione_id, p.data, p.ora, p.impianto,
               p.presenza, p.giudizio, p.note, p.creato_il, p.aggiornato_il,
               per.cognome, per.nome, per.ruolo,
               c.categoria, c.squadra_casa, c.squadra_ospite
        FROM provini p
        LEFT JOIN persone per ON p.persona_id = per.id
        LEFT JOIN convocazioni c ON p.convocazione_id = c.id
        WHERE p.id = :id
        LIMIT 1
    """)
    suspend fun dettaglioConJoin(id: Int): ProvinoConJoinRow?

    /** Elenco completo per dashboard, con filtri opzionali categoria/data. */
    @Query("""
        SELECT p.id, p.persona_id, p.convocazione_id, p.data, p.ora, p.impianto,
               p.presenza, p.giudizio, p.note, p.creato_il, p.aggiornato_il,
               per.cognome, per.nome, per.ruolo,
               c.categoria, c.squadra_casa, c.squadra_ospite
        FROM provini p
        LEFT JOIN persone per ON p.persona_id = per.id
        LEFT JOIN convocazioni c ON p.convocazione_id = c.id
        WHERE (:categoria IS NULL OR c.categoria = :categoria)
          AND (:data IS NULL OR p.data = :data)
        ORDER BY p.data DESC, p.ora DESC, p.id DESC
    """)
    suspend fun dashboardFiltrata(categoria: String?, data: String?): List<ProvinoConJoinRow>

    /** Categorie distinte con almeno un provino. */
    @Query("""
        SELECT DISTINCT c.categoria
        FROM provini p
        LEFT JOIN convocazioni c ON p.convocazione_id = c.id
        WHERE c.categoria IS NOT NULL AND c.categoria != ''
        ORDER BY c.categoria
    """)
    suspend fun categorieDisponibili(): List<String>

    /** Date distinte con almeno un provino, filtrabili per categoria. */
    @Query("""
        SELECT DISTINCT p.data
        FROM provini p
        LEFT JOIN convocazioni c ON p.convocazione_id = c.id
        WHERE p.data IS NOT NULL AND p.data != ''
          AND (:categoria IS NULL OR c.categoria = :categoria)
        ORDER BY p.data DESC
    """)
    suspend fun dateDisponibili(categoria: String?): List<String>

    // === STATISTICHE ===

    /** Un provino conta come "compilato" se ALMENO UNO tra presenza, giudizio, note è valorizzato. */
    @Query("""
        SELECT COUNT(*) FROM provini
        WHERE (presenza IS NOT NULL AND presenza != '')
           OR giudizio IS NOT NULL
           OR (note IS NOT NULL AND note != '')
    """)
    suspend fun contaCompilati(): Int

    @Query("""
        SELECT COUNT(DISTINCT persona_id) FROM provini
        WHERE (presenza IS NOT NULL AND presenza != '')
           OR giudizio IS NOT NULL
           OR (note IS NOT NULL AND note != '')
    """)
    suspend fun contaGiocatoriConProvino(): Int

    @Query("UPDATE provini SET presenza = :presenza, giudizio = :giudizio, note = :note, aggiornato_il = :aggiornatoIl WHERE id = :id")
    suspend fun aggiornaCampi(id: Int, presenza: String?, giudizio: Int?, note: String?, aggiornatoIl: String)

    @Query("DELETE FROM provini WHERE id = :id")
    suspend fun eliminaById(id: Int)

    /** Elimina i provini per una convocazione dove tutti i campi editabili sono vuoti. */
    @Query("""
        DELETE FROM provini
        WHERE convocazione_id = :convocazioneId
          AND persona_id = :personaId
          AND (presenza IS NULL OR presenza = '')
          AND giudizio IS NULL
          AND (note IS NULL OR note = '')
    """)
    suspend fun eliminaSeVuoto(convocazioneId: Int, personaId: Int)

    /** Verifica se esiste già un provino per la coppia. */
    @Query("SELECT COUNT(*) FROM provini WHERE convocazione_id = :convocazioneId AND persona_id = :personaId")
    suspend fun esiste(convocazioneId: Int, personaId: Int): Int
}
