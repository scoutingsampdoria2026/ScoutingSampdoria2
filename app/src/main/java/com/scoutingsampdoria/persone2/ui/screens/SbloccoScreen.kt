package com.scoutingsampdoria.persone2.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.scoutingsampdoria.persone2.R
import com.scoutingsampdoria.persone2.ui.theme.SampColors
import com.scoutingsampdoria.persone2.viewmodel.AuthViewModel

@Composable
fun SbloccoScreen(
    viewModel: AuthViewModel,
    onSbloccato: () -> Unit,
) {
    var codice by remember { mutableStateOf("") }

    LaunchedEffect(viewModel.sbloccato) {
        if (viewModel.sbloccato) onSbloccato()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SampColors.SfondoChiaro),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_sampdoria),
                contentDescription = "Logo U.C. Sampdoria",
                modifier = Modifier.size(120.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "U.C. SAMPDORIA - SCOUTING giovani calciatori",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SampColors.Blu,
                textAlign = TextAlign.Center
            )
            Text(
                text = "versione 2.0 · offline",
                style = MaterialTheme.typography.labelSmall,
                color = SampColors.TestoSecondario
            )

            Spacer(Modifier.height(32.dp))

            OutlinedTextField(
                value = codice,
                onValueChange = {
                    codice = it.filter { c -> c.isDigit() }.take(10)
                    viewModel.pulisciErrore()
                },
                label = { Text("Codice di sblocco") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                shape = RoundedCornerShape(12.dp),
                isError = viewModel.errore != null,
                modifier = Modifier.fillMaxWidth()
            )

            viewModel.errore?.let {
                Spacer(Modifier.height(4.dp))
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { viewModel.tentaSblocco(codice) },
                enabled = codice.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = SampColors.Blu),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sblocca", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(48.dp))
            Text(
                text = "Developed by Di Vito Ruggero",
                style = MaterialTheme.typography.labelSmall,
                color = SampColors.TestoMuto
            )
        }
    }
}
