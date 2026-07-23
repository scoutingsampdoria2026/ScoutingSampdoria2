package com.scoutingsampdoria.persone2.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val Context.dataStore by preferencesDataStore(name = "scouting_prefs")

/** ViewModel per gestire lo sblocco tramite codice.
 *  Il codice iniziale è "391622" ma può essere modificato dall'utente in Configurazione. */
class AuthViewModel(private val context: Context) : ViewModel() {

    var sbloccato by mutableStateOf(false)
        private set

    var errore by mutableStateOf<String?>(null)
        private set

    var tentatividiSblocco by mutableStateOf(0)
        private set

    fun tentaSblocco(codice: String) {
        viewModelScope.launch {
            val corrente = leggiCodiceCorrente()
            if (codice == corrente) {
                sbloccato = true
                errore = null
                tentatividiSblocco = 0
            } else {
                errore = "Codice errato"
                tentatividiSblocco++
            }
        }
    }

    fun blocca() {
        sbloccato = false
        errore = null
    }

    fun cambiaCodice(nuovo: String, onCompletato: (Boolean) -> Unit) {
        viewModelScope.launch {
            if (nuovo.isBlank() || nuovo.length < 4) {
                errore = "Il codice deve avere almeno 4 caratteri"
                onCompletato(false)
                return@launch
            }
            context.dataStore.edit { prefs ->
                prefs[CHIAVE_CODICE] = nuovo
            }
            onCompletato(true)
        }
    }

    fun pulisciErrore() {
        errore = null
    }

    private suspend fun leggiCodiceCorrente(): String {
        val prefs = context.dataStore.data.first()
        return prefs[CHIAVE_CODICE] ?: CODICE_DEFAULT
    }

    companion object {
        private val CHIAVE_CODICE = stringPreferencesKey("codice_sblocco")
        private const val CODICE_DEFAULT = "391622"
    }
}
