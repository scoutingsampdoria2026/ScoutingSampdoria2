package com.scoutingsampdoria.persone2.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "admin_log")
data class AdminLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tipo: String,
    val dettaglio: String? = null,
    @ColumnInfo(name = "creato_il", defaultValue = "CURRENT_TIMESTAMP")
    val creatoIl: String? = null,
)
