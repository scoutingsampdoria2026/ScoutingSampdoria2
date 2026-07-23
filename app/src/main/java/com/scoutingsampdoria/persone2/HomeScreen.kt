package com.scoutingsampdoria.persone2.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.scoutingsampdoria.persone2.BuildConfig
import com.scoutingsampdoria.persone2.R
import com.scoutingsampdoria.persone2.ui.theme.SampColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onGestioneGiocatori: () -> Unit,
    onConvocazioni: () -> Unit,
    onConfigurazione: () -> Unit,
    onBlocca: () -> Unit,
) {
    var menuImpostazioniAperto by remember { mutableStateOf(false) }
    var mostraDialogHelp by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    navigationIcon = {
                        Box(
                            modifier = Modifier.padding(start = 12.dp).size(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.logo_sampdoria),
                                contentDescription = "Logo",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    },
                    title = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Scouting Sampdoria 2.0",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "offline · v${BuildConfig.VERSION_NAME}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center
                            )
                        }
                    },
                    actions = {
                        Box {
                            IconButton(onClick = { menuImpostazioniAperto = true }) {
                                Icon(Icons.Filled.Settings, contentDescription = "Impostazioni",
                                    tint = MaterialTheme.colorScheme.onPrimary)
                            }
                            DropdownMenu(
                                expanded = menuImpostazioniAperto,
                                onDismissRequest = { menuImpostazioniAperto = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Configurazione") },
                                    leadingIcon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                                    onClick = {
                                        menuImpostazioniAperto = false
                                        onConfigurazione()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Help") },
                                    leadingIcon = { Icon(Icons.Filled.Info, contentDescription = null) },
                                    onClick = {
                                        menuImpostazioniAperto = false
                                        mostraDialogHelp = true
                                    }
                                )
                            }
                        }
                        IconButton(onClick = onBlocca) {
                            Icon(Icons.Filled.Lock, contentDescription = "Blocca",
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
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SampColors.BluNebbia),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("BENVENUTO", style = MaterialTheme.typography.labelSmall,
                        color = SampColors.Blu, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "U.C. Sampdoria - Scouting giovani calciatori",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = SampColors.Blu
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Seleziona un modulo per iniziare",
                        style = MaterialTheme.typography.bodySmall,
                        color = SampColors.TestoSecondario
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Text("MODULI DISPONIBILI", style = MaterialTheme.typography.labelSmall,
                color = SampColors.TestoSecondario, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))

            PulsanteModulo(
                titolo = "Gestione Giocatori",
                descrizione = "Consulta, filtra e gestisci il database dei giocatori",
                icona = Icons.Filled.Groups,
                coloreIcona = SampColors.Blu,
                onClick = onGestioneGiocatori
            )
            PulsanteModulo(
                titolo = "Convocazioni",
                descrizione = "Prepara convocazioni per le partite con distinta e disposizione tattica",
                icona = Icons.Filled.EventNote,
                coloreIcona = SampColors.Rosso,
                onClick = onConvocazioni
            )
        }
    }

    if (mostraDialogHelp) {
        AlertDialog(
            onDismissRequest = { mostraDialogHelp = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Info, contentDescription = null, tint = SampColors.Blu)
                    Spacer(Modifier.width(8.dp))
                    Text("Informazioni", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text("Scouting Sampdoria 2.0", fontWeight = FontWeight.Bold,
                        color = SampColors.Blu, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text("Versione ${BuildConfig.VERSION_NAME}",
                        style = MaterialTheme.typography.bodyMedium)
                    Text("Modalità offline - DB locale",
                        style = MaterialTheme.typography.labelMedium, color = SampColors.TestoSecondario)
                    Spacer(Modifier.height(12.dp))
                    Text("App sviluppata da Di Vito Ruggero",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                TextButton(onClick = { mostraDialogHelp = false }) { Text("Chiudi") }
            }
        )
    }
}

@Composable
private fun PulsanteModulo(
    titolo: String,
    descrizione: String,
    icona: ImageVector,
    coloreIcona: Color,
    onClick: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = SampColors.Superficie),
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(48.dp)
                    .background(coloreIcona.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icona, contentDescription = null, tint = coloreIcona,
                    modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(titolo, style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold, color = SampColors.Nero)
                Text(descrizione, style = MaterialTheme.typography.bodySmall,
                    color = SampColors.TestoSecondario)
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = SampColors.TestoMuto)
        }
    }
}
