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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.material.icons.filled.ArrowBack
import com.scoutingsampdoria.persone2.data.model.Provino
import com.scoutingsampdoria.persone2.ui.theme.SampColors
import com.scoutingsampdoria.persone2.viewmodel.PersoneViewModel
import com.scoutingsampdoria.persone2.viewmodel.ProviniViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElencoProviniScreen(
    personaId: Int,
    personeViewModel: PersoneViewModel,
    proviniViewModel: ProviniViewModel,
    onIndietro: () -> Unit,
    onApriProvino: (Int) -> Unit
) {
    LaunchedEffect(personaId) {
        proviniViewModel.caricaProviniPersona(personaId)
        personeViewModel.caricaDettaglio(personaId)
    }

    val persona = personeViewModel.personaSelezionata
    val titoloScheda = if (persona != null) "${persona.cognome} ${persona.nome}" else "Provini"

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
                        Column {
                            Text(
                                text = "Provini",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = titoloScheda,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                            )
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            when {
                proviniViewModel.caricamento -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = SampColors.Blu)
                }
                proviniViewModel.provini.isEmpty() -> Box(
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
                            "Nessun provino registrato",
                            color = SampColors.TestoSecondario
                        )
                        Text(
                            "I provini vengono creati quando il giocatore viene aggiunto ai convocati.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SampColors.TestoMuto,
                            modifier = Modifier.padding(top = 4.dp, start = 32.dp, end = 32.dp)
                        )
                    }
                }
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(proviniViewModel.provini, key = { it.id }) { provino ->
                        CardProvino(
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
private fun CardProvino(provino: Provino, onClick: () -> Unit) {
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
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Colonna sinistra: data e ora
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.CalendarToday,
                        contentDescription = null,
                        tint = SampColors.Blu,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = formattaDataItaliana(provino.data),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SampColors.Blu
                    )
                    Spacer(Modifier.width(12.dp))
                    Icon(
                        imageVector = Icons.Filled.Schedule,
                        contentDescription = null,
                        tint = SampColors.TestoSecondario,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = provino.ora ?: "-",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                if (!provino.impianto.isNullOrBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = SampColors.TestoMuto,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = provino.impianto,
                            style = MaterialTheme.typography.bodySmall,
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
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Colonna destra: badge presenza
            Column(horizontalAlignment = Alignment.End) {
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
}

/** Trasforma "2026-07-24" in "24/07/2026" */
private fun formattaDataItaliana(data: String?): String {
    if (data.isNullOrBlank()) return "-"
    val parti = data.split("-")
    return if (parti.size == 3) "${parti[2]}/${parti[1]}/${parti[0]}" else data
}
