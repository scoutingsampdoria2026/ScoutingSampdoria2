package com.scoutingsampdoria.persone2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.scoutingsampdoria.persone2.data.model.Convocazione
import com.scoutingsampdoria.persone2.data.model.ConvocazioneGiocatore
import com.scoutingsampdoria.persone2.data.model.Persona
import com.scoutingsampdoria.persone2.ui.theme.SampColors
import com.scoutingsampdoria.persone2.viewmodel.ConvocazioniViewModel
import com.scoutingsampdoria.persone2.viewmodel.PersoneViewModel

/**
 * NOTA: questa è la versione base della schermata dettaglio convocazione.
 * In una fase successiva verranno aggiunti:
 *  - Il tab "Campo" con posizionamento interattivo dei calciatori stilizzati
 *  - Il pulsante di export PDF (con iText7)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConvocazioneDetailScreen(
    personeViewModel: PersoneViewModel,
    convocazioniViewModel: ConvocazioniViewModel,
    convocazioneId: Int,
    onIndietro: () -> Unit,
) {
    LaunchedEffect(convocazioneId) {
        convocazioniViewModel.caricaDettaglioConvocazione(convocazioneId)
        personeViewModel.caricaLista()
    }

    val conv = convocazioniViewModel.convocazioneCorrente

    LaunchedEffect(personeViewModel.persone, conv?.categoria) {
        conv?.categoria?.let {
            convocazioniViewModel.caricaGiocatoriCategoria(personeViewModel.persone, it)
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = onIndietro) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Indietro",
                                tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    },
                    title = {
                        Column {
                            Text("Convocazione",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold)
                            conv?.let {
                                Text("${it.categoria} · ${formattaDataItaliana(it.data)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f))
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
                FasciaBlucerchiata()
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (conv == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SampColors.Blu)
            }
        } else {
            var tabSelezionato by remember { mutableStateOf(0) }
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                TabRow(selectedTabIndex = tabSelezionato,
                    containerColor = MaterialTheme.colorScheme.background) {
                    Tab(selected = tabSelezionato == 0, onClick = { tabSelezionato = 0 },
                        text = { Text("Convocati") })
                    Tab(selected = tabSelezionato == 1, onClick = { tabSelezionato = 1 },
                        text = { Text("Distinta") })
                }
                when (tabSelezionato) {
                    0 -> TabConvocati(conv, convocazioniViewModel)
                    1 -> TabDistinta(conv, convocazioniViewModel)
                }
            }
        }
    }
}

@Composable
private fun TabConvocati(convocazione: Convocazione, viewModel: ConvocazioniViewModel) {
    val giocatoriCategoria = viewModel.giocatoriCategoria
    val convocati = convocazione.giocatori.orEmpty()

    // Set degli id già presenti nelle caselle
    val idConvocati = convocati.mapNotNull { it.personaId }.toSet()

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
    ) {
        Text("Seleziona i giocatori da convocare",
            style = MaterialTheme.typography.bodyMedium,
            color = SampColors.TestoSecondario)
        Spacer(Modifier.height(8.dp))

        giocatoriCategoria.forEach { giocatore ->
            val convocato = giocatore.id in idConvocati
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (convocato) SampColors.BluNebbia else SampColors.Superficie
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                onClick = {
                    val nuovi = if (convocato) {
                        // Rimuovo dalla lista
                        convocati.map { g ->
                            if (g.personaId == giocatore.id) g.copy(personaId = null, numero = null)
                            else g
                        }
                    } else {
                        // Aggiungo nella prima casella libera
                        var aggiunto = false
                        convocati.map { g ->
                            if (!aggiunto && g.personaId == null) {
                                aggiunto = true
                                g.copy(personaId = giocatore.id)
                            } else g
                        }
                    }
                    viewModel.aggiornaGiocatori(convocazione.id, nuovi) { }
                },
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("${giocatore.cognome} ${giocatore.nome}",
                        modifier = Modifier.weight(1f),
                        fontWeight = if (convocato) FontWeight.Bold else FontWeight.Normal)
                    if (convocato) {
                        Text("✓ Convocato", color = SampColors.Success,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun TabDistinta(convocazione: Convocazione, viewModel: ConvocazioniViewModel) {
    val giocatoriConvocati = convocazione.giocatori.orEmpty()
        .filter { it.personaId != null }
        .sortedBy { it.cognome ?: "" }

    val numeriPerCasella = remember(convocazione.id) {
        mutableStateMapOf<Int, String>().apply {
            giocatoriConvocati.forEach { g ->
                g.id?.let { put(it, g.numero?.toString() ?: "") }
            }
        }
    }
    var contatoreCambi by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
    ) {
        Text("Assegna il numero maglia (1-11 titolari)",
            style = MaterialTheme.typography.bodyMedium,
            color = SampColors.TestoSecondario)
        Spacer(Modifier.height(8.dp))

        giocatoriConvocati.forEach { g ->
            val numeriGiaUsati = numeriPerCasella
                .filter { it.key != g.id && it.value.isNotBlank() }
                .mapNotNull { it.value.toIntOrNull() }.toSet()
            val numeroCorrente = numeriPerCasella[g.id]?.toIntOrNull()
            var menuAperto by remember(g.id) { mutableStateOf(false) }

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${g.cognome} ${g.nome}", modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium)
                Box {
                    AssistChip(
                        onClick = { menuAperto = true },
                        label = { Text(numeroCorrente?.toString() ?: "N°",
                            fontWeight = FontWeight.Bold) },
                        trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
                        colors = if (numeroCorrente != null)
                            AssistChipDefaults.assistChipColors(
                                containerColor = SampColors.BluNebbia, labelColor = SampColors.Blu)
                        else AssistChipDefaults.assistChipColors(),
                        modifier = Modifier.width(90.dp)
                    )
                    DropdownMenu(expanded = menuAperto, onDismissRequest = { menuAperto = false }) {
                        if (numeroCorrente != null) {
                            DropdownMenuItem(
                                text = { Text("— Rimuovi —", fontWeight = FontWeight.Bold) },
                                onClick = {
                                    numeriPerCasella[g.id!!] = ""
                                    contatoreCambi++; menuAperto = false
                                }
                            )
                        }
                        (1..30).filter { it !in numeriGiaUsati }.forEach { n ->
                            DropdownMenuItem(
                                text = { Text("$n") },
                                onClick = {
                                    numeriPerCasella[g.id!!] = n.toString()
                                    contatoreCambi++; menuAperto = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                val numeriManuali = numeriPerCasella
                    .filter { it.value.isNotBlank() }
                    .mapValues { it.value.toInt() }
                val numeriUsati = numeriManuali.values.toMutableSet()
                val senzaNumero = giocatoriConvocati
                    .filter { numeriManuali[it.id] == null }
                    .sortedWith(compareBy({ it.cognome ?: "" }, { it.nome ?: "" }))
                val panchinaAssegnati = mutableMapOf<Int, Int>()
                var prossimo = 12
                senzaNumero.forEach { g ->
                    while (prossimo in numeriUsati) prossimo++
                    panchinaAssegnati[g.id!!] = prossimo
                    numeriUsati.add(prossimo); prossimo++
                }
                val nuovi = convocazione.giocatori.orEmpty().map { g ->
                    if (g.personaId != null) {
                        val num = numeriManuali[g.id] ?: panchinaAssegnati[g.id]
                        g.copy(numero = num)
                    } else g
                }
                viewModel.aggiornaGiocatori(convocazione.id, nuovi) { }
            },
            enabled = contatoreCambi > 0 && !viewModel.caricamento,
            colors = ButtonDefaults.buttonColors(containerColor = SampColors.Blu),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Save, contentDescription = null)
            Spacer(Modifier.width(4.dp))
            Text("Salva distinta", color = Color.White)
        }

        viewModel.messaggio?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = SampColors.Success, style = MaterialTheme.typography.bodySmall)
        }
    }
}
