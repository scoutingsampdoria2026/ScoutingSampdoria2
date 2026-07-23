package com.scoutingsampdoria.persone2

import android.app.Application
import androidx.work.Configuration
import com.scoutingsampdoria.persone2.data.db.ScoutingDatabase
import com.scoutingsampdoria.persone2.worker.BackupWorker

class ScoutingApp : Application(), Configuration.Provider {

    override fun onCreate() {
        super.onCreate()
        // Forza la creazione del DB al primo avvio (inserisce i campi custom di default)
        ScoutingDatabase.get(this)
        // Pianifica entrambi i worker di backup:
        //  - OneTime: parte appena c'è rete (se offline, resta in coda)
        //  - Periodic: rete di sicurezza ogni 24h per tablet sempre online
        // Al primo backup effettivo verifica se sono passate 24h dall'ultimo,
        // altrimenti salta silenziosamente.
        BackupWorker.pianificaOneTime(this)
        BackupWorker.pianificaPeriodico(this)
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().build()
}
