package com.scoutingsampdoria.persone2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
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
import com.scoutingsampdoria.persone2.data.model.Convocazione
import com.scoutingsampdoria.persone2.ui.theme.SampColors
import com.scoutingsampdoria.persone2.viewmodel.ConvocazioniViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConvocazioniListaScreen(
    viewModel: ConvocazioniViewModel,
    categoria: String,
    onIndietro: () -> Unit,
    onApriConvocazione: (Int) -> Unit,
) {
    LaunchedEffect(categoria) { viewModel.caricaConvocazioniPerCategoria(categoria) }

    var mostraDialogNuova by remember { mutableStateOf(false) }
    var convocazioneDaEliminare by remember { mutableStateOf<Convocazione?>(null) }

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
                            Text("Convocazioni",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold)
                            Text(categoria,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f))
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
                onClick = { mostraDialogNuova = true },
                containerColor = SampColors.Rosso, contentColor = Color.White
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Nuova convocazione")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            when {
                viewModel.caricamento -> Box(Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SampColors.Blu)
                }
                viewModel.convocazioni.isEmpty() ->
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.EventNote, contentDescription = null,
                                tint = SampColors.TestoMuto, modifier = Modifier.size(64.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("Nessuna convocazione", color = SampColors.TestoSecondario)
                            Text("Tocca + per crearne una",
                                style = MaterialTheme.typography.bodySmall,
                                color = SampColors.TestoMuto)
                        }
                    }
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(viewModel.convocazioni, key = { it.id }) { c ->
                        CardConvocazione(
                            convocazione = c,
                            onClick = { onApriConvocazione(c.id) },
                            onElimina = { convocazioneDaEliminare = c }
                        )
                    }
                }
            }
        }
    }

    if (mostraDialogNuova) {
        DialogNuovaConvocazione(
            categoria = categoria,
            onAnnulla = { mostraDialogNuova = false },
            onCrea = { data, ora, oraConv, impianto, casa, ospite ->
                mostraDialogNuova = false
                viewModel.creaConvocazione(
                    categoria = categoria,
                    data = data.ifBlank { null },
                    ora = ora.ifBlank { null },
                    oraConvocazione = oraConv.ifBlank { null },
                    impianto = impianto.ifBlank { null },
                    squadraCasa = casa.ifBlank { null },
                    squadraOspite = ospite.ifBlank { null },
                    modulo = null,
                    onCreata = { id -> onApriConvocazione(id) }
                )
            }
        )
    }

    convocazioneDaEliminare?.let { conv ->
        AlertDialog(
            onDismissRequest = { convocazioneDaEliminare = null },
            title = { Text("Elimina convocazione") },
            text = {
                Text("Vuoi eliminare la convocazione del ${conv.data ?: "-"} ${conv.ora ?: ""}? " +
                    "L'operazione è irreversibile.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        val id = conv.id
                        convocazioneDaEliminare = null
                        viewModel.eliminaConvocazione(id, categoria) { }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SampColors.ErrorColor)
                ) { Text("Elimina", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { convocazioneDaEliminare = null }) { Text("Annulla") }
            }
        )
    }
}

