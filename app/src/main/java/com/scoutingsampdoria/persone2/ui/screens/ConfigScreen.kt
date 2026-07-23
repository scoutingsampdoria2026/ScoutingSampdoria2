package com.scoutingsampdoria.persone2.ui.screens

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.scoutingsampdoria.persone2.data.repository.BackupManager
import com.scoutingsampdoria.persone2.data.repository.RisultatoBackup
import com.scoutingsampdoria.persone2.ui.theme.SampColors
import com.scoutingsampdoria.persone2.viewmodel.AuthViewModel
import com.scoutingsampdoria.persone2.viewmodel.ConfigViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigScreen(
    configViewModel: ConfigViewModel,
    authViewModel: AuthViewModel,
    onIndietro: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val backupManager = remember { BackupManager(context) }

    LaunchedEffect(Unit) { configViewModel.caricaTutto() }

    var mostraDialogNuovoCampo by remember { mutableStateOf(false) }
    var mostraCodicePerRipristino by remember { mutableStateOf(false) }
    var mostraCodicePerSvuota by remember { mutableStateOf(false) }
    var mostraDialogLog by remember { mutableStateOf(false) }
    var mostraDialogCambioCodice by remember { mutableStateOf(false) }

    // Stato locale per feedback backup
    var cartellaSelezionata by remember { mutableStateOf<android.net.Uri?>(null) }
    var descrizioneUltimoBackup by remember { mutableStateOf<String?>(null) }
    var backupInCorso by remember { mutableStateOf(false) }
    var esitoBackup by remember { mutableStateOf<String?>(null) }
    var uriBackupDaRipristinare by remember { mutableStateOf<android.net.Uri?>(null) }

    // Ricarico stato backup all'apertura
    LaunchedEffect(Unit) {
        cartellaSelezionata = backupManager.cartellaDestinazione()
        descrizioneUltimoBackup = backupManager.descrizioneUltimoBackup()
    }

    // Launcher SAF per scegliere cartella (una tantum)
    val launcherSceltaCartella = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                backupManager.impostaCartellaDestinazione(uri)
                cartellaSelezionata = uri
                esitoBackup = "Cartella salvata"
            }
        }
    }

    // Launcher SAF per selezionare un .db da ripristinare
    val launcherApriBackup = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            uriBackupDaRipristinare = uri
            mostraCodicePerRipristino = true
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

            // Sezione backup su cloud
            SezioneConfig(titolo = "Backup su cloud") {
                Text("Salva il database in una cartella (es. Google Drive) e ripristina all'occorrenza. " +
                    "Il backup automatico gira ogni giorno se il tablet è online.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SampColors.TestoSecondario)
                Spacer(Modifier.height(10.dp))

                // Stato cartella
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (cartellaSelezionata != null) {
                        Icon(Icons.Filled.CloudDone, contentDescription = null,
                            tint = SampColors.Success)
                        Spacer(Modifier.width(6.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Cartella configurata",
                                style = MaterialTheme.typography.labelMedium,
                                color = SampColors.Success, fontWeight = FontWeight.Bold)
                            descrizioneUltimoBackup?.let {
                                Text(it, style = MaterialTheme.typography.labelSmall,
                                    color = SampColors.TestoMuto)
                            }
                        }
                    } else {
                        Icon(Icons.Filled.CloudOff, contentDescription = null,
                            tint = SampColors.Warning)
                        Spacer(Modifier.width(6.dp))
                        Text("Nessuna cartella selezionata",
                            style = MaterialTheme.typography.labelMedium,
                            color = SampColors.Warning, fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f))
                    }
                }

                Spacer(Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { launcherSceltaCartella.launch(null) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Folder, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text(if (cartellaSelezionata == null) "Scegli cartella backup"
                         else "Cambia cartella")
                }

                Spacer(Modifier.height(4.dp))

                Button(
                    onClick = {
                        backupInCorso = true
                        esitoBackup = null
                        scope.launch {
                            when (val r = backupManager.eseguiBackup()) {
                                is RisultatoBackup.Successo -> {
                                    esitoBackup = "Backup creato: ${r.nomeFile}"
                                    descrizioneUltimoBackup = backupManager.descrizioneUltimoBackup()
                                }
                                is RisultatoBackup.Errore -> esitoBackup = "Errore: ${r.messaggio}"
                            }
                            backupInCorso = false
                        }
                    },
                    enabled = cartellaSelezionata != null && !backupInCorso,
                    colors = ButtonDefaults.buttonColors(containerColor = SampColors.Success),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (backupInCorso) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Backup in corso...", color = Color.White)
                    } else {
                        Icon(Icons.Filled.Backup, contentDescription = null, tint = Color.White)
                        Spacer(Modifier.width(4.dp))
                        Text("Esegui backup ora", color = Color.White)
                    }
                }

                Spacer(Modifier.height(4.dp))

                OutlinedButton(
                    onClick = {
                        // Filtro per file .db (uso mimetype generico + estensione)
                        launcherApriBackup.launch(arrayOf("application/octet-stream", "*/*"))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Restore, contentDescription = null,
                        tint = SampColors.Warning)
                    Spacer(Modifier.width(4.dp))
                    Text("Ripristina da backup", color = SampColors.Warning)
                }

                esitoBackup?.let {
                    Spacer(Modifier.height(6.dp))
                    Text(it, style = MaterialTheme.typography.labelSmall,
                        color = if (it.startsWith("Errore")) SampColors.ErrorColor
                                else SampColors.Success)
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
                    singleLine = true, modifier = Modifier.fillMaxWidth()
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
            descrizione = "Il ripristino sovrascrive TUTTI i dati attuali. Inserisci il codice per confermare.",
            authViewModel = authViewModel,
            onAnnulla = {
                mostraCodicePerRipristino = false
                uriBackupDaRipristinare = null
            },
            onConfermato = {
                mostraCodicePerRipristino = false
                val uri = uriBackupDaRipristinare
                uriBackupDaRipristinare = null
                if (uri != null) {
                    scope.launch {
                        when (val r = backupManager.ripristinaBackup(uri)) {
                            is RisultatoBackup.Successo ->
                                esitoBackup = "Ripristino completato. Riavvia l'app per applicare."
                            is RisultatoBackup.Errore ->
                                esitoBackup = "Errore ripristino: ${r.messaggio}"
                        }
                    }
                }
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
                        Text("Nessuna operazione registrata.", color = SampColors.TestoMuto)
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
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                errore?.let {
                    Text(it, color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                authViewModel.tentaSblocco(codice)
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
