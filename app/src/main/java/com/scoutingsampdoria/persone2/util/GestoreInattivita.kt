package com.scoutingsampdoria.persone2.util

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.delay

/** Timeout inattività: 30 minuti in millisecondi. */
const val TIMEOUT_INATTIVITA_MS = 30L * 60L * 1000L

/**
 * Modifier che intercetta ogni tocco (senza consumarlo) e aggiorna il timestamp
 * dell'ultima interazione. Da applicare sul contenitore radice dell'app dopo il login.
 */
fun Modifier.tracciaInterazione(onInterazione: () -> Unit): Modifier =
    this.pointerInput(Unit) {
        awaitEachGesture {
            // Osservo il gesto senza consumarlo (Initial pass) e aggiorno il timestamp
            awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
            onInterazione()
        }
    }

/**
 * Composable che monitora il tempo dall'ultima interazione dell'utente.
 * Quando supera TIMEOUT_INATTIVITA_MS, chiama onScaduto() (di solito per fare logoff).
 *
 * Uso tipico:
 *   var ultimaInterazione by remember { mutableLongStateOf(System.currentTimeMillis()) }
 *   MonitorInattivita(ultimaInterazione) { logoff() }
 *   Box(Modifier.tracciaInterazione { ultimaInterazione = System.currentTimeMillis() }) { ... }
 */
@Composable
fun MonitorInattivita(
    ultimaInterazioneMs: Long,
    onScaduto: () -> Unit
) {
    val onScadutoAggiornato by rememberUpdatedState(onScaduto)
    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000L) // Controllo ogni 30 secondi
            val trascorsi = System.currentTimeMillis() - ultimaInterazioneMs
            if (trascorsi >= TIMEOUT_INATTIVITA_MS) {
                onScadutoAggiornato()
                break
            }
        }
    }
}
