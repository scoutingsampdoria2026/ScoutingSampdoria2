package com.scoutingsampdoria.persone2.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scoutingsampdoria.persone2.data.model.Provino
import com.scoutingsampdoria.persone2.data.model.StatisticheProvini
import com.scoutingsampdoria.persone2.data.repository.Repository
import kotlinx.coroutines.launch

class ProviniViewModel(private val repository: Repository) : ViewModel() {

    var caricamento by mutableStateOf(false)
        private set
    var errore by mutableStateOf<String?>(null)
        private set
    var messaggio by mutableStateOf<String?>(null)
        private set

    // Per la scheda giocatore
    var provini by mutableStateOf<List<Provino>>(emptyList())
        private set
    var provinoCorrente by mutableStateOf<Provino?>(null)
        private set

    // Per la dashboard
    var statistiche by mutableStateOf<StatisticheProvini?>(null)
        private set
    var proviniDashboard by mutableStateOf<List<Provino>>(emptyList())
        private set
    var categorieDisponibili by mutableStateOf<List<String>>(emptyList())
        private set
    var dateDisponibili by mutableStateOf<List<String>>(emptyList())
        private set
    var filtroCategoria by mutableStateOf<String?>(null)
        private set
    var filtroData by mutableStateOf<String?>(null)
        private set

    fun caricaProviniPersona(personaId: Int) {
        caricamento = true
        errore = null
        viewModelScope.launch {
            try {
                provini = repository.listaProviniPersona(personaId)
            } catch (e: Exception) {
                errore = "Errore caricamento provini: ${e.message}"
            }
            caricamento = false
        }
    }

    fun caricaDettaglio(provinoId: Int) {
        caricamento = true
        errore = null
        viewModelScope.launch {
            try {
                provinoCorrente = repository.dettaglioProvino(provinoId)
            } catch (e: Exception) {
                errore = "Errore: ${e.message}"
            }
            caricamento = false
        }
    }

    fun aggiornaProvino(
        provinoId: Int,
        presenza: String?,
        giudizio: Int?,
        note: String?,
        onCompletato: () -> Unit
    ) {
        caricamento = true
        errore = null
        viewModelScope.launch {
            try {
                repository.aggiornaProvino(provinoId, presenza, giudizio, note)
                messaggio = "Provino aggiornato"
                caricaDettaglio(provinoId)
                onCompletato()
            } catch (e: Exception) {
                errore = "Errore aggiornamento: ${e.message}"
            }
            caricamento = false
        }
    }

    // === Dashboard ===

    fun caricaStatistiche(forza: Boolean = false) {
        if (statistiche != null && !forza) return
        viewModelScope.launch {
            try {
                statistiche = repository.statisticheProvini()
            } catch (e: Exception) {
                errore = "Errore statistiche: ${e.message}"
            }
        }
    }

    fun caricaDashboard(categoria: String? = filtroCategoria, data: String? = filtroData) {
        filtroCategoria = categoria
        filtroData = data
        caricamento = true
        errore = null
        viewModelScope.launch {
            try {
                statistiche = repository.statisticheProvini()
                categorieDisponibili = repository.categorieProvini()
                dateDisponibili = repository.dateProvini(categoria)
                proviniDashboard = repository.elencoProviniDashboard(categoria, data)
            } catch (e: Exception) {
                errore = "Errore caricamento dashboard: ${e.message}"
            }
            caricamento = false
        }
    }

    fun impostaFiltroCategoria(categoria: String?) {
        filtroCategoria = categoria
        filtroData = null
        caricaDashboard()
    }

    fun impostaFiltroData(data: String?) {
        filtroData = data
        caricaDashboard()
    }

    fun pulisciMessaggi() {
        errore = null
        messaggio = null
    }
}
