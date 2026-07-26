package com.scoutingsampdoria.persone2.data.repository

import com.scoutingsampdoria.persone2.data.db.AdminLogEntity
import com.scoutingsampdoria.persone2.data.db.CampoCustomEntity
import com.scoutingsampdoria.persone2.data.db.ConvocazioneEntity
import com.scoutingsampdoria.persone2.data.db.ConvocazioneGiocatoreEntity
import com.scoutingsampdoria.persone2.data.db.PersonaEntity
import com.scoutingsampdoria.persone2.data.db.ProvinoConJoinRow
import com.scoutingsampdoria.persone2.data.db.ProvinoEntity
import com.scoutingsampdoria.persone2.data.db.ScoutingDatabase
import com.scoutingsampdoria.persone2.data.model.CampoCustom
import com.scoutingsampdoria.persone2.data.model.Convocazione
import com.scoutingsampdoria.persone2.data.model.ConvocazioneGiocatore
import com.scoutingsampdoria.persone2.data.model.LogAdmin
import com.scoutingsampdoria.persone2.data.model.Persona
import com.scoutingsampdoria.persone2.data.model.Provino
import com.scoutingsampdoria.persone2.data.model.StatisticheProvini
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Repository unico dell'app 2.0: incapsula tutte le operazioni sul DB Room locale.
 * Sostituisce PersoneRepository dell'1.0 (che parlava via HTTPS con Flask).
 */
class Repository(private val db: ScoutingDatabase) {

    // ------------------- PERSONE -------------------

    suspend fun listaPersone(
        query: String? = null,
        regione: String? = null,
        societa: String? = null,
        ruolo: String? = null,
        quickReport: String? = null,
        filtriExtra: Map<String, String> = emptyMap()
    ): List<Persona> {
        val righe = db.personaDao().listaFiltrata(
            q = query?.takeIf { it.isNotBlank() },
            regione = regione,
            societa = societa,
            ruolo = ruolo,
            quickReport = quickReport
        )
        val persone = righe.map(::entityToModel)
        // Filtro extra applicato in memoria: SQL non può filtrare dentro il JSON facilmente
        return if (filtriExtra.isEmpty()) persone
        else persone.filter { p ->
            filtriExtra.all { (k, v) -> p.extra?.get(k) == v }
        }
    }

    suspend fun dettaglioPersona(id: Int): Persona? {
        return db.personaDao().byId(id)?.let(::entityToModel)
    }

    suspend fun creaPersona(persona: Persona): Int {
        // Garantisco STATO = "Segnalato" se non specificato
        val extra = (persona.extra ?: emptyMap()).toMutableMap()
        if (extra["STATO"].isNullOrBlank()) extra["STATO"] = "Segnalato"
        val personaConStato = persona.copy(extra = extra)
        val id = db.personaDao().inserisci(modelToEntity(personaConStato))
        return id.toInt()
    }

    suspend fun aggiornaPersona(persona: Persona) {
        db.personaDao().aggiorna(modelToEntity(persona))
    }

    suspend fun eliminaPersona(id: Int) {
        db.personaDao().eliminaById(id)
    }

    suspend fun eliminaTuttePersone() {
        db.personaDao().eliminaTutte()
    }

    suspend fun contaPersone(): Int = db.personaDao().conta()

    suspend fun valoriRegione(): List<String> = db.personaDao().valoriRegione()
    suspend fun valoriSocieta(): List<String> = db.personaDao().valoriSocieta()
    suspend fun valoriRuolo(): List<String> = db.personaDao().valoriRuolo()
    suspend fun valoriQuickReport(): List<String> = db.personaDao().valoriQuickReport()

    // ------------------- CAMPI CUSTOM -------------------

    suspend fun campiCustom(): List<CampoCustom> {
        return db.campoCustomDao().listaTutti().map { CampoCustom(it.id, it.nome) }
    }

    suspend fun aggiungiCampoCustom(nome: String) {
        db.campoCustomDao().inserisci(CampoCustomEntity(nome = nome.trim().uppercase()))
    }

    suspend fun eliminaCampoCustom(nome: String) {
        db.campoCustomDao().eliminaByNome(nome)
    }

    // ------------------- CONVOCAZIONI -------------------

    suspend fun listaConvocazioni(categoria: String? = null): List<Convocazione> {
        val entities = if (categoria != null) db.convocazioneDao().listaPerCategoria(categoria)
                       else db.convocazioneDao().listaTutte()
        return entities.map { convocazioneEntityToModel(it, null) }
    }

