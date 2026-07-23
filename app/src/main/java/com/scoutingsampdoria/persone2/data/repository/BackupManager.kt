package com.scoutingsampdoria.persone2.data.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.documentfile.provider.DocumentFile
import com.scoutingsampdoria.persone2.data.db.ScoutingDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Context.backupDataStore by preferencesDataStore(name = "scouting_backup")

/**
 * Gestisce l'export e l'import del database Room in file .db.
 * Il file di backup è la copia bit-per-bit del file SQLite.
 * L'URI della cartella di destinazione (es. una cartella su Google Drive)
 * è memorizzato in DataStore, insieme al timestamp dell'ultimo backup.
 */
class BackupManager(private val context: Context) {

    /** Restituisce l'URI persistente della cartella scelta per i backup, o null. */
    suspend fun cartellaDestinazione(): Uri? {
        val prefs = context.backupDataStore.data.first()
        val uriStringa = prefs[CHIAVE_CARTELLA_URI] ?: return null
        return runCatching { Uri.parse(uriStringa) }.getOrNull()
    }

    /** Salva la cartella scelta e richiede il permesso persistente su di essa. */
    suspend fun impostaCartellaDestinazione(uri: Uri) {
        // Chiedo permesso persistente lettura+scrittura sulla cartella
        val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        try {
            context.contentResolver.takePersistableUriPermission(uri, flags)
        } catch (_: SecurityException) {
            // Non tutti i provider concedono permessi persistenti (es. Drive richiede il flag già presente)
        }
        context.backupDataStore.edit { prefs ->
            prefs[CHIAVE_CARTELLA_URI] = uri.toString()
        }
    }

    /** Timestamp dell'ultimo backup effettuato (millisecondi epoch), o null. */
    suspend fun timestampUltimoBackup(): Long? {
        val prefs = context.backupDataStore.data.first()
        return prefs[CHIAVE_ULTIMO_BACKUP]
    }

    suspend fun descrizioneUltimoBackup(): String? {
        val ts = timestampUltimoBackup() ?: return null
        val fmt = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ITALIAN)
        return "Ultimo backup: ${fmt.format(Date(ts))}"
    }

    /**
     * Esegue un backup del DB scrivendo un file .db nella cartella persistente scelta.
     * Restituisce l'URI del file creato, o null in caso di errore.
     */
    suspend fun eseguiBackup(): RisultatoBackup {
        val cartella = cartellaDestinazione()
            ?: return RisultatoBackup.Errore("Nessuna cartella di backup selezionata")

        val destDir = DocumentFile.fromTreeUri(context, cartella)
            ?: return RisultatoBackup.Errore("Cartella non accessibile")

        if (!destDir.canWrite()) {
            return RisultatoBackup.Errore("La cartella scelta non è scrivibile")
        }

        // Chiudo il DB prima di copiarne il file per evitare corruzioni
        ScoutingDatabase.invalida()

        val fileDb = File(ScoutingDatabase.percorsoFile(context))
        if (!fileDb.exists()) {
            return RisultatoBackup.Errore("Database non trovato")
        }

        val fmt = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ITALIAN)
        val nomeFile = "scouting_backup_${fmt.format(Date())}.db"

        val fileDest = destDir.createFile("application/octet-stream", nomeFile)
            ?: return RisultatoBackup.Errore("Impossibile creare il file destinazione")

        return try {
            context.contentResolver.openOutputStream(fileDest.uri)?.use { output ->
                FileInputStream(fileDb).use { input ->
                    input.copyTo(output)
                }
            } ?: return RisultatoBackup.Errore("Impossibile aprire il file destinazione")

            // Aggiorno timestamp ultimo backup
            context.backupDataStore.edit { prefs ->
                prefs[CHIAVE_ULTIMO_BACKUP] = System.currentTimeMillis()
            }
            // Riapro il DB (verrà ricreato al prossimo accesso)
            ScoutingDatabase.get(context)
            RisultatoBackup.Successo(fileDest.uri, nomeFile)
        } catch (e: Exception) {
            RisultatoBackup.Errore("Errore durante il backup: ${e.message}")
        }
    }

    /**
     * Ripristina il database da un file .db scelto dall'utente.
     * Sovrascrive completamente il DB corrente.
     */
    suspend fun ripristinaBackup(fileBackupUri: Uri): RisultatoBackup {
        return try {
            // Chiudo il DB corrente
            ScoutingDatabase.invalida()

            val fileDb = File(ScoutingDatabase.percorsoFile(context))
            // Sovrascrivo il file
            context.contentResolver.openInputStream(fileBackupUri)?.use { input ->
                FileOutputStream(fileDb).use { output ->
                    input.copyTo(output)
                }
            } ?: return RisultatoBackup.Errore("Impossibile leggere il file selezionato")

            // Rimuovo eventuali file -shm e -wal per evitare inconsistenze
            File("${fileDb.absolutePath}-shm").takeIf { it.exists() }?.delete()
            File("${fileDb.absolutePath}-wal").takeIf { it.exists() }?.delete()

            // Il DB verrà ricreato al prossimo accesso
            RisultatoBackup.Successo(fileBackupUri, "ripristinato")
        } catch (e: Exception) {
            RisultatoBackup.Errore("Errore durante il ripristino: ${e.message}")
        }
    }

    companion object {
        private val CHIAVE_CARTELLA_URI = stringPreferencesKey("cartella_backup_uri")
        private val CHIAVE_ULTIMO_BACKUP = longPreferencesKey("ultimo_backup_ts")

        const val NOME_WORKER_BACKUP = "backup_automatico"
        const val NOME_WORKER_BACKUP_PERIODICO = "backup_automatico_periodico"
    }
}

sealed class RisultatoBackup {
    data class Successo(val uri: Uri, val nomeFile: String) : RisultatoBackup()
    data class Errore(val messaggio: String) : RisultatoBackup()
}
