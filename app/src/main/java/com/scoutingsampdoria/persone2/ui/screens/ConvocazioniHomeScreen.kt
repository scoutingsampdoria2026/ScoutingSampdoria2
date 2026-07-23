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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EventNote
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.scoutingsampdoria.persone2.ui.theme.SampColors
import com.scoutingsampdoria.persone2.viewmodel.ConvocazioniViewModel
import com.scoutingsampdoria.persone2.viewmodel.PersoneViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConvocazioniHomeScreen(
    personeViewModel: PersoneViewModel,
    convocazioniViewModel: ConvocazioniViewModel,
    onIndietro: () -> Unit,
    onSelezionaCategoria: (String) -> Unit,
) {
    LaunchedEffect(Unit) { personeViewModel.caricaLista() }
    LaunchedEffect(personeViewModel.persone) {
        convocazioniViewModel.caricaCategorieDaPersone(personeViewModel.persone)
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
                    title = { Text("Convocazioni", fontWeight = FontWeight.Bold) },
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
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("SELEZIONA CATEGORIA",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = SampColors.TestoSecondario)
            Spacer(Modifier.height(8.dp))

            when {
                personeViewModel.caricamento -> Box(Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SampColors.Blu)
                }
                convocazioniViewModel.categorieDisponibili.isEmpty() ->
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.EventNote, contentDescription = null,
                                tint = SampColors.TestoMuto, modifier = Modifier.size(64.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("Nessuna categoria disponibile",
                                color = SampColors.TestoSecondario)
                            Text("Aggiungi CATEGORIA ai giocatori",
                                style = MaterialTheme.typography.bodySmall,
                                color = SampColors.TestoMuto)
                        }
                    }
                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(convocazioniViewModel.categorieDisponibili) { categoria ->
                            val giocatoriInCategoria = personeViewModel.persone
                                .count { it.extra?.get("CATEGORIA") == categoria }
                            CardCategoria(categoria, giocatoriInCategoria) {
                                onSelezionaCategoria(categoria)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CardCategoria(categoria: String, giocatori: Int, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(48.dp)
                    .background(SampColors.BluNebbia, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.EventNote, contentDescription = null,
                    tint = SampColors.Blu, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(categoria, style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold, color = SampColors.Blu)
                Text("$giocatori giocatori disponibili",
                    style = MaterialTheme.typography.bodySmall,
                    color = SampColors.TestoSecondario)
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = SampColors.TestoMuto)
        }
    }
}
