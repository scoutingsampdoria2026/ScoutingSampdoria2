package com.scoutingsampdoria.persone2.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "campi_custom",
    indices = [Index(value = ["nome"], unique = true)]
)
data class CampoCustomEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nome: String,
)
