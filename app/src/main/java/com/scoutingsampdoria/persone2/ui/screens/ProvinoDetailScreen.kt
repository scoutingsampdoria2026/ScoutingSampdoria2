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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
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
import androidx.compose.material3.OutlinedButton
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
import com.scoutingsampdoria.persone2.ui.theme.SampColors
import com.scoutingsampdoria.persone2.viewmodel.ProviniViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProvinoDetailScreen(
    provinoId: Int,
    proviniViewModel: ProviniViewModel,
    onIndietro: () -> Unit
) {
    LaunchedEffect(provinoId) {
        proviniViewModel.caricaDettaglio(provinoId)
    }

    val provino = proviniViewModel.provinoCorrente

    // Stato locale editabile — si sincronizza col provino quando questo cambia
    var presenza by remember { mutableStateOf<String?>(null) }
    var giudizio by remember { mutableStateOf<Int?>(null) }
    var note by remember { mutableStateOf("") }
    var inizializzato by remember { mutableStateOf(false) }

    LaunchedEffect(provino) {
        if (provino != null && !inizializzato) {
            presenza = provino.presenza
            giudizio = provino.giudizio
            note = provino.note ?: ""
            inizializzato = true
        }
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
                        Column {
                            Text(
                                text = "Provino",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (provino != null) {
                                Text(
                                    text = "${provino.cognome ?: ""} ${provino.nome ?: ""}".trim(),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                                )
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
        if (provino == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = SampColors.Blu)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Sezione informazioni convocazione (readonly)
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SampColors.BluNebbia),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            "CONVOCAZIONE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SampColors.Blu
                        )
                        Spacer(Modifier.height(8.dp))
                        RigaInfo(Icons.Filled.CalendarToday, "Data",
                            formattaDataItaliana(provino.data))
                        RigaInfo(Icons.Filled.Schedule, "Ora",
                            provino.ora ?: "-")
                        RigaInfo(Icons.Filled.LocationOn, "Impianto",
                            provino.impianto ?: "-")
                        val squadre = listOfNotNull(provino.squadraCasa, provino.squadraOspite)
                            .joinToString(" vs ")
                        if (squadre.isNotBlank()) {
                            Text(
                                text = squadre,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = SampColors.Nero,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                        provino.categoria?.let {
                            Text(
                                text = "Categoria: $it",
                                style = MaterialTheme.typography.labelSmall,
                                color = SampColors.TestoSecondario
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Selettore presenza
                SelettorePresenza(
                    valore = presenza,
                    onValoreCambiato = { presenza = it }
                )

                Spacer(Modifier.height(12.dp))

                // Selettore giudizio
                SelettoreGiudizio(
                    valore = giudizio,
                    onValoreCambiato = { giudizio = it }
                )

                Spacer(Modifier.height(12.dp))

                // Note
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note") },
                    placeholder = { Text("Osservazioni sulla prestazione...") },
                    minLines = 4,
                    maxLines = 8,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = {
                        proviniViewModel.aggiornaProvino(
                            provinoId = provino.id,
                            presenza = presenza,
                            giudizio = giudizio,
                            note = note.ifBlank { null },
                            onCompletato = { }
                        )
                    },
                    enabled = !proviniViewModel.caricamento,
                    colors = ButtonDefaults.buttonColors(containerColor = SampColors.Blu),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Save, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(4.dp))
                    Text("Salva provino", color = Color.White, fontWeight = FontWeight.Bold)
                }

                proviniViewModel.messaggio?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = it,
                        color = SampColors.Success,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
                proviniViewModel.errore?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun RigaInfo(icona: androidx.compose.ui.graphics.vector.ImageVector,
                     etichetta: String, valore: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Icon(
            imageVector = icona, contentDescription = null,
            tint = SampColors.Blu, modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = "$etichetta:",
            style = MaterialTheme.typography.bodySmall,
            color = SampColors.TestoSecondario,
            modifier = Modifier.width(70.dp)
        )
        Text(
            text = valore,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SelettorePresenza(valore: String?, onValoreCambiato: (String?) -> Unit) {
    val opzioni = listOf("Assente", "Assente giustificato", "Presente")
    var menuAperto by remember { mutableStateOf(false) }

    val (colore, testoMostrato) = when (valore) {
        "Presente" -> SampColors.Success to "Presente"
        "Assente giustificato" -> SampColors.Warning to "Assente giustificato"
        "Assente" -> SampColors.ErrorColor to "Assente"
        else -> SampColors.TestoMuto to "Non compilato"
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "PRESENZA",
            style = MaterialTheme.typography.labelMedium,
            color = SampColors.TestoSecondario,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(4.dp))
        Box {
            OutlinedButton(
                onClick = { menuAperto = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(colore, CircleShape)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = testoMostrato,
                    fontWeight = FontWeight.Bold,
                    color = SampColors.Nero,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Start
                )
                Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
            }
            DropdownMenu(expanded = menuAperto, onDismissRequest = { menuAperto = false }) {
                DropdownMenuItem(
                    text = { Text("— Nessuno —", fontWeight = FontWeight.Bold,
                        color = SampColors.TestoMuto) },
                    onClick = {
                        onValoreCambiato(null); menuAperto = false
                    }
                )
                opzioni.forEach { op ->
                    val col = when (op) {
                        "Presente" -> SampColors.Success
                        "Assente giustificato" -> SampColors.Warning
                        else -> SampColors.ErrorColor
                    }
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(col, CircleShape)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(op, fontWeight = if (op == valore) FontWeight.Bold else FontWeight.Normal)
                            }
                        },
                        onClick = {
                            onValoreCambiato(op); menuAperto = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SelettoreGiudizio(valore: Int?, onValoreCambiato: (Int?) -> Unit) {
    var menuAperto by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "GIUDIZIO (0-10)",
            style = MaterialTheme.typography.labelMedium,
            color = SampColors.TestoSecondario,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(4.dp))
        Box {
            OutlinedButton(
                onClick = { menuAperto = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = valore?.toString() ?: "Non compilato",
                    fontWeight = FontWeight.Bold,
                    color = if (valore != null) SampColors.Blu else SampColors.TestoMuto,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Start
                )
                Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
            }
            DropdownMenu(expanded = menuAperto, onDismissRequest = { menuAperto = false }) {
                DropdownMenuItem(
                    text = { Text("— Nessuno —", fontWeight = FontWeight.Bold,
                        color = SampColors.TestoMuto) },
                    onClick = {
                        onValoreCambiato(null); menuAperto = false
                    }
                )
                (0..10).forEach { n ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                "$n / 10",
                                fontWeight = if (n == valore) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        onClick = {
                            onValoreCambiato(n); menuAperto = false
                        }
                    )
                }
            }
        }
    }
}

