package com.scoutingsampdoria.persone2.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.scoutingsampdoria.persone2.ui.screens.ConfigScreen
import com.scoutingsampdoria.persone2.ui.screens.ConvocazioneDetailScreen
import com.scoutingsampdoria.persone2.ui.screens.ConvocazioniHomeScreen
import com.scoutingsampdoria.persone2.ui.screens.ConvocazioniListaScreen
import com.scoutingsampdoria.persone2.ui.screens.ElencoProviniScreen
import com.scoutingsampdoria.persone2.ui.screens.HomeScreen
import com.scoutingsampdoria.persone2.ui.screens.PersonDetailScreen
import com.scoutingsampdoria.persone2.ui.screens.PersonFormScreen
import com.scoutingsampdoria.persone2.ui.screens.PersonListScreen
import com.scoutingsampdoria.persone2.ui.screens.ProviniDashboardScreen
import com.scoutingsampdoria.persone2.ui.screens.ProvinoDetailScreen
import com.scoutingsampdoria.persone2.ui.screens.SbloccoScreen
import com.scoutingsampdoria.persone2.util.MonitorInattivita
import com.scoutingsampdoria.persone2.util.tracciaInterazione
import com.scoutingsampdoria.persone2.viewmodel.AuthViewModel
import com.scoutingsampdoria.persone2.viewmodel.ConfigViewModel
import com.scoutingsampdoria.persone2.viewmodel.ConvocazioniViewModel
import com.scoutingsampdoria.persone2.viewmodel.PersoneViewModel
import com.scoutingsampdoria.persone2.viewmodel.ProviniViewModel
import com.scoutingsampdoria.persone2.viewmodel.ViewModelFactory

object Rotte {
    const val SBLOCCO = "sblocco"
    const val HOME = "home"
    const val LISTA_GIOCATORI = "giocatori"
    const val DETTAGLIO_GIOCATORE = "giocatore/{id}"
    const val FORM_GIOCATORE = "giocatore/form?id={id}"
    const val CONFIGURAZIONE = "configurazione"
    const val CONVOCAZIONI_HOME = "convocazioni"
    const val CONVOCAZIONI_LISTA = "convocazioni/{categoria}"
    const val CONVOCAZIONE_DETTAGLIO = "convocazione/{id}"
    const val ELENCO_PROVINI = "provini_persona/{personaId}"
    const val DETTAGLIO_PROVINO = "provino/{provinoId}"
    const val PROVINI_DASHBOARD = "provini_dashboard"

    fun dettaglioGiocatore(id: Int) = "giocatore/$id"
    fun formGiocatore(id: Int?) = if (id == null) "giocatore/form?id=-1" else "giocatore/form?id=$id"
    fun convocazioniLista(categoria: String) = "convocazioni/$categoria"
    fun convocazioneDettaglio(id: Int) = "convocazione/$id"
    fun elencoProvini(personaId: Int) = "provini_persona/$personaId"
    fun dettaglioProvino(provinoId: Int) = "provino/$provinoId"
}

