package com.scoutingsampdoria.persone2.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scoutingsampdoria.persone2.data.model.CampoCustom
import com.scoutingsampdoria.persone2.data.model.Persona
import com.scoutingsampdoria.persone2.data.repository.Repository
import kotlinx.coroutines.launch

class PersoneViewModel(private val repository: Repository) : ViewModel() {

    var caricamento by mutableStateOf(false)
        private set
    var errore by mutableStateOf<String?>(null)
        private set
    var messaggio by mutableStateOf<String?>(null)
        private set

    // Lista principale
    var persone by mutableStateOf<List<Persona>>(emptyList())
        private set
    var totale by mutableStateOf(0)
        private set

    // Dettaglio corrente
    var personaSelezionata by mutableStateOf<Persona?>(null)
        private set

    // Campi custom disponibili
    var campiCustom by mutableStateOf<List<CampoCustom>>(emptyList())
        private set

    // Filtri applicati
    var filtroRegione by mutableStateOf<String?>(null)
        private set
    var filtroSocieta by mutableStateOf<String?>(null)
        private set
    var filtroRuolo by mutableStateOf<String?>(null)
        private set
    var filtroQuickReport by mutableStateOf<String?>(null)
        private set
    val filtriExtra = mutableStateMapOf<String, String>()

    // Valori distinct per popolare i dropdown filtro
    var valoriRegione by mutableStateOf<List<String>>(emptyList())
        private set
    var valoriSocieta by mutableStateOf<List<String>>(emptyList())
        private set
    var valoriRuolo by mutableStateOf<List<String>>(emptyList())
        private set
    var valoriQuickReport by mutableStateOf<List<String>>(emptyList())
        private set
    val valoriExtra = mutableStateMapOf<String, List<String>>()

    fun caricaLista(query: String? = null) {
        caricamento = true
        errore = null
        viewModelScope.launch {
            try {
                persone = repository.listaPersone(
                    query = query,
                    regione = filtroRegione,
                    societa = filtroSocieta,
                    ruolo = filtroRuolo,
                    quickReport = filtroQuickReport,
                    filtriExtra = filtriExtra.toMap()
                )
                totale = persone.size
                // Ricalcolo anche i valori distinct per i dropdown
                valoriRegione = repository.valoriRegione()
                valoriSocieta = repository.valoriSocieta()
                valoriRuolo = repository.valoriRuolo()
                valoriQuickReport = repository.valoriQuickReport()
                // Valori extra dai campi custom presenti nei giocatori
                val extraMap = mutableMapOf<String, MutableSet<String>>()
                persone.forEach { p ->
                    p.extra?.forEach { (k, v) ->
                        if (v.isNotBlank()) extraMap.getOrPut(k) { mutableSetOf() }.add(v)
                    }
                }
                valoriExtra.clear()
                extraMap.forEach { (k, set) -> valoriExtra[k] = set.sorted() }
            } catch (e: Exception) {
                errore = e.message ?: "Errore lettura DB"
            } finally {
                caricamento = false
            }
        }
    }

    fun caricaDettaglio(id: Int) {
        caricamento = true
        errore = null
        viewModelScope.launch {
            try {
                personaSelezionata = repository.dettaglioPersona(id)
            } catch (e: Exception) {
                errore = e.message
            } finally {
                caricamento = false
            }
        }
    }

    fun caricaCampiCustom() {
        viewModelScope.launch {
            try {
                campiCustom = repository.campiCustom()
            } catch (e: Exception) {
                errore = e.message
            }
        }
    }

    fun creaPersona(persona: Persona, onCompletato: (Int) -> Unit) {
        caricamento = true
        errore = null
        viewModelScope.launch {
            try {
                val id = repository.creaPersona(persona)
                messaggio = "Giocatore creato"
                caricaLista()
                onCompletato(id)
            } catch (e: Exception) {
                errore = e.message
            } finally {
                caricamento = false
            }
        }
    }

    fun aggiornaPersona(persona: Persona, onCompletato: () -> Unit) {
        caricamento = true
        errore = null
        viewModelScope.launch {
            try {
                repository.aggiornaPersona(persona)
                messaggio = "Giocatore aggiornato"
                caricaLista()
                personaSelezionata = repository.dettaglioPersona(persona.id)
                onCompletato()
            } catch (e: Exception) {
                errore = e.message
            } finally {
                caricamento = false
            }
        }
    }

    fun eliminaPersona(id: Int, onCompletato: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.eliminaPersona(id)
                messaggio = "Giocatore eliminato"
                caricaLista()
                onCompletato()
            } catch (e: Exception) {
                errore = e.message
            }
        }
    }

    fun impostaFiltro(
        regione: String? = filtroRegione,
        societa: String? = filtroSocieta,
        ruolo: String? = filtroRuolo,
        quickReport: String? = filtroQuickReport,
    ) {
        filtroRegione = regione
        filtroSocieta = societa
        filtroRuolo = ruolo
        filtroQuickReport = quickReport
        caricaLista()
    }

    fun impostaFiltroExtra(campo: String, valore: String?) {
        if (valore == null) filtriExtra.remove(campo)
        else filtriExtra[campo] = valore
        caricaLista()
    }

    fun azzeraFiltri() {
        filtroRegione = null
        filtroSocieta = null
        filtroRuolo = null
        filtroQuickReport = null
        filtriExtra.clear()
        caricaLista()
    }

    fun pulisciMessaggi() {
        errore = null
        messaggio = null
    }
}
