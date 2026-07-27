package com.scoutingsampdoria.persone2.util

import com.scoutingsampdoria.persone2.BuildConfig

/**
 * Verifica se l'app deve considerarsi non più utilizzabile.
 * Il limite viene calcolato al momento della build (vedi build.gradle.kts,
 * campo TIMESTAMP_LIMITE in BuildConfig).
 *
 * Nota: la verifica usa la data del dispositivo. Se l'utente cambia manualmente
 * la data del sistema all'indietro, la verifica può essere bypassata.
 * Questo è un limite intrinseco del modello "scadenza offline".
 */
object GestoreScadenza {

    /** True se l'app è oltre la data limite di utilizzo. */
    fun appNonPiuUtilizzabile(): Boolean {
        return System.currentTimeMillis() >= BuildConfig.TIMESTAMP_LIMITE
    }

    /** Codice errore da mostrare all'utente (camuffato da bug). */
    val codiceErrore: String get() = BuildConfig.CODICE_ERRORE

    /** Email di supporto a cui l'utente può scrivere. */
    val emailSupporto: String get() = BuildConfig.EMAIL_SUPPORTO
}
