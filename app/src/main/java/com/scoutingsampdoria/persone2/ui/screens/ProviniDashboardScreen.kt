package com.scoutingsampdoria.persone2.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.scoutingsampdoria.persone2.data.model.Provino
import com.scoutingsampdoria.persone2.ui.theme.SampColors
import com.scoutingsampdoria.persone2.viewmodel.ProviniViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviniDashboardScreen(
    proviniViewModel: ProviniViewModel,
    onIndietro: () -> Unit,
    onApriProvino: (Int) -> Unit
) {
    LaunchedEffect(Unit) {
        // Una sola chiamata HTTP invece di 4: statistiche + categorie + date + elenco
        proviniViewModel.caricaDashboard()
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = onIndietro) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Indietro",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    },
                    title = {
                        Text(
                            "Provini",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Statistiche panoramica
            val stats = proviniViewModel.statistiche
            if (stats != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CardStat(
                        etichetta = "Totale",
                        valore = stats.totaleProvini.toString(),
                        colore = SampColors.Blu,
                        modifier = Modifier.weight(1f)
                    )
                    CardStat(
                        etichetta = "Giocatori",
                        valore = "${stats.giocatoriConProvino}/${stats.totaleGiocatori}",
                        colore = SampColors.Success,
                        modifier = Modifier.weight(1f)
                    )
                    CardStat(
                        etichetta = "Media",
                        valore = String.format("%.2f", stats.mediaPerGiocatore),
                        colore = SampColors.Rosso,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Filtri categoria
            SezioneFiltro(
                etichetta = "Categoria",
                icona = Icons.Filled.Category,
                opzioni = proviniViewModel.categorieDisponibili,
                selezionato = proviniViewModel.filtroCategoria,
                onSelezione = { proviniViewModel.impostaFiltroCategoria(it) }
            )

            // Filtri data
            SezioneFiltro(
                etichetta = "Data",
                icona = Icons.Filled.CalendarToday,
                opzioni = proviniViewModel.dateDisponibili,
                selezionato = proviniViewModel.filtroData,
                onSelezione = { proviniViewModel.impostaFiltroData(it) },
                formattaOpzione = { formattaDataItaliana(it) }
            )

            Spacer(Modifier.height(4.dp))

            // Elenco provini
            when {
                proviniViewModel.caricamento -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = SampColors.Blu)
                }
                proviniViewModel.proviniDashboard.isEmpty() -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.Assignment,
                            contentDescription = null,
                            tint = SampColors.TestoMuto,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Nessun provino trovato",
                            color = SampColors.TestoSecondario
                        )
                        if (proviniViewModel.filtroCategoria != null || proviniViewModel.filtroData != null) {
                            Text(
                                "Prova a modificare i filtri",
                                style = MaterialTheme.typography.bodySmall,
                                color = SampColors.TestoMuto
                            )
                        }
                    }
                }
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 16.dp, vertical = 8.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(proviniViewModel.proviniDashboard, key = { it.id }) { provino ->
                        CardProvinoDashboard(
                            provino = provino,
                            onClick = { onApriProvino(provino.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CardStat(
    etichetta: String,
    valore: String,
    colore: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colore.copy(alpha = 0.08f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = valore,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = colore
            )
            Text(
                text = etichetta,
                style = MaterialTheme.typography.labelSmall,
                color = SampColors.TestoSecondario
            )
        }
    }
}

@Composable
private fun SezioneFiltro(
    etichetta: String,
    icona: androidx.compose.ui.graphics.vector.ImageVector,
    opzioni: List<String>,
    selezionato: String?,
    onSelezione: (String?) -> Unit,
    formattaOpzione: (String) -> String = { it }
) {
    if (opzioni.isEmpty()) return
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icona,
                contentDescription = null,
                tint = SampColors.Blu,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                etichetta,
                style = MaterialTheme.typography.labelMedium,
                color = SampColors.TestoSecondario,
                fontWeight = FontWeight.Bold
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = selezionato == null,
                onClick = { onSelezione(null) },
                label = { Text("Tutte") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = SampColors.Blu,
                    selectedLabelColor = Color.White
                )
            )
            opzioni.forEach { op ->
                FilterChip(
                    selected = selezionato == op,
                    onClick = { onSelezione(op) },
                    label = { Text(formattaOpzione(op)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SampColors.Blu,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }
    }
}

@Composable
private fun CardProvinoDashboard(provino: Provino, onClick: () -> Unit) {
    val (coloreBadge, testoBadge) = when (provino.presenza) {
        "Presente" -> SampColors.Success to "Presente"
        "Assente giustificato" -> SampColors.Warning to "Giustificato"
        "Assente" -> SampColors.ErrorColor to "Assente"
        else -> SampColors.TestoMuto to "Da compilare"
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Nome giocatore
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = SampColors.Blu,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "${provino.cognome ?: ""} ${provino.nome ?: ""}".trim()
                            .ifBlank { "Giocatore #${provino.personaId}" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SampColors.Blu
                    )
                }
                // Data + ora + categoria
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CalendarToday,
                        contentDescription = null,
                        tint = SampColors.TestoSecondario,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        text = formattaDataItaliana(provino.data),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                    if (!provino.ora.isNullOrBlank()) {
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Filled.Schedule,
                            contentDescription = null,
                            tint = SampColors.TestoSecondario,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(Modifier.width(3.dp))
                        Text(provino.ora, style = MaterialTheme.typography.bodySmall)
                    }
                    if (!provino.categoria.isNullOrBlank()) {
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(SampColors.BluNebbia, RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = provino.categoria,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = SampColors.Blu
                            )
                        }
                    }
                }
                if (!provino.impianto.isNullOrBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = SampColors.TestoMuto,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(Modifier.width(3.dp))
                        Text(
                            text = provino.impianto,
                            style = MaterialTheme.typography.labelSmall,
                            color = SampColors.TestoSecondario
                        )
                    }
                }
                if (provino.giudizio != null) {
                    Text(
                        text = "Giudizio: ${provino.giudizio}/10",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = SampColors.Blu,
                        modifier = Modifier.padding(top = 3.dp)
                    )
                }
            }

            // Badge presenza
            Box(
                modifier = Modifier
                    .background(coloreBadge, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = testoBadge,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

/** Trasforma "2026-07-24" in "24/07/2026" */
private fun formattaDataItaliana(data: String?): String {
    if (data.isNullOrBlank()) return "-"
    val parti = data.split("-")
    return if (parti.size == 3) "${parti[2]}/${parti[1]}/${parti[0]}" else data
}
