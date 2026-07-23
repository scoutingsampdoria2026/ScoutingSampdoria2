package com.scoutingsampdoria.persone2.data.model

import org.json.JSONObject

data class Persona(
    val id: Int,
    val cognome: String,
    val nome: String,
    val dataNascita: String? = null,
    val regione: String? = null,
    val societa: String? = null,
    val ruolo: String? = null,
    val matricola: String? = null,
    val quickReport: String? = null,
    val extra: Map<String, String>? = null,
) {
    companion object {
        fun parseExtra(json: String?): Map<String, String>? {
            if (json.isNullOrBlank()) return null
            return try {
                val jo = JSONObject(json)
                val mappa = mutableMapOf<String, String>()
                jo.keys().forEach { k -> mappa[k] = jo.optString(k, "") }
                mappa
            } catch (e: Exception) {
                null
            }
        }

        fun serializzaExtra(mappa: Map<String, String>?): String? {
            if (mappa.isNullOrEmpty()) return null
            val jo = JSONObject()
            mappa.forEach { (k, v) -> jo.put(k, v) }
            return jo.toString()
        }
    }
}

data class CampoCustom(val id: Int, val nome: String)

data class LogAdmin(
    val id: Int,
    val tipo: String,
    val dettaglio: String?,
    val creatoIl: String?,
)

data class Convocazione(
    val id: Int,
    val categoria: String,
    val data: String? = null,
    val ora: String? = null,
    val oraConvocazione: String? = null,
    val impianto: String? = null,
    val squadraCasa: String? = null,
    val squadraOspite: String? = null,
    val modulo: String? = null,
    val note: String? = null,
    val giocatori: List<ConvocazioneGiocatore>? = null,
)

data class ConvocazioneGiocatore(
    val id: Int? = null,
    val personaId: Int?,
    val numero: Int?,
    val ordine: Int,
    val posizioneCampo: Int? = null,
    val cognome: String? = null,
    val nome: String? = null,
    val ruolo: String? = null,
)
