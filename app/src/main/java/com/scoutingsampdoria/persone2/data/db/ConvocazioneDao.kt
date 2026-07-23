package com.scoutingsampdoria.persone2.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface ConvocazioneDao {

    @Query("SELECT * FROM convocazioni ORDER BY data DESC, ora DESC, id DESC")
    suspend fun listaTutte(): List<ConvocazioneEntity>

    @Query("SELECT * FROM convocazioni WHERE categoria = :categoria ORDER BY data DESC, ora DESC, id DESC")
    suspend fun listaPerCategoria(categoria: String): List<ConvocazioneEntity>

    @Query("SELECT * FROM convocazioni WHERE id = :id")
    suspend fun byId(id: Int): ConvocazioneEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserisci(conv: ConvocazioneEntity): Long

    @Update
    suspend fun aggiorna(conv: ConvocazioneEntity)

    @Query("DELETE FROM convocazioni WHERE id = :id")
    suspend fun eliminaById(id: Int)

    @Query("DELETE FROM convocazioni")
    suspend fun eliminaTutte()
}
