package com.scoutingsampdoria.persone2.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CampoCustomDao {
    @Query("SELECT * FROM campi_custom ORDER BY nome ASC")
    suspend fun listaTutti(): List<CampoCustomEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun inserisci(campo: CampoCustomEntity): Long

    @Query("DELETE FROM campi_custom WHERE nome = :nome AND nome NOT IN ('CATEGORIA', 'RATING')")
    suspend fun eliminaByNome(nome: String)

    @Query("SELECT * FROM campi_custom WHERE nome = :nome LIMIT 1")
    suspend fun byNome(nome: String): CampoCustomEntity?
}