    suspend fun dettaglioConvocazione(id: Int): Convocazione? {
        val entity = db.convocazioneDao().byId(id) ?: return null
        val giocatori = db.convocazioneGiocatoreDao().perConvocazione(id).map { g ->
            ConvocazioneGiocatore(
                id = g.id,
                personaId = g.personaId,
                numero = g.numero,
                ordine = g.ordine,
                posizioneCampo = g.posizioneCampo,
                cognome = g.cognome,
                nome = g.nome,
                ruolo = g.ruolo,
            )
        }
        return convocazioneEntityToModel(entity, giocatori)
    }

    suspend fun creaConvocazione(
        categoria: String,
        data: String? = null,
        ora: String? = null,
        oraConvocazione: String? = null,
        impianto: String? = null,
        squadraCasa: String? = null,
        squadraOspite: String? = null,
        modulo: String? = null,
        numeroCaselle: Int = 20,
    ): Int {
        val id = db.convocazioneDao().inserisci(
            ConvocazioneEntity(
                categoria = categoria,
                data = data,
                ora = ora,
                oraConvocazione = oraConvocazione,
                impianto = impianto,
                squadraCasa = squadraCasa,
                squadraOspite = squadraOspite,
                modulo = modulo,
            )
        ).toInt()

        // Preparo N caselle vuote
        val vuote = (0 until numeroCaselle).map { ordine ->
            ConvocazioneGiocatoreEntity(convocazioneId = id, personaId = null, numero = null, ordine = ordine)
        }
        db.convocazioneGiocatoreDao().sostituisciTutti(id, vuote)
        return id
    }

    suspend fun aggiornaConvocazione(
        id: Int,
        data: String?,
        ora: String?,
        oraConvocazione: String?,
        impianto: String?,
        squadraCasa: String?,
        squadraOspite: String?,
        modulo: String?,
    ) {
        val corrente = db.convocazioneDao().byId(id) ?: return
        db.convocazioneDao().aggiorna(
            corrente.copy(
                data = data,
                ora = ora,
                oraConvocazione = oraConvocazione,
                impianto = impianto,
                squadraCasa = squadraCasa,
                squadraOspite = squadraOspite,
                modulo = modulo,
            )
        )
    }

    suspend fun eliminaConvocazione(id: Int) {
        db.convocazioneDao().eliminaById(id)
    }

    suspend fun aggiornaGiocatoriConvocazione(convocazioneId: Int, giocatori: List<ConvocazioneGiocatore>) {
        // 1. Leggo la convocazione per copiare data/ora/impianto nei nuovi provini
        val conv = db.convocazioneDao().byId(convocazioneId)

        // 2. Trova personaId prima e dopo l'aggiornamento
        val idPrima = db.convocazioneGiocatoreDao()
            .personIdsPerConvocazione(convocazioneId).toSet()
        val idDopo = giocatori.mapNotNull { it.personaId }.toSet()

        // 3. Sostituisci elenco convocati
        val entities = giocatori.map { g ->
            ConvocazioneGiocatoreEntity(
                convocazioneId = convocazioneId,
                personaId = g.personaId,
                numero = g.numero,
                ordine = g.ordine,
                posizioneCampo = g.posizioneCampo,
            )
        }
        db.convocazioneGiocatoreDao().sostituisciTutti(convocazioneId, entities)

        // 4. Sincronizza provini: crea per i nuovi convocati, elimina per rimossi (se vuoti)
        val provinoDao = db.provinoDao()
        val ora = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ITALIAN).format(Date())

        // Nuovi: creo provino vuoto solo se non esiste già
        val nuovi = idDopo - idPrima
        nuovi.forEach { personaId ->
            if (provinoDao.esiste(convocazioneId, personaId) == 0) {
                provinoDao.inserisci(ProvinoEntity(
                    personaId = personaId,
                    convocazioneId = convocazioneId,
                    data = conv?.data,
                    ora = conv?.ora,
                    impianto = conv?.impianto,
                    creatoIl = ora,
                    aggiornatoIl = ora,
                ))
            }
        }

