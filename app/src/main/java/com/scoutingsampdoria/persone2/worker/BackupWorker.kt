package com.scoutingsampdoria.persone2.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.scoutingsampdoria.persone2.data.repository.BackupManager
import com.scoutingsampdoria.persone2.data.repository.RisultatoBackup
import java.util.concurrent.TimeUnit

/**
 * Worker che effettua un backup automatico rispettando queste regole:
 *  - Parte solo se il dispositivo è connesso a Internet.
 *  - Non fa nulla se non c'è una cartella backup configurata.
 *  - Esegue realmente il backup solo se sono passate almeno 24 ore
 *    dall'ultimo backup effettuato (manuale o automatico).
 *
 * Il worker viene attivato in due modi:
 *  - OneTime con constraint di rete: registrato all'avvio dell'app,
 *    parte appena il tablet ha connessione. Se offline, resta in coda.
 *    Alla fine si ripianifica per la prossima esecuzione.
 *  - Periodic 24h: fallback per tablet sempre online, garantisce che
 *    almeno una volta al giorno venga verificata la necessità di backup.
 */
class BackupWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val backupManager = BackupManager(applicationContext)

        // Se non ci sono credenziali GitHub configurate, salta silenziosamente
        if (!backupManager.credenzialiConfigurate()) {
            // Ripianifico il OneTime per ritentare tra 6 ore (magari nel frattempo l'utente configura)
            pianificaOneTime(applicationContext, ritardoOre = 6)
            return Result.success()
        }

        // Verifico se è passato abbastanza tempo dall'ultimo backup
        val ultimo = backupManager.timestampUltimoBackup() ?: 0L
        val trascorseMs = System.currentTimeMillis() - ultimo
        val soglia24h = TimeUnit.HOURS.toMillis(24)

        val esitoFinale = if (trascorseMs >= soglia24h) {
            // Eseguo il backup
            when (val esito = backupManager.eseguiBackup()) {
                is RisultatoBackup.Successo -> Result.success()
                is RisultatoBackup.Errore -> {
                    if (runAttemptCount < 3) Result.retry() else Result.failure()
                }
            }
        } else {
            // Non serve ancora, salto
            Result.success()
        }

        // Ripianifico il OneTime per la prossima esecuzione (~24h)
        // Se durante quel tempo la rete viene persa e ritrovata, il worker parte appena riconnesso
        pianificaOneTime(applicationContext, ritardoOre = 24)
        return esitoFinale
    }

    companion object {

        /** Pianifica il worker OneTime che parte appena c'è rete.
         *  Da chiamare all'avvio dell'app e dopo ogni esecuzione. */
        fun pianificaOneTime(context: Context, ritardoOre: Long = 0) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val builder = OneTimeWorkRequestBuilder<BackupWorker>()
                .setConstraints(constraints)

            if (ritardoOre > 0) {
                builder.setInitialDelay(ritardoOre, TimeUnit.HOURS)
            }

            WorkManager.getInstance(context).enqueueUniqueWork(
                BackupManager.NOME_WORKER_BACKUP,
                ExistingWorkPolicy.REPLACE,
                builder.build()
            )
        }

        /** Pianifica il worker periodico come rete di sicurezza (parte ogni 24h se non l'ha già
         *  fatto il OneTime). Da chiamare all'avvio dell'app. */
        fun pianificaPeriodico(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val richiesta = PeriodicWorkRequestBuilder<BackupWorker>(
                repeatInterval = 1, repeatIntervalTimeUnit = TimeUnit.DAYS,
                flexTimeInterval = 6, flexTimeIntervalUnit = TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                BackupManager.NOME_WORKER_BACKUP_PERIODICO,
                ExistingPeriodicWorkPolicy.KEEP,
                richiesta
            )
        }

        /** Cancella tutti i backup automatici. */
        fun cancella(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(BackupManager.NOME_WORKER_BACKUP)
            WorkManager.getInstance(context).cancelUniqueWork(BackupManager.NOME_WORKER_BACKUP_PERIODICO)
        }
    }
}
