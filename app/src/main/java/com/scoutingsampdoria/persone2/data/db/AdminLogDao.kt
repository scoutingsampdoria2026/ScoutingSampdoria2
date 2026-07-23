package com.scoutingsampdoria.persone2.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface AdminLogDao {
    @Query("SELECT * FROM admin_log ORDER BY id DESC LIMIT :limite")
    suspend fun ultimi(limite: Int = 100): List<AdminLogEntity>

    @Insert
    suspend fun inserisci(log: AdminLogEntity): Long

    @Query("DELETE FROM admin_log")
    suspend fun eliminaTutti()
}
