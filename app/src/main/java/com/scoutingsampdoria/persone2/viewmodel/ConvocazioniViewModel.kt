package com.scoutingsampdoria.persone2.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scoutingsampdoria.persone2.data.model.Convocazione
import com.scoutingsampdoria.persone2.data.model.ConvocazioneGiocatore
import com.scoutingsampdoria.persone2.data.model.Persona
import com.scoutingsampdoria.persone2.data.repository.Repository
import kotlinx.coroutines.launch

class ConvocazioniViewModel(private val repository: Repository) : ViewModel() {

    var caricamento by mutableStateOf(false)
        private set
    var errore by mutableStateOf<String?>(null)
        private set
    var messaggio by mutableStateOf<String?>(null)
        private set

    var categorieDisponibili by mutableStateOf<List<String>>(emptyList())
        private set
    var convocazioni by mutableStateOf<List<Convocazione>>(emptyList())
        private set
    var convocazioneCorrente by mutableStateOf<Convocazione?>(null)
        private set
    var giocatoriCategoria by mutableStateOf<List<Persona>>(emptyList())
        private set

    fun caricaCategorieDaPersone(personeList: List<Persona>) {
        categorieDisponibili = personeList
            .mapNotNull { it.extra?.get("CATEGORIA")?.takeIf { c -> c.isNotBlank() } }
            .distinct()
            .sorted()
    }

    fun caricaConvocazioniPerCategoria(categoria: String) {
        caricamento = true
        errore = null
        viewModelScope.launch {
            try {
                convocazioni = repository.listaConvocazioni(categoria)
            } catch (e: Exception) {
                errore = e.message
            } finally {
                caricamento = false
            }
        }
    }

    fun caricaGiocatoriCategoria(personeList: List<Persona>, categoria: String) {
        giocatoriCategoria = personeList
            .filter { it.extra?.get("CATEGORIA") == categoria }
            .sortedWith(compareBy({ it.cognome }, { it.nome }))
    }

    fun caricaDettaglioConvocazione(id: Int) {
        caricamento = true
        errore = null
        viewModelScope.launch {
            try {
                convocazioneCorrente = repository.dettaglioConvocazione(id)
            } catch (e: Exception) {
                errore = e.message
            } finally {
                caricamento = false
            }
        }
    }

    fun creaConvocazione(
        categoria: String,
        data: String?,
        ora: String?,
        oraConvocazione: String?,
        impianto: String?,
        squadraCasa: String?,
        squadraOspite: String?,
        modulo: String?,
        onCreata: (Int) -> Unit,
    ) {
        caricamento = true
        errore = null
        viewModelScope.launch {
            try {
                val id = repository.creaConvocazione(
                    categoria = categoria,
                    data = data,
                    ora = ora,
                    oraConvocazione = oraConvocazione,
                    impianto = impianto,
                    squadraCasa = squadraCasa,
                    squadraOspite = squadraOspite,
                    modulo = modulo,
                )
                messaggio = "Convocazione creata"
                caricaConvocazioniPerCategoria(categoria)
                onCreata(id)
            } catch (e: Exception) {
                errore = e.message
            } finally {
                caricamento = false
            }
        }
    }

    fun aggiornaConvocazione(
        id: Int,
        data: String?,
        ora: String?,
        oraConvocazione: String?,
        impianto: String?,
        squadraCasa: String?,
        squadraOspite: String?,
        modulo: String?,
        onCompletato: () -> Unit,
    ) {
        caricamento = true
        errore = null
        viewModelScope.launch {
            try {
                repository.aggiornaConvocazione(
                    id, data, ora, oraConvocazione, impianto,
                    squadraCasa, squadraOspite, modulo
                )
                messaggio = "Convocazione salvata"
                caricaDettaglioConvocazione(id)
                onCompletato()
            } catch (e: Exception) {
                errore = e.message
            } finally {
                caricamento = false
            }
        }
    }

    fun aggiornaGiocatori(
        convocazioneId: Int,
        giocatori: List<ConvocazioneGiocatore>,
        onCompletato: () -> Unit,
    ) {
        caricamento = true
        errore = null
        viewModelScope.launch {
            try {
                repository.aggiornaGiocatoriConvocazione(convocazioneId, giocatori)
                messaggio = "Distinta aggiornata"
                caricaDettaglioConvocazione(convocazioneId)
                onCompletato()
            } catch (e: Exception) {
                errore = e.message
            } finally {
                caricamento = false
            }
        }
    }

    fun eliminaConvocazione(id: Int, categoria: String, onCompletato: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.eliminaConvocazione(id)
                messaggio = "Convocazione eliminata"
                caricaConvocazioniPerCategoria(categoria)
                onCompletato()
            } catch (e: Exception) {
                errore = e.message
            }
        }
    }

    fun pulisciMessaggi() {
        errore = null
        messaggio = null
    }
}
