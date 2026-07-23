package com.scoutingsampdoria.persone2.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.scoutingsampdoria.persone2.data.db.ScoutingDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

private val Context.backupDataStore by preferencesDataStore(name = "scouting_backup")

/**
 * Gestisce backup e ripristino del DB caricando/scaricando file .db
 * come "release assets" su un repository GitHub privato.
 */
class BackupManager(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()

    private val prefsCifrate by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "scouting_secure",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    suspend fun ownerRepo(): String? = context.backupDataStore.data.first()[CHIAVE_OWNER]
    suspend fun nomeRepo(): String? = context.backupDataStore.data.first()[CHIAVE_REPO]

    fun tokenGitHub(): String? = prefsCifrate.getString(CHIAVE_TOKEN, null)?.takeIf { it.isNotBlank() }

    suspend fun impostaCredenziali(owner: String, repo: String, token: String) {
        context.backupDataStore.edit { prefs ->
            prefs[CHIAVE_OWNER] = owner.trim()
            prefs[CHIAVE_REPO] = repo.trim()
        }
        prefsCifrate.edit().putString(CHIAVE_TOKEN, token.trim()).apply()
    }

    suspend fun cancellaCredenziali() {
        context.backupDataStore.edit { prefs ->
            prefs.remove(CHIAVE_OWNER)
            prefs.remove(CHIAVE_REPO)
        }
        prefsCifrate.edit().remove(CHIAVE_TOKEN).apply()
    }

    suspend fun credenzialiConfigurate(): Boolean {
        return !ownerRepo().isNullOrBlank() &&
               !nomeRepo().isNullOrBlank() &&
               !tokenGitHub().isNullOrBlank()
    }

    suspend fun timestampUltimoBackup(): Long? {
        return context.backupDataStore.data.first()[CHIAVE_ULTIMO_BACKUP]
    }

    suspend fun descrizioneUltimoBackup(): String? {
        val ts = timestampUltimoBackup() ?: return null
        val fmt = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ITALIAN)
        return "Ultimo backup: ${fmt.format(Date(ts))}"
    }

    /** Esegue un backup caricando il file .db come release GitHub. */
    suspend fun eseguiBackup(): RisultatoBackup = withContext(Dispatchers.IO) {
        if (!credenzialiConfigurate()) {
            return@withContext RisultatoBackup.Errore("Credenziali GitHub non configurate")
        }
        val owner = ownerRepo()!!
        val repo = nomeRepo()!!
        val token = tokenGitHub()!!

        ScoutingDatabase.invalida()
        val fileDb = File(ScoutingDatabase.percorsoFile(context))
        if (!fileDb.exists()) {
            return@withContext RisultatoBackup.Errore("Database non trovato")
        }

        val fmt = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.ITALIAN)
        val timestamp = fmt.format(Date())
        val tag = "backup-$timestamp"
        val nomeFile = "scouting_backup_$timestamp.db"

        try {
            // 1. Crea la release
            val corpoRelease = JSONObject().apply {
                put("tag_name", tag)
                put("name", "Backup $timestamp")
                put("body", "Backup automatico DB Scouting Sampdoria")
                put("draft", false)
                put("prerelease", false)
            }.toString()

            val reqCreaRelease = Request.Builder()
                .url("https://api.github.com/repos/$owner/$repo/releases")
                .header("Authorization", "Bearer $token")
                .header("Accept", "application/vnd.github+json")
                .header("X-GitHub-Api-Version", "2022-11-28")
                .post(corpoRelease.toRequestBody("application/json".toMediaTypeOrNull()))
                .build()

            val respRelease = client.newCall(reqCreaRelease).execute()
            if (!respRelease.isSuccessful) {
                val err = respRelease.body?.string()?.take(200) ?: ""
                respRelease.close()
                return@withContext RisultatoBackup.Errore(
                    "Errore creazione release (${respRelease.code}): $err"
                )
            }

            val bodyRelease = respRelease.body?.string() ?: ""
            respRelease.close()
            val jsonRelease = JSONObject(bodyRelease)
            val uploadUrl = jsonRelease.getString("upload_url")
                .replace("{?name,label}", "?name=$nomeFile")

            // 2. Carica il file .db come asset
            val reqUpload = Request.Builder()
                .url(uploadUrl)
                .header("Authorization", "Bearer $token")
                .header("Accept", "application/vnd.github+json")
                .header("Content-Type", "application/octet-stream")
                .post(fileDb.readBytes().toRequestBody("application/octet-stream".toMediaTypeOrNull()))
                .build()

            val respUpload = client.newCall(reqUpload).execute()
            if (!respUpload.isSuccessful) {
                val err = respUpload.body?.string()?.take(200) ?: ""
                respUpload.close()
                return@withContext RisultatoBackup.Errore(
                    "Errore upload file (${respUpload.code}): $err"
                )
            }
            respUpload.close()

            // 3. Aggiorna timestamp
            context.backupDataStore.edit { prefs ->
                prefs[CHIAVE_ULTIMO_BACKUP] = System.currentTimeMillis()
            }

            ScoutingDatabase.get(context)
            RisultatoBackup.Successo(nomeFile, tag)
        } catch (e: Exception) {
            RisultatoBackup.Errore("Errore rete: ${e.message}")
        }
    }

    /** Elenca i backup disponibili come release. */
    suspend fun elencoBackup(): List<BackupRemoto> = withContext(Dispatchers.IO) {
        if (!credenzialiConfigurate()) return@withContext emptyList()
        val owner = ownerRepo()!!
        val repo = nomeRepo()!!
        val token = tokenGitHub()!!

        try {
            val req = Request.Builder()
                .url("https://api.github.com/repos/$owner/$repo/releases?per_page=100")
                .header("Authorization", "Bearer $token")
                .header("Accept", "application/vnd.github+json")
                .header("X-GitHub-Api-Version", "2022-11-28")
                .get()
                .build()

            val resp = client.newCall(req).execute()
            if (!resp.isSuccessful) {
                resp.close()
                return@withContext emptyList()
            }
            val body = resp.body?.string() ?: "[]"
            resp.close()

            val arr = JSONArray(body)
            val lista = mutableListOf<BackupRemoto>()
            for (i in 0 until arr.length()) {
                val rel = arr.getJSONObject(i)
                val tag = rel.getString("tag_name")
                val name = rel.optString("name", tag)
                val createdAt = rel.optString("created_at", "")
                val assets = rel.getJSONArray("assets")
                if (assets.length() == 0) continue
                val primoAsset = assets.getJSONObject(0)
                val urlDownload = primoAsset.getString("url")
                val nomeFile = primoAsset.getString("name")
                val dimensione = primoAsset.optLong("size", 0)
                lista.add(BackupRemoto(tag, name, createdAt, urlDownload, nomeFile, dimensione))
            }
            lista
        } catch (e: Exception) {
            emptyList()
        }
    }

    /** Ripristina il DB scaricando un asset dal repo GitHub. */
    suspend fun ripristinaBackup(urlAsset: String): RisultatoBackup = withContext(Dispatchers.IO) {
        val token = tokenGitHub() ?: return@withContext RisultatoBackup.Errore("Token mancante")
        try {
            val req = Request.Builder()
                .url(urlAsset)
                .header("Authorization", "Bearer $token")
                .header("Accept", "application/octet-stream")
                .header("X-GitHub-Api-Version", "2022-11-28")
                .get()
                .build()

            val resp = client.newCall(req).execute()
            if (!resp.isSuccessful) {
                val err = resp.body?.string()?.take(200) ?: ""
                resp.close()
                return@withContext RisultatoBackup.Errore("Errore download (${resp.code}): $err")
            }

            ScoutingDatabase.invalida()
            val fileDb = File(ScoutingDatabase.percorsoFile(context))
            resp.body?.byteStream()?.use { input ->
                FileOutputStream(fileDb).use { output ->
                    input.copyTo(output)
                }
            }
            resp.close()

            File("${fileDb.absolutePath}-shm").takeIf { it.exists() }?.delete()
            File("${fileDb.absolutePath}-wal").takeIf { it.exists() }?.delete()

            RisultatoBackup.Successo("ripristinato", "")
        } catch (e: Exception) {
            RisultatoBackup.Errore("Errore rete: ${e.message}")
        }
    }

    /** Testa le credenziali facendo una GET al repo. */
    suspend fun testaCredenziali(): RisultatoTest = withContext(Dispatchers.IO) {
        if (!credenzialiConfigurate()) return@withContext RisultatoTest.Errore("Credenziali incomplete")
        val owner = ownerRepo()!!
        val repo = nomeRepo()!!
        val token = tokenGitHub()!!

        try {
            val req = Request.Builder()
                .url("https://api.github.com/repos/$owner/$repo")
                .header("Authorization", "Bearer $token")
                .header("Accept", "application/vnd.github+json")
                .get()
                .build()
            val resp = client.newCall(req).execute()
            val ok = resp.isSuccessful
            val codice = resp.code
            resp.close()
            if (ok) RisultatoTest.Successo
            else RisultatoTest.Errore("Repository non accessibile (HTTP $codice)")
        } catch (e: Exception) {
            RisultatoTest.Errore("Errore rete: ${e.message}")
        }
    }

    companion object {
        private val CHIAVE_OWNER = stringPreferencesKey("gh_owner")
        private val CHIAVE_REPO = stringPreferencesKey("gh_repo")
        private const val CHIAVE_TOKEN = "gh_token"
        private val CHIAVE_ULTIMO_BACKUP = longPreferencesKey("ultimo_backup_ts")

        const val NOME_WORKER_BACKUP = "backup_automatico"
        const val NOME_WORKER_BACKUP_PERIODICO = "backup_automatico_periodico"
    }
}

sealed class RisultatoBackup {
    data class Successo(val nomeFile: String, val tag: String) : RisultatoBackup()
    data class Errore(val messaggio: String) : RisultatoBackup()
}

sealed class RisultatoTest {
    object Successo : RisultatoTest()
    data class Errore(val messaggio: String) : RisultatoTest()
}

data class BackupRemoto(
    val tag: String,
    val nome: String,
    val createdAt: String,
    val urlDownload: String,
    val nomeFile: String,
    val dimensioneByte: Long,
)
