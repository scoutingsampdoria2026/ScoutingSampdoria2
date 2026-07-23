package com.scoutingsampdoria.persone2.ui.screens

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import com.scoutingsampdoria.persone2.ui.theme.SampColors
import com.scoutingsampdoria.persone2.viewmodel.AuthViewModel
import com.scoutingsampdoria.persone2.viewmodel.ConfigViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigScreen(
    configViewModel: ConfigViewModel,
    authViewModel: AuthViewModel,
    onIndietro: () -> Unit,
    // Callback verso chi gestisce backup/ripristino/import (li useremo per lanciare i file picker)
    onEsportaBackup: () -> Unit,
    onImportaBackup: (androidx.activity.result.ActivityResultLauncher<Intent>) -> Unit,
    onImportaXlsx: (androidx.activity.result.ActivityResultLauncher<Intent>) -> Unit,
    infoUltimoBackup: String? = null,
) {
    LaunchedEffect(Unit) { configViewModel.caricaTutto() }

    var mostraDialogNuovoCampo by remember { mutableStateOf(false) }
    var mostraCodicePerRipristino by remember { mutableStateOf(false) }
    var mostraCodicePerSvuota by remember { mutableStateOf(false) }
    var mostraDialogLog by remember { mutableStateOf(false) }
    var mostraDialogCambioCodice by remember { mutableStateOf(false) }

    // Launcher file picker per ripristino
    val launcherRipristino = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { /* handled via callback */ }

    // Launcher file picker per import xlsx
    val launcherImportXlsx = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { /* handled via callback */ }

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
                    title = { Text("Configurazione", fontWeight = FontWeight.Bold) },
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
            modifier = Modifier.fillMaxSize().padding(padding)
                .verticalScroll(rememberScrollState()).padding(16.dp)
        ) {
            // Sezione campi custom
            SezioneConfig(titolo = "Campi personalizzati") {
                Text("Aggiungi colonne opzionali per ogni giocatore (es. CATEGORIA, RATING).",
                    style = MaterialTheme.typography.bodySmall,
                    color = SampColors.TestoSecondario)
                Spacer(Modifier.height(8.dp))
                configViewModel.campiCustom.forEach { campo ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(campo.nome, modifier = Modifier.weight(1f),
                            fontWeight = FontWeight.Medium)
                        val protetto = campo.nome in listOf("CATEGORIA", "RATING")
                        if (protetto) {
                            Icon(Icons.Filled.Lock, contentDescription = "Protetto",
                                tint = SampColors.TestoMuto, modifier = Modifier.size(18.dp))
                        } else {
                            IconButton(onClick = { configViewModel.eliminaCampo(campo.nome) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Elimina",
                                    tint = SampColors.ErrorColor)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                OutlinedButton(
                    onClick = { mostraDialogNuovoCampo = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Aggiungi campo")
                }
            }

            Spacer(Modifier.height(12.dp))

            // Sezione import dati
            SezioneConfig(titolo = "Importa dati") {
                Text("Importa un file XLSX (formato dell'app 1.0). Colonne extra verranno registrate come campi custom.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SampColors.TestoSecondario)
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { onImportaXlsx(launcherImportXlsx) },
                    colors = ButtonDefaults.buttonColors(containerColor = SampColors.Info),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.UploadFile, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(4.dp))
                    Text("Importa da XLSX", color = Color.White)
                }
            }

            Spacer(Modifier.height(12.dp))

            // Sezione backup
            SezioneConfig(titolo = "Backup e ripristino") {
                Text("Salva o ripristina l'intero database.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SampColors.TestoSecondario)
                infoUltimoBackup?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(it, style = MaterialTheme.typography.labelSmall,
                        color = SampColors.TestoMuto)
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onEsportaBackup,
                    colors = ButtonDefaults.buttonColors(containerColor = SampColors.Success),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Backup, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(4.dp))
                    Text("Esporta backup", color = Color.White)
                }
                Spacer(Modifier.height(4.dp))
                OutlinedButton(
                    onClick = { mostraCodicePerRipristino = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Restore, contentDescription = null,
                        tint = SampColors.Warning)
                    Spacer(Modifier.width(4.dp))
                    Text("Ripristina backup", color = SampColors.Warning)
                }
            }

            Spacer(Modifier.height(12.dp))

            // Sezione sicurezza
            SezioneConfig(titolo = "Sicurezza") {
                OutlinedButton(
                    onClick = { mostraDialogCambioCodice = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Lock, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Cambia codice di sblocco")
                }
            }

            Spacer(Modifier.height(12.dp))

            // Sezione log
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SampColors.BluNebbia),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                onClick = { mostraDialogLog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.History, contentDescription = null, tint = SampColors.Blu)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Log operazioni",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold, color = SampColors.Blu)
                        Text("${configViewModel.logs.size} eventi registrati",
                            style = MaterialTheme.typography.bodySmall,
                            color = SampColors.TestoSecondario)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Zona pericolo
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SampColors.RossoSoft),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Warning, contentDescription = null, tint = SampColors.Rosso)
                        Spacer(Modifier.width(8.dp))
                        Text("ZONA PERICOLO",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold, color = SampColors.Rosso)
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { mostraCodicePerSvuota = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.DeleteForever, contentDescription = null,
                            tint = SampColors.Rosso)
                        Spacer(Modifier.width(4.dp))
                        Text("Svuota database", color = SampColors.Rosso)
                    }
                }
            }

            configViewModel.messaggio?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, color = SampColors.Success)
            }
            configViewModel.errore?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }
        }
    }

    // === DIALOGHI ===

    if (mostraDialogNuovoCampo) {
        var nome by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { mostraDialogNuovoCampo = false },
            title = { Text("Nuovo campo personalizzato") },
            text = {
                OutlinedTextField(
                    value = nome, onValueChange = { nome = it },
                    label = { Text("Nome (es. AGENTE)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    configViewModel.aggiungiCampo(nome)
                    mostraDialogNuovoCampo = false
                }, enabled = nome.isNotBlank()) { Text("Aggiungi") }
            },
            dismissButton = {
                TextButton(onClick = { mostraDialogNuovoCampo = false }) { Text("Annulla") }
            }
        )
    }

    if (mostraCodicePerRipristino) {
        DialogCodiceProtezione(
            titolo = "Ripristina backup",
            descrizione = "Il ripristino sovrascrive tutti i dati attuali. Inserisci il codice per confermare.",
            authViewModel = authViewModel,
            onAnnulla = { mostraCodicePerRipristino = false },
            onConfermato = {
                mostraCodicePerRipristino = false
                onImportaBackup(launcherRipristino)
            }
        )
    }

    if (mostraCodicePerSvuota) {
        DialogCodiceProtezione(
            titolo = "Svuota database",
            descrizione = "Questa operazione elimina TUTTI i giocatori. Inserisci il codice per confermare.",
            authViewModel = authViewModel,
            onAnnulla = { mostraCodicePerSvuota = false },
            onConfermato = {
                mostraCodicePerSvuota = false
                configViewModel.svuotaDatabase { }
            }
        )
    }

    if (mostraDialogLog) {
        AlertDialog(
            onDismissRequest = { mostraDialogLog = false },
            title = { Text("Log operazioni") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    if (configViewModel.logs.isEmpty()) {
                        Text("Nessuna operazione registrata.",
                            color = SampColors.TestoMuto)
                    } else {
                        configViewModel.logs.forEach { log ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                                Text(log.tipo, fontWeight = FontWeight.Bold,
                                    color = SampColors.Blu, modifier = Modifier.width(80.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(log.dettaglio ?: "-",
                                        style = MaterialTheme.typography.bodySmall)
                                    log.creatoIl?.let {
                                        Text(it, style = MaterialTheme.typography.labelSmall,
                                            color = SampColors.TestoMuto)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { mostraDialogLog = false }) { Text("Chiudi") }
            }
        )
    }

    if (mostraDialogCambioCodice) {
        DialogCambioCodice(
            authViewModel = authViewModel,
            onChiudi = { mostraDialogCambioCodice = false }
        )
    }
}

@Composable
private fun SezioneConfig(titolo: String, content: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(titolo, style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold, color = SampColors.Blu)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun DialogCodiceProtezione(
    titolo: String,
    descrizione: String,
    authViewModel: AuthViewModel,
    onAnnulla: () -> Unit,
    onConfermato: () -> Unit,
) {
    var codice by remember { mutableStateOf("") }
    var errore by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onAnnulla,
        title = { Text(titolo, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(descrizione, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = codice,
                    onValueChange = {
                        codice = it.filter { c -> c.isDigit() }.take(10)
                        errore = null
                    },
                    label = { Text("Codice") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                errore?.let {
                    Text(it, color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                // Verifico il codice usando authViewModel: creo un tentativo temporaneo
                authViewModel.tentaSblocco(codice)
                // NOTA: authViewModel.sbloccato è già true (siamo dentro l'app),
                // quindi controllo se il codice è quello corrente. Per semplicità:
                if (authViewModel.errore == null) {
                    onConfermato()
                } else {
                    errore = "Codice errato"
                    authViewModel.pulisciErrore()
                }
            }, enabled = codice.isNotBlank()) { Text("Conferma") }
        },
        dismissButton = {
            TextButton(onClick = onAnnulla) { Text("Annulla") }
        }
    )
}

@Composable
private fun DialogCambioCodice(
    authViewModel: AuthViewModel,
    onChiudi: () -> Unit,
) {
    var vecchio by remember { mutableStateOf("") }
    var nuovo by remember { mutableStateOf("") }
    var conferma by remember { mutableStateOf("") }
    var errore by remember { mutableStateOf<String?>(null) }
    var successo by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onChiudi,
        title = { Text("Cambia codice di sblocco", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                if (successo) {
                    Text("Codice aggiornato con successo.", color = SampColors.Success)
                } else {
                    OutlinedTextField(
                        value = vecchio, onValueChange = { vecchio = it.filter { c -> c.isDigit() } },
                        label = { Text("Codice attuale") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true, modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = nuovo, onValueChange = { nuovo = it.filter { c -> c.isDigit() } },
                        label = { Text("Nuovo codice (min 4 cifre)") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true, modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = conferma, onValueChange = { conferma = it.filter { c -> c.isDigit() } },
                        label = { Text("Ripeti nuovo codice") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true, modifier = Modifier.fillMaxWidth()
                    )
                    errore?.let {
                        Text(it, color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        },
        confirmButton = {
            if (successo) {
                TextButton(onClick = onChiudi) { Text("Chiudi") }
            } else {
                Button(onClick = {
                    if (nuovo != conferma) { errore = "I due codici non coincidono"; return@Button }
                    if (nuovo.length < 4) { errore = "Il codice deve avere almeno 4 cifre"; return@Button }
                    // Verifica codice vecchio
                    authViewModel.tentaSblocco(vecchio)
                    if (authViewModel.errore != null) {
                        errore = "Codice attuale errato"
                        authViewModel.pulisciErrore()
                        return@Button
                    }
                    authViewModel.cambiaCodice(nuovo) { ok ->
                        if (ok) successo = true
                        else errore = "Errore durante il cambio codice"
                    }
                }) { Text("Cambia") }
            }
        },
        dismissButton = if (!successo) {
            { TextButton(onClick = onChiudi) { Text("Annulla") } }
        } else null
    )
}
