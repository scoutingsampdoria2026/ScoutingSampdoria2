package com.scoutingsampdoria.persone2

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager
import com.scoutingsampdoria.persone2.data.db.ScoutingDatabase

class ScoutingApp : Application(), Configuration.Provider {

    override fun onCreate() {
        super.onCreate()
        // Forza la creazione del DB al primo avvio (inserisce i campi custom di default)
        ScoutingDatabase.get(this)
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().build()
}
