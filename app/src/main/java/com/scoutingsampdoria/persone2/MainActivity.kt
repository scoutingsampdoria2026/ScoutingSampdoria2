package com.scoutingsampdoria.persone2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.scoutingsampdoria.persone2.navigation.ScoutingNavGraph
import com.scoutingsampdoria.persone2.ui.theme.SampdoriaTheme
import com.scoutingsampdoria.persone2.viewmodel.ViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val factory = ViewModelFactory(applicationContext)
        setContent {
            SampdoriaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ScoutingNavGraph(factory)
                }
            }
        }
    }
}