        // Rimossi: elimino solo se il provino è vuoto (nessun dato compilato)
        val rimossi = idPrima - idDopo
        rimossi.forEach { personaId ->
            provinoDao.eliminaSeVuoto(convocazioneId, personaId)
        }
    }

    // ------------------- PROVINI -------------------

    suspend fun listaProviniPersona(personaId: Int): List<Provino> {
        return db.provinoDao().perPersonaConJoin(personaId).map(::rowToProvino)
    }

    suspend fun contaProviniPersona(personaId: Int): Int {
        return db.provinoDao().contaPerPersona(personaId)
    }

    suspend fun dettaglioProvino(id: Int): Provino? {
        return db.provinoDao().dettaglioConJoin(id)?.let(::rowToProvino)
    }

    suspend fun aggiornaProvino(id: Int, presenza: String?, giudizio: Int?, note: String?) {
        val ora = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ITALIAN).format(Date())
        db.provinoDao().aggiornaCampi(
            id = id,
            presenza = presenza?.takeIf { it.isNotBlank() },
            giudizio = giudizio,
            note = note?.takeIf { it.isNotBlank() },
            aggiornatoIl = ora,
        )
    }

    suspend fun eliminaProvino(id: Int) {
        db.provinoDao().eliminaById(id)
    }

    // === Dashboard provini ===

    suspend fun statisticheProvini(): StatisticheProvini {
        val totaleGiocatori = db.personaDao().conta()
        val totaleProvini = db.provinoDao().contaCompilati()
        val giocatoriConProvino = db.provinoDao().contaGiocatoriConProvino()
        val media = if (totaleGiocatori > 0) {
            val v = totaleProvini.toDouble() / totaleGiocatori.toDouble()
            // Arrotondo a 2 decimali
            Math.round(v * 100.0) / 100.0
        } else 0.0
        return StatisticheProvini(
            totaleProvini = totaleProvini,
            giocatoriConProvino = giocatoriConProvino,
            totaleGiocatori = totaleGiocatori,
            mediaPerGiocatore = media,
        )
    }

    suspend fun elencoProviniDashboard(categoria: String? = null, data: String? = null): List<Provino> {
        return db.provinoDao().dashboardFiltrata(
            categoria = categoria?.takeIf { it.isNotBlank() },
            data = data?.takeIf { it.isNotBlank() }
        ).map(::rowToProvino)
    }

    suspend fun categorieProvini(): List<String> = db.provinoDao().categorieDisponibili()

    suspend fun dateProvini(categoria: String? = null): List<String> =
        db.provinoDao().dateDisponibili(categoria?.takeIf { it.isNotBlank() })

    private fun rowToProvino(r: ProvinoConJoinRow): Provino = Provino(
        id = r.id,
        personaId = r.personaId,
        convocazioneId = r.convocazioneId,
        data = r.data,
        ora = r.ora,
        impianto = r.impianto,
        presenza = r.presenza,
        giudizio = r.giudizio,
        note = r.note,
        cognome = r.cognome,
        nome = r.nome,
        ruolo = r.ruolo,
        categoria = r.categoria,
        squadraCasa = r.squadraCasa,
        squadraOspite = r.squadraOspite,
        creatoIl = r.creatoIl,
        aggiornatoIl = r.aggiornatoIl,
    )

    // ------------------- LOG -------------------

    suspend fun logs(): List<LogAdmin> {
        return db.adminLogDao().ultimi().map { LogAdmin(it.id, it.tipo, it.dettaglio, it.creatoIl) }
    }

    suspend fun scriviLog(tipo: String, dettaglio: String? = null) {
        db.adminLogDao().inserisci(AdminLogEntity(tipo = tipo, dettaglio = dettaglio))
    }

    // ------------------- CONVERSIONI -------------------

    private fun entityToModel(e: PersonaEntity): Persona = Persona(
        id = e.id,
        cognome = e.cognome,
        nome = e.nome,
        dataNascita = e.dataNascita,
        regione = e.regione,
        societa = e.societa,
        ruolo = e.ruolo,
        matricola = e.matricola,
        quickReport = e.quickReport,
        extra = Persona.parseExtra(e.extra),
    )

    private fun modelToEntity(p: Persona): PersonaEntity = PersonaEntity(
        id = p.id,
        cognome = p.cognome,
        nome = p.nome,
        dataNascita = p.dataNascita,
        regione = p.regione,
        societa = p.societa,
        ruolo = p.ruolo,
        matricola = p.matricola,
        quickReport = p.quickReport,
        extra = Persona.serializzaExtra(p.extra),
    )

    private fun convocazioneEntityToModel(
        e: ConvocazioneEntity,
        giocatori: List<ConvocazioneGiocatore>?
    ): Convocazione = Convocazione(
        id = e.id,
        categoria = e.categoria,
        data = e.data,
        ora = e.ora,
        oraConvocazione = e.oraConvocazione,
        impianto = e.impianto,
        squadraCasa = e.squadraCasa,
        squadraOspite = e.squadraOspite,
        modulo = e.modulo,
        note = e.note,
        giocatori = giocatori,
    )
}