@Composable
fun ScoutingNavGraph(factory: ViewModelFactory) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel(factory = factory)
    val personeViewModel: PersoneViewModel = viewModel(factory = factory)
    val convocazioniViewModel: ConvocazioniViewModel = viewModel(factory = factory)
    val configViewModel: ConfigViewModel = viewModel(factory = factory)
    val proviniViewModel: ProviniViewModel = viewModel(factory = factory)

    // Tracker inattività: si aggiorna a ogni tocco. Se supera 30 min → blocco automatico.
    var ultimaInterazione by remember { mutableLongStateOf(System.currentTimeMillis()) }
    if (authViewModel.sbloccato) {
        MonitorInattivita(ultimaInterazione) {
            authViewModel.blocca()
            navController.navigate(Rotte.SBLOCCO) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .tracciaInterazione { ultimaInterazione = System.currentTimeMillis() }
    ) {
    NavHost(navController = navController, startDestination = Rotte.SBLOCCO) {

        composable(Rotte.SBLOCCO) {
            SbloccoScreen(
                viewModel = authViewModel,
                onSbloccato = {
                    navController.navigate(Rotte.HOME) {
                        popUpTo(Rotte.SBLOCCO) { inclusive = true }
                    }
                }
            )
        }

        composable(Rotte.HOME) {
            LaunchedEffect(Unit) { proviniViewModel.caricaStatistiche(forza = false) }
            HomeScreen(
                statistiche = proviniViewModel.statistiche,
                onGestioneGiocatori = { navController.navigate(Rotte.LISTA_GIOCATORI) },
                onConvocazioni = { navController.navigate(Rotte.CONVOCAZIONI_HOME) },
                onProvini = { navController.navigate(Rotte.PROVINI_DASHBOARD) },
                onConfigurazione = { navController.navigate(Rotte.CONFIGURAZIONE) },
                onBlocca = {
                    authViewModel.blocca()
                    navController.navigate(Rotte.SBLOCCO) {
                        popUpTo(Rotte.HOME) { inclusive = true }
                    }
                }
            )
        }

        composable(Rotte.LISTA_GIOCATORI) {
            PersonListScreen(
                viewModel = personeViewModel,
                onIndietro = { navController.popBackStack() },
                onSelezionaGiocatore = { id ->
                    navController.navigate(Rotte.dettaglioGiocatore(id))
                },
                onNuovoGiocatore = { navController.navigate(Rotte.formGiocatore(null)) },
                onApriFiltri = {
                    // TODO in fase successiva: schermata filtri dedicata
                }
            )
        }

        composable(
            route = Rotte.DETTAGLIO_GIOCATORE,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("id") ?: return@composable
            PersonDetailScreen(
                viewModel = personeViewModel,
                proviniViewModel = proviniViewModel,
                idPersona = id,
                onIndietro = { navController.popBackStack() },
                onModifica = { navController.navigate(Rotte.formGiocatore(id)) },
                onApriElencoProvini = { personaId ->
                    navController.navigate(Rotte.elencoProvini(personaId))
                }
            )
        }

        composable(
            route = Rotte.FORM_GIOCATORE,
            arguments = listOf(navArgument("id") {
                type = NavType.IntType
                defaultValue = -1
            })
        ) { backStackEntry ->
            val idRaw = backStackEntry.arguments?.getInt("id") ?: -1
            val idPersona = if (idRaw > 0) idRaw else null
            PersonFormScreen(
                viewModel = personeViewModel,
                idPersona = idPersona,
                onSalvato = { id ->
                    // Torna al dettaglio dopo il salvataggio
                    navController.navigate(Rotte.dettaglioGiocatore(id)) {
                        popUpTo(Rotte.LISTA_GIOCATORI)
                    }
                },
                onIndietro = { navController.popBackStack() }
            )
        }

        composable(Rotte.CONFIGURAZIONE) {
            ConfigScreen(
                configViewModel = configViewModel,
                authViewModel = authViewModel,
                onIndietro = { navController.popBackStack() }
            )
        }

        composable(Rotte.CONVOCAZIONI_HOME) {
            ConvocazioniHomeScreen(
                personeViewModel = personeViewModel,
                convocazioniViewModel = convocazioniViewModel,
                onIndietro = { navController.popBackStack() },
                onSelezionaCategoria = { categoria ->
                    navController.navigate(Rotte.convocazioniLista(categoria))
                }
            )
        }

        composable(
            route = Rotte.CONVOCAZIONI_LISTA,
            arguments = listOf(navArgument("categoria") { type = NavType.StringType })
        ) { backStackEntry ->
            val categoria = backStackEntry.arguments?.getString("categoria") ?: return@composable
            ConvocazioniListaScreen(
                viewModel = convocazioniViewModel,
                categoria = categoria,
                onIndietro = { navController.popBackStack() },
                onApriConvocazione = { id ->
                    navController.navigate(Rotte.convocazioneDettaglio(id))
                }
            )
        }

        composable(
            route = Rotte.CONVOCAZIONE_DETTAGLIO,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("id") ?: return@composable
            ConvocazioneDetailScreen(
                personeViewModel = personeViewModel,
                convocazioniViewModel = convocazioniViewModel,
                convocazioneId = id,
                onIndietro = { navController.popBackStack() }
            )
        }

        composable(
            route = Rotte.ELENCO_PROVINI,
            arguments = listOf(navArgument("personaId") { type = NavType.IntType })
        ) { backStackEntry ->
            val personaId = backStackEntry.arguments?.getInt("personaId") ?: return@composable
            ElencoProviniScreen(
                personaId = personaId,
                personeViewModel = personeViewModel,
                proviniViewModel = proviniViewModel,
                onIndietro = { navController.popBackStack() },
                onApriProvino = { provinoId ->
                    navController.navigate(Rotte.dettaglioProvino(provinoId))
                }
            )
        }

        composable(
            route = Rotte.DETTAGLIO_PROVINO,
            arguments = listOf(navArgument("provinoId") { type = NavType.IntType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("provinoId") ?: return@composable
            ProvinoDetailScreen(
                provinoId = id,
                proviniViewModel = proviniViewModel,
                onIndietro = { navController.popBackStack() }
            )
        }

        composable(Rotte.PROVINI_DASHBOARD) {
            ProviniDashboardScreen(
                proviniViewModel = proviniViewModel,
                onIndietro = { navController.popBackStack() },
                onApriProvino = { provinoId ->
                    navController.navigate(Rotte.dettaglioProvino(provinoId))
                }
            )
        }
    }
    }  // chiusura Box tracker inattività
}