@Composable
private fun CardConvocazione(convocazione: Convocazione, onClick: () -> Unit, onElimina: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CalendarToday, contentDescription = null,
                        tint = SampColors.Blu, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(formattaDataItaliana(convocazione.data),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold, color = SampColors.Blu)
                    Spacer(Modifier.width(12.dp))
                    Icon(Icons.Filled.Schedule, contentDescription = null,
                        tint = SampColors.Blu, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(convocazione.ora ?: "-", style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold)
                }
                convocazione.oraConvocazione?.let {
                    Text("Convocazione: $it", style = MaterialTheme.typography.labelSmall,
                        color = SampColors.TestoSecondario)
                }
                convocazione.impianto?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall)
                }
                val squadre = listOfNotNull(convocazione.squadraCasa, convocazione.squadraOspite)
                    .joinToString(" vs ")
                if (squadre.isNotBlank()) {
                    Text(squadre, style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium)
                }
            }
            IconButton(onClick = onElimina) {
                Icon(Icons.Filled.Delete, contentDescription = "Elimina",
                    tint = SampColors.ErrorColor)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DialogNuovaConvocazione(
    categoria: String,
    onAnnulla: () -> Unit,
    onCrea: (data: String, ora: String, oraConvocazione: String, impianto: String, casa: String, ospite: String) -> Unit
) {
    var data by remember { mutableStateOf("") }
    var ora by remember { mutableStateOf("") }
    var oraConvocazione by remember { mutableStateOf("") }
    var impianto by remember { mutableStateOf("") }
    var casa by remember { mutableStateOf("Sampdoria $categoria") }
    var ospite by remember { mutableStateOf("") }

    var mostraDatePicker by remember { mutableStateOf(false) }
    var mostraTimePickerPartita by remember { mutableStateOf(false) }
    var mostraTimePickerConvocazione by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState()
    val timePickerStatePartita = rememberTimePickerState(is24Hour = true)
    val timePickerStateConvocazione = rememberTimePickerState(is24Hour = true)

    AlertDialog(
        onDismissRequest = onAnnulla,
        title = { Text("Nuova convocazione", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text("Categoria: $categoria",
                    style = MaterialTheme.typography.labelMedium,
                    color = SampColors.Blu, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = if (data.isNotBlank()) formattaDataItaliana(data) else "",
                    onValueChange = { }, readOnly = true,
                    label = { Text("Data") },
                    placeholder = { Text("Tocca per selezionare") },
                    trailingIcon = {
                        IconButton(onClick = { mostraDatePicker = true }) {
                            Icon(Icons.Filled.CalendarToday, contentDescription = null)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().clickable { mostraDatePicker = true }
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = ora, onValueChange = { }, readOnly = true,
                    label = { Text("Ora partita") },
                    placeholder = { Text("Tocca per selezionare") },
                    trailingIcon = {
                        IconButton(onClick = { mostraTimePickerPartita = true }) {
                            Icon(Icons.Filled.Schedule, contentDescription = null)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().clickable { mostraTimePickerPartita = true }
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = oraConvocazione, onValueChange = { }, readOnly = true,
                    label = { Text("Ora convocazione") },
                    placeholder = { Text("Tocca per selezionare") },
                    trailingIcon = {
                        IconButton(onClick = { mostraTimePickerConvocazione = true }) {
                            Icon(Icons.Filled.Schedule, contentDescription = null)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().clickable { mostraTimePickerConvocazione = true }
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = impianto, onValueChange = { impianto = titleCase(it) },
                    label = { Text("Impianto") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = casa, onValueChange = { casa = titleCase(it) },
                    label = { Text("Squadra casa") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = ospite, onValueChange = { ospite = titleCase(it) },
                    label = { Text("Squadra ospite") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onCrea(data, ora, oraConvocazione, impianto, casa, ospite) },
                colors = ButtonDefaults.buttonColors(containerColor = SampColors.Blu)
            ) { Text("Crea", color = Color.White) }
        },
        dismissButton = {
            TextButton(onClick = onAnnulla) { Text("Annulla") }
        }
    )

    if (mostraDatePicker) {
        DatePickerDialog(
            onDismissRequest = { mostraDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val cal = java.util.Calendar.getInstance().apply { timeInMillis = millis }
                        val y = cal.get(java.util.Calendar.YEAR)
                        val m = (cal.get(java.util.Calendar.MONTH) + 1).toString().padStart(2, '0')
                        val d = cal.get(java.util.Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
                        data = "$y-$m-$d"
                    }
                    mostraDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { mostraDatePicker = false }) { Text("Annulla") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    if (mostraTimePickerPartita) {
        AlertDialog(
            onDismissRequest = { mostraTimePickerPartita = false },
            title = { Text("Ora partita") },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = timePickerStatePartita)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val h = timePickerStatePartita.hour.toString().padStart(2, '0')
                    val m = timePickerStatePartita.minute.toString().padStart(2, '0')
                    ora = "$h:$m"; mostraTimePickerPartita = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { mostraTimePickerPartita = false }) { Text("Annulla") }
            }
        )
    }
    if (mostraTimePickerConvocazione) {
        AlertDialog(
            onDismissRequest = { mostraTimePickerConvocazione = false },
            title = { Text("Ora convocazione") },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = timePickerStateConvocazione)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val h = timePickerStateConvocazione.hour.toString().padStart(2, '0')
                    val m = timePickerStateConvocazione.minute.toString().padStart(2, '0')
                    oraConvocazione = "$h:$m"; mostraTimePickerConvocazione = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { mostraTimePickerConvocazione = false }) { Text("Annulla") }
            }
        )
    }
}

/** Formatta una data ISO YYYY-MM-DD in DD/MM/YYYY */
internal fun formattaDataItaliana(data: String?): String {
    if (data.isNullOrBlank()) return "-"
    val parti = data.split("-")
    return if (parti.size == 3) "${parti[2]}/${parti[1]}/${parti[0]}" else data
}

/** Trasforma "sampdoria u13" -> "Sampdoria U13" */
internal fun titleCase(input: String): String {
    return input.split(" ").joinToString(" ") { parola ->
        if (parola.isEmpty()) parola
        else parola.substring(0, 1).uppercase() + parola.substring(1)
    }
}
