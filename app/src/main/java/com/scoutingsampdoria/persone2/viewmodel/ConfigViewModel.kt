package com.scoutingsampdoria.persone2.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scoutingsampdoria.persone2.data.model.CampoCustom
import com.scoutingsampdoria.persone2.data.model.LogAdmin
import com.scoutingsampdoria.persone2.data.repository.Repository
import kotlinx.coroutines.launch

class ConfigViewModel(private val repository: Repository) : ViewModel() {

    var caricamento by mutableStateOf(false)
        private set
    var errore by mutableStateOf<String?>(null)
        private set
    var messaggio by mutableStateOf<String?>(null)
        private set

    var campiCustom by mutableStateOf<List<CampoCustom>>(emptyList())
        private set
    var logs by mutableStateOf<List<LogAdmin>>(emptyList())
        private set

    fun caricaTutto() {
        viewModelScope.launch {
            try {
                campiCustom = repository.campiCustom()
                logs = repository.logs()
            } catch (e: Exception) {
                errore = e.message
            }
        }
    }

    fun caricaLog() {
        viewModelScope.launch {
            try {
                logs = repository.logs()
            } catch (e: Exception) {
                errore = e.message
            }
        }
    }

    fun aggiungiCampo(nome: String) {
        if (nome.isBlank()) return
        viewModelScope.launch {
            try {
                repository.aggiungiCampoCustom(nome)
                campiCustom = repository.campiCustom()
                messaggio = "Campo $nome aggiunto"
            } catch (e: Exception) {
                errore = e.message
            }
        }
    }

    fun eliminaCampo(nome: String) {
        viewModelScope.launch {
            try {
                repository.eliminaCampoCustom(nome)
                campiCustom = repository.campiCustom()
                messaggio = "Campo $nome eliminato"
            } catch (e: Exception) {
                errore = e.message
            }
        }
    }

    fun svuotaDatabase(onCompletato: () -> Unit) {
        caricamento = true
        errore = null
        viewModelScope.launch {
            try {
                repository.eliminaTuttePersone()
                repository.scriviLog("svuota", "Database svuotato manualmente")
                logs = repository.logs()
                messaggio = "Database svuotato"
                onCompletato()
            } catch (e: Exception) {
                errore = e.message
            } finally {
                caricamento = false
            }
        }
    }

    fun pulisciMessaggi() {
        errore = null
        messaggio = null
    }
}
