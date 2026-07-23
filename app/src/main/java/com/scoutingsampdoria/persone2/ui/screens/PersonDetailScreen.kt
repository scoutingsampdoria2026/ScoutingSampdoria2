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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.scoutingsampdoria.persone2.data.model.Persona
import com.scoutingsampdoria.persone2.ui.theme.SampColors
import com.scoutingsampdoria.persone2.viewmodel.PersoneViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonDetailScreen(
    viewModel: PersoneViewModel,
    idPersona: Int,
    onIndietro: () -> Unit,
    onModifica: () -> Unit,
) {
    LaunchedEffect(idPersona) { viewModel.caricaDettaglio(idPersona) }

    var mostraConfermaElimina by remember { mutableStateOf(false) }
    val p = viewModel.personaSelezionata

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
                    title = { Text("Scheda giocatore", fontWeight = FontWeight.Bold) },
                    actions = {
                        IconButton(onClick = onModifica) {
                            Icon(Icons.Filled.Edit, contentDescription = "Modifica",
                                tint = MaterialTheme.colorScheme.onPrimary)
                        }
                        IconButton(onClick = { mostraConfermaElimina = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Elimina",
                                tint = MaterialTheme.colorScheme.onPrimary)
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
        if (p == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SampColors.Blu)
            }
        } else {
            var tabSelezionato by remember { mutableStateOf(0) }
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {

                // Hero blu con avatar e nome
                Card(
                    shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
                    colors = CardDefaults.cardColors(containerColor = SampColors.Blu),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth().padding(20.dp)
                    ) {
                        Box(
                            modifier = Modifier.size(80.dp)
                                .background(Color.White.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            val iniziali = listOfNotNull(
                                p.cognome.firstOrNull()?.uppercaseChar(),
                                p.nome.firstOrNull()?.uppercaseChar()
                            ).joinToString("")
                            Text(iniziali, color = Color.White,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("${p.cognome} ${p.nome}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold, color = Color.White)
                        val ruoloCat = listOfNotNull(p.ruolo, p.extra?.get("CATEGORIA")).joinToString(" · ")
                        if (ruoloCat.isNotEmpty()) {
                            Text(ruoloCat, style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.85f))
                        }
                        // Rating stelle
                        val rating = p.extra?.get("RATING")?.toIntOrNull() ?: 0
                        if (rating > 0) {
                            Spacer(Modifier.height(4.dp))
                            Row {
                                for (i in 1..5) {
                                    Icon(
                                        imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                        contentDescription = null,
                                        tint = SampColors.Warning,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                TabRow(selectedTabIndex = tabSelezionato,
                    containerColor = MaterialTheme.colorScheme.background) {
                    Tab(selected = tabSelezionato == 0, onClick = { tabSelezionato = 0 },
                        text = { Text("Info") })
                    Tab(selected = tabSelezionato == 1, onClick = { tabSelezionato = 1 },
                        text = { Text("Campi custom") })
                }

                Column(modifier = Modifier.fillMaxSize()
                    .verticalScroll(rememberScrollState()).padding(16.dp)) {
                    when (tabSelezionato) {
                        0 -> TabInfo(p)
                        1 -> TabCustom(p)
                    }
                }
            }
        }
    }

    if (mostraConfermaElimina) {
        AlertDialog(
            onDismissRequest = { mostraConfermaElimina = false },
            title = { Text("Conferma eliminazione") },
            text = { Text("Vuoi davvero eliminare questo giocatore? L'azione è irreversibile.") },
            confirmButton = {
                Button(
                    onClick = {
                        mostraConfermaElimina = false
                        p?.let { viewModel.eliminaPersona(it.id) { onIndietro() } }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SampColors.ErrorColor)
                ) { Text("Elimina", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { mostraConfermaElimina = false }) { Text("Annulla") }
            }
        )
    }
}

@Composable
private fun TabInfo(p: Persona) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        InfoCard("Data nascita", p.dataNascita ?: "-", Modifier.weight(1f))
        InfoCard("Regione", p.regione ?: "-", Modifier.weight(1f))
    }
    Spacer(Modifier.height(8.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        InfoCard("Società", p.societa ?: "-", Modifier.weight(1f))
        InfoCard("Matricola", p.matricola ?: "-", Modifier.weight(1f))
    }
    Spacer(Modifier.height(8.dp))
    if (!p.quickReport.isNullOrBlank()) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SampColors.RossoSoft),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("QUICK REPORT", style = MaterialTheme.typography.labelSmall,
                    color = SampColors.Rosso, fontWeight = FontWeight.Bold)
                Text(p.quickReport, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun TabCustom(p: Persona) {
    if (p.extra.isNullOrEmpty()) {
        Text("Nessun campo personalizzato",
            color = SampColors.TestoMuto, style = MaterialTheme.typography.bodyMedium)
    } else {
        p.extra.filter { it.value.isNotBlank() }.forEach { (k, v) ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Text(k, style = MaterialTheme.typography.labelMedium,
                    color = SampColors.Blu, fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(120.dp))
                Text(v, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun InfoCard(etichetta: String, valore: String, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SampColors.BluNebbia),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(etichetta.uppercase(), style = MaterialTheme.typography.labelSmall,
                color = SampColors.Blu, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(valore, style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold)
        }
    }
}
