package com.scoutingsampdoria.persone2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.scoutingsampdoria.persone2.data.model.Persona
import com.scoutingsampdoria.persone2.ui.theme.SampColors
import com.scoutingsampdoria.persone2.viewmodel.PersoneViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonListScreen(
    viewModel: PersoneViewModel,
    onIndietro: () -> Unit,
    onSelezionaGiocatore: (Int) -> Unit,
    onNuovoGiocatore: () -> Unit,
    onApriFiltri: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var categoriaSelezionata by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.caricaLista()
        viewModel.caricaCampiCustom()
    }

    val categorieDisponibili = remember(viewModel.persone) {
        viewModel.persone
            .mapNotNull { it.extra?.get("CATEGORIA")?.takeIf { c -> c.isNotBlank() } }
            .distinct()
            .sorted()
    }

    val personeFiltrate = if (categoriaSelezionata == null) viewModel.persone
    else viewModel.persone.filter { it.extra?.get("CATEGORIA") == categoriaSelezionata }

    val numeriFiltriAttivi = listOfNotNull(
        viewModel.filtroRegione, viewModel.filtroSocieta,
        viewModel.filtroRuolo, viewModel.filtroQuickReport
    ).size + viewModel.filtriExtra.size

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
                            Text("Gestione Giocatori", fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium)
                            Text("${personeFiltrate.size} di ${viewModel.persone.size}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f))
                        }
                    },
                    actions = {
                        BadgedBox(badge = {
                            if (numeriFiltriAttivi > 0) {
                                Badge(containerColor = SampColors.Rosso) {
                                    Text(numeriFiltriAttivi.toString(), color = Color.White)
                                }
                            }
                        }) {
                            IconButton(onClick = onApriFiltri) {
                                Icon(Icons.Filled.FilterList, contentDescription = "Filtri",
                                    tint = MaterialTheme.colorScheme.onPrimary)
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNuovoGiocatore,
                containerColor = SampColors.Rosso,
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Nuovo giocatore")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 12.dp)) {

            // Barra ricerca
            OutlinedTextField(
                value = query,
                onValueChange = {
                    query = it
                    viewModel.caricaLista(it)
                },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                placeholder = { Text("Cerca cognome o nome...") },
                singleLine = true,
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            )

            // Card riepilogo
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SampColors.BluNebbia),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp)) {
                    StatBox(SampColors.Blu, viewModel.persone.size.toString(), "Totali")
                    Spacer(Modifier.weight(1f))
                    StatBox(SampColors.Info, categorieDisponibili.size.toString(), "Categorie")
                }
            }

            // Chip categorie
            if (categorieDisponibili.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ChipCategoria(
                        etichetta = "Tutte",
                        conteggio = viewModel.persone.size,
                        selezionata = categoriaSelezionata == null,
                        onClick = { categoriaSelezionata = null }
                    )
                    categorieDisponibili.forEach { cat ->
                        val conteggio = viewModel.persone.count { it.extra?.get("CATEGORIA") == cat }
                        ChipCategoria(
                            etichetta = cat,
                            conteggio = conteggio,
                            selezionata = categoriaSelezionata == cat,
                            onClick = { categoriaSelezionata = cat }
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            if (viewModel.caricamento) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SampColors.Blu)
                }
            } else if (personeFiltrate.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.Groups, contentDescription = null,
                            tint = SampColors.TestoMuto, modifier = Modifier.size(64.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("Nessun giocatore trovato",
                            style = MaterialTheme.typography.bodyLarge,
                            color = SampColors.TestoSecondario)
                        Text("Tocca + per aggiungerne uno",
                            style = MaterialTheme.typography.bodySmall,
                            color = SampColors.TestoMuto)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(personeFiltrate, key = { it.id }) { p ->
                        CardGiocatore(p) { onSelezionaGiocatore(p.id) }
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
private fun StatBox(colore: Color, valore: String, etichetta: String) {
    Column {
        Text(valore, style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold, color = colore)
        Text(etichetta, style = MaterialTheme.typography.labelSmall,
            color = SampColors.TestoSecondario)
    }
}

@Composable
private fun ChipCategoria(etichetta: String, conteggio: Int, selezionata: Boolean, onClick: () -> Unit) {
    AssistChip(
        onClick = onClick,
        label = {
            Text("$etichetta ($conteggio)",
                fontWeight = if (selezionata) FontWeight.Bold else FontWeight.Normal)
        },
        colors = if (selezionata)
            AssistChipDefaults.assistChipColors(
                containerColor = SampColors.Blu,
                labelColor = Color.White
            )
        else AssistChipDefaults.assistChipColors()
    )
}

@Composable
private fun CardGiocatore(persona: Persona, onClick: () -> Unit) {
    val rating = persona.extra?.get("RATING")?.toIntOrNull() ?: 0
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = SampColors.Superficie),
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            // Avatar iniziali
            Box(
                modifier = Modifier.size(44.dp)
                    .background(SampColors.Blu, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                val iniziali = listOfNotNull(
                    persona.cognome.firstOrNull()?.uppercaseChar(),
                    persona.nome.firstOrNull()?.uppercaseChar()
                ).joinToString("")
                Text(iniziali, color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("${persona.cognome} ${persona.nome}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold, color = SampColors.Nero)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    persona.ruolo?.let {
                        Badge(containerColor = SampColors.Rosso, contentColor = Color.White) {
                            Text(it, modifier = Modifier.padding(horizontal = 6.dp))
                        }
                        Spacer(Modifier.width(6.dp))
                    }
                    val categoria = persona.extra?.get("CATEGORIA")
                    categoria?.let {
                        Badge(containerColor = SampColors.Blu, contentColor = Color.White) {
                            Text(it, modifier = Modifier.padding(horizontal = 6.dp))
                        }
                        Spacer(Modifier.width(6.dp))
                    }
                    persona.societa?.let {
                        Text(it, style = MaterialTheme.typography.labelSmall,
                            color = SampColors.TestoSecondario)
                    }
                }
            }
            if (rating > 0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Star, contentDescription = null,
                        tint = SampColors.Warning, modifier = Modifier.size(16.dp))
                    Text(rating.toString(), fontWeight = FontWeight.Bold, color = SampColors.Warning)
                }
            }
        }
    }
}
