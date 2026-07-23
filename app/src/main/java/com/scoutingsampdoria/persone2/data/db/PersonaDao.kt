package com.scoutingsampdoria.persone2.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface PersonaDao {

    @Query("SELECT * FROM persone ORDER BY cognome ASC, nome ASC")
    suspend fun listaTutte(): List<PersonaEntity>

    @Query("""
        SELECT * FROM persone
        WHERE (:q IS NULL OR cognome LIKE '%' || :q || '%' OR nome LIKE '%' || :q || '%')
          AND (:regione IS NULL OR regione = :regione)
          AND (:societa IS NULL OR societa = :societa)
          AND (:ruolo IS NULL OR ruolo = :ruolo)
          AND (:quickReport IS NULL OR quick_report = :quickReport)
        ORDER BY cognome ASC, nome ASC
    """)
    suspend fun listaFiltrata(
        q: String?,
        regione: String?,
        societa: String?,
        ruolo: String?,
        quickReport: String?
    ): List<PersonaEntity>

    @Query("SELECT * FROM persone WHERE id = :id")
    suspend fun byId(id: Int): PersonaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserisci(persona: PersonaEntity): Long

    @Update
    suspend fun aggiorna(persona: PersonaEntity)

    @Delete
    suspend fun elimina(persona: PersonaEntity)

    @Query("DELETE FROM persone WHERE id = :id")
    suspend fun eliminaById(id: Int)

    @Query("DELETE FROM persone")
    suspend fun eliminaTutte()

    @Query("SELECT COUNT(*) FROM persone")
    suspend fun conta(): Int

    @Query("SELECT DISTINCT regione FROM persone WHERE regione IS NOT NULL AND regione != '' ORDER BY regione")
    suspend fun valoriRegione(): List<String>

    @Query("SELECT DISTINCT societa FROM persone WHERE societa IS NOT NULL AND societa != '' ORDER BY societa")
    suspend fun valoriSocieta(): List<String>

    @Query("SELECT DISTINCT ruolo FROM persone WHERE ruolo IS NOT NULL AND ruolo != '' ORDER BY ruolo")
    suspend fun valoriRuolo(): List<String>

    @Query("SELECT DISTINCT quick_report FROM persone WHERE quick_report IS NOT NULL AND quick_report != '' ORDER BY quick_report")
    suspend fun valoriQuickReport(): List<String>
}
