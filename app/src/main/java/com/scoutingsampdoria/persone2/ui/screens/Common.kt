package com.scoutingsampdoria.persone2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.scoutingsampdoria.persone2.ui.theme.SampColors

@Composable
fun FasciaBlucerchiata(modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth().height(4.dp)) {
        Box(Modifier.weight(1f).fillMaxSize().background(SampColors.Bianco))
        Box(Modifier.weight(1f).fillMaxSize().background(SampColors.Rosso))
        Box(Modifier.weight(1f).fillMaxSize().background(SampColors.Nero))
        Box(Modifier.weight(1f).fillMaxSize().background(SampColors.Bianco))
    }
}
