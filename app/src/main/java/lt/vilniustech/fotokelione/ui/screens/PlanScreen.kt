package lt.vilniustech.fotokelione.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import lt.vilniustech.fotokelione.R
import kotlinx.coroutines.launch
import lt.vilniustech.fotokelione.Category
import lt.vilniustech.fotokelione.NAME_MAX_LENGTH
import lt.vilniustech.fotokelione.Place
import lt.vilniustech.fotokelione.PlaceFilter
import lt.vilniustech.fotokelione.PlannerViewModel
import lt.vilniustech.fotokelione.TimeOfDay
import lt.vilniustech.fotokelione.ui.components.EmptyState
import lt.vilniustech.fotokelione.ui.components.HeroCard
import lt.vilniustech.fotokelione.ui.components.IconCircle
import lt.vilniustech.fotokelione.ui.components.icon
import lt.vilniustech.fotokelione.validateName
import kotlin.math.roundToInt

/*
 * ============================================================================
 *  PlanScreen.kt – 1 EKRANAS „PLANAS“ (fotografavimo vietos)
 * ============================================================================
 *  Ekrano dalys (iš viršaus į apačią):
 *   ProgressHeader  – spalvota kortelė su progreso juosta (LinearProgressIndicator)
 *   FilterRow       – filtro žymos Visi / Laukia / Atlikta (FilterChip)
 *   PlaceCard       – vietos kortelė: varnelė (Checkbox), išsiskleidimas
 *                     (animateContentSize), šalinimas + Snackbar „Atšaukti“
 *   FAB „Nauja vieta“ – atidaro AddPlaceDialog
 *   AddPlaceDialog  – forma: tekstinis laukas su tikrinimu, išskleidžiamas
 *                     sąrašas, radijo mygtukai, slankiklis, jungiklis
 *
 *  Tinklelis (LazyVerticalGrid + GridCells.Adaptive) pats parenka stulpelių
 *  skaičių: telefone 1 stulpelis, planšetėje 2–3.
 * ============================================================================
 */

@Composable
fun PlanScreen(viewModel: PlannerViewModel, snackbarHostState: SnackbarHostState) {
    // Korutina reikalinga Snackbar'ui parodyti (showSnackbar laukia vartotojo).
    val scope = rememberCoroutineScope()
    val context = LocalContext.current // reikalingas tekstui iš strings.xml gauti korutinoje
    // Ar rodomas dialogas „Nauja vieta“. rememberSaveable – išlieka pasukus ekraną.
    var showAddDialog by rememberSaveable { mutableStateOf(false) }

    // Box – elementai gali gulėti vienas ant kito (sąrašas + FAB virš jo)
    Box(Modifier.fillMaxSize()) {
        // TINKLELIS: LazyVerticalGrid piešia tik matomus elementus
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 320.dp), // prisitaikantis stulpelių skaičius
            // bottom = 96.dp – vieta apačioje, kad FAB neuždengtų paskutinės kortelės
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp), // tarpas tarp stulpelių
            verticalArrangement = Arrangement.spacedBy(12.dp),   // tarpas tarp eilučių
            modifier = Modifier.fillMaxSize(),
        ) {
            // span = maxLineSpan → elementas užima visą eilutės plotį
            item(span = { GridItemSpan(maxLineSpan) }) {
                ProgressHeader(
                    done = viewModel.doneCount,       // kiek nufotografuota
                    total = viewModel.places.size,    // kiek iš viso vietų
                    progress = viewModel.progress,    // 0..1 (placeProgress iš Logic.kt)
                )
            }
            // Filtro žymos per visą plotį
            item(span = { GridItemSpan(maxLineSpan) }) {
                FilterRow(
                    selected = viewModel.placeFilter,          // kuris filtras pasirinktas
                    countFor = viewModel::countFor,            // funkcija skaičiui „(3)“ gauti
                    onSelect = { viewModel.placeFilter = it }, // paspaudus – keičiame filtrą
                )
            }

            // Jei pagal filtrą nėra nė vienos vietos – rodome pranešimą
            if (viewModel.visiblePlaces.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EmptyState(Icons.Filled.PhotoCamera, stringResource(R.string.plan_empty))
                }
            }

            // Kiekvienai matomai vietai – po kortelę. key = id padeda animacijoms.
            items(viewModel.visiblePlaces, key = { it.id }) { place ->
                PlaceCard(
                    place = place,
                    onToggleDone = { viewModel.togglePlaceDone(place.id) }, // varnelė → atlikta / ne
                    onDelete = {
                        // ?: return@PlaceCard – jei nepavyko pašalinti (null), nieko nedarome
                        val removed = viewModel.removePlace(place.id) ?: return@PlaceCard
                        // Parodome Snackbar su mygtuku „Atšaukti“
                        scope.launch { // korutina: showSnackbar laukia, todėl vykdoma „fone“
                            snackbarHostState.currentSnackbarData?.dismiss() // uždarome seną pranešimą
                            val result = snackbarHostState.showSnackbar(
                                message = context.getString(R.string.place_removed, place.name),
                                actionLabel = context.getString(R.string.undo),
                                duration = SnackbarDuration.Short,
                            )
                            // ActionPerformed – vartotojas paspaudė „Atšaukti“
                            if (result == SnackbarResult.ActionPerformed) {
                                viewModel.restorePlace(removed.first, removed.second)
                            }
                        }
                    },
                )
            }
        }

        // FAB – plaukiojantis veiksmo mygtukas apačioje dešinėje
        ExtendedFloatingActionButton(
            onClick = { showAddDialog = true }, // atidaro dialogą „Nauja vieta“
            icon = { Icon(Icons.Filled.Add, contentDescription = null) }, // „+“ (tekstas jau yra šalia)
            text = { Text(stringResource(R.string.new_place)) },
            modifier = Modifier
                .align(Alignment.BottomEnd) // Box viduje – į apatinį dešinį kampą
                .padding(16.dp),
        )
    }

    // Dialogas rodomas tik kai showAddDialog = true
    if (showAddDialog) {
        AddPlaceDialog(
            onDismiss = { showAddDialog = false }, // „Atšaukti“ arba paspaudus šalia dialogo
            onAdd = { name, category, time, priority, outdoor ->
                // Dialogą uždarome tik tada, kai vieta sėkmingai pridėta
                if (viewModel.addPlace(name, category, time, priority, outdoor) == null) {
                    showAddDialog = false
                }
            },
        )
    }
}

/** Spalvota kortelė viršuje: „Nufotografuota X iš Y“ + procentai + progreso juosta. */
@Composable
private fun ProgressHeader(done: Int, total: Int, progress: Float) {
    val percent = (progress * 100).roundToInt() // 0.25 → 25
    val progressDescription = stringResource(R.string.progress_cd, percent)
    HeroCard { // spalvota kortelė su gradientu (Components.kt)
        Text(stringResource(R.string.trip_progress), style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.85f))
        Row(verticalAlignment = Alignment.Bottom) {
            Text("$percent %", style = MaterialTheme.typography.headlineMedium) // didelis skaičius
            Spacer(Modifier.weight(1f)) // tuščia vieta, nustumianti tekstą į dešinę
            Text(stringResource(R.string.photographed_of, done, total), style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(Modifier.height(12.dp))
        // PROGRESO JUOSTA: kiek vietų jau nufotografuota
        LinearProgressIndicator(
            progress = { progress },                     // 0..1
            color = Color.White,                         // užpildyta dalis
            trackColor = Color.White.copy(alpha = 0.3f), // neužpildyta dalis (permatoma)
            strokeCap = StrokeCap.Round,                 // apvalūs galai
            gapSize = 0.dp,                              // be tarpo tarp dalių
            drawStopIndicator = {},                      // be taško gale
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .semantics { contentDescription = progressDescription }, // PRIEINAMUMAS
        )
    }
}

/** Filtro žymos su skaičiumi, pvz. „Laukia (1)“. */
@Composable
private fun FilterRow(
    selected: PlaceFilter,
    countFor: (PlaceFilter) -> Int,
    onSelect: (PlaceFilter) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        PlaceFilter.entries.forEach { filter -> // Visi, Laukia, Atlikta
            // FILTRO ŽYMA
            FilterChip(
                selected = selected == filter,   // paryškinta, jei tai pasirinktas filtras
                onClick = { onSelect(filter) },  // paspaudus – pranešame, kuris pasirinktas
                label = { Text("${stringResource(filter.label)} (${countFor(filter)})") }, // pvz. „Laukia (3)“
                // varnelė rodoma tik ant pasirinkto filtro
                leadingIcon = if (selected == filter) {
                    { Icon(Icons.Filled.Check, contentDescription = null, Modifier.size(18.dp)) }
                } else {
                    null
                },
            )
        }
    }
}

/**
 * Vienos vietos kortelė.
 * Paspaudus kortelę – ji išsiskleidžia (animateContentSize) ir parodo detales.
 * Rodyklė pasisuka (animateFloatAsState).
 */
@Composable
private fun PlaceCard(place: Place, onToggleDone: () -> Unit, onDelete: () -> Unit) {
    // Ar kortelė išskleista. Kiekviena kortelė turi savo reikšmę (raktas place.id).
    var expanded by rememberSaveable(place.id) { mutableStateOf(false) }
    // ANIMACIJA: rodyklės kampas sklandžiai keičiasi 0° ↔ 180°
    val arrowRotation by animateFloatAsState(if (expanded) 180f else 0f, label = "rodyklė")
    val colors = MaterialTheme.colorScheme
    val markDoneDescription = stringResource(R.string.mark_done_cd, place.name)

    // KORTELĖ
    Card(
        onClick = { expanded = !expanded }, // paspaudus – išskleidžiame / suskleidžiame
        colors = CardDefaults.cardColors(
            // atlikta vieta – šiek tiek tamsesnis fonas
            containerColor = if (place.done) colors.surfaceContainerHigh else colors.surfaceContainerLow,
        ),
        // šešėlis tik nebaigtoms vietoms – atliktos atrodo „įspaustos“
        elevation = CardDefaults.cardElevation(defaultElevation = if (place.done) 0.dp else 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(), // ANIMACIJA: sklandus dydžio pokytis
    ) {
        // --- Viršutinė eilutė: ikona, pavadinimas, varnelė, rodyklė ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
        ) {
            // Kategorijos ikona apskritime (Components.kt); atlikta – žalia, laukia – žalsvai mėlyna
            IconCircle(
                icon = place.category.icon,
                background = if (place.done) colors.tertiaryContainer else colors.primaryContainer,
                tint = if (place.done) colors.onTertiaryContainer else colors.onPrimaryContainer,
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    place.name,
                    style = MaterialTheme.typography.titleMedium,
                    // atlikta vieta – perbraukta
                    textDecoration = if (place.done) TextDecoration.LineThrough else null,
                    color = if (place.done) colors.onSurfaceVariant else colors.onSurface,
                    maxLines = 1,                     // tik viena eilutė
                    overflow = TextOverflow.Ellipsis, // per ilgas – baigiasi „…“
                )
                // Antra eilutė: „Architektūra · Saulėtekis“
                Text(
                    "${stringResource(place.category.label)} · ${stringResource(place.timeOfDay.label)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                )
            }
            // Rodyklė ⌄, kuri pasisuka pagal arrowRotation
            Icon(
                Icons.Filled.ExpandMore,
                contentDescription = stringResource(if (expanded) R.string.collapse else R.string.expand), // PRIEINAMUMAS
                tint = colors.onSurfaceVariant,
                modifier = Modifier.rotate(arrowRotation),
            )
            // ŽYMIMASIS LANGELIS: varnelė = nufotografuota
            Checkbox(
                checked = place.done,
                onCheckedChange = { onToggleDone() },
                // PRIEINAMUMAS: TalkBack pasakys, kurios vietos varnelė
                modifier = Modifier.semantics {
                    contentDescription = markDoneDescription
                },
            )
        }

        // --- Išskleista dalis: prioritetas, žymos, šalinimo mygtukas ---
        // Šita dalis atsiranda tik išskleidus; animateContentSize padaro tai sklandžiai
        if (expanded) {
            HorizontalDivider(Modifier.padding(horizontal = 16.dp)) // skiriamoji linija
            Column(Modifier.padding(16.dp)) {
                PriorityStars(place.priority)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Žyma „Lauke“ / „Patalpoje“ (tik informacija, paspaudus nieko nedaro)
                    AssistChip(
                        onClick = {},
                        label = { Text(stringResource(if (place.outdoor) R.string.outdoor else R.string.indoor)) },
                        leadingIcon = {
                            Icon(
                                if (place.outdoor) Icons.Filled.Park else Icons.Filled.Home,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                        },
                    )
                    // Žyma su paros metu
                    AssistChip(
                        onClick = {},
                        label = { Text(stringResource(place.timeOfDay.label)) },
                        leadingIcon = {
                            Icon(place.timeOfDay.icon, contentDescription = null, modifier = Modifier.size(18.dp))
                        },
                    )
                }
                // Mygtukas „Šalinti“ dešinėje → PlanScreen parodys Snackbar su „Atšaukti“
                TextButton(onClick = onDelete, modifier = Modifier.align(Alignment.End)) {
                    Icon(Icons.Filled.Delete, contentDescription = null, tint = colors.error)
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.delete), color = colors.error)
                }
            }
        }
    }
}

/** Prioritetas žvaigždutėmis: ★★★☆☆ */
@Composable
private fun PriorityStars(priority: Int) {
    val priorityDescription = stringResource(R.string.priority_cd, priority)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        // PRIEINAMUMAS: visa eilutė perskaitoma kaip „Prioritetas 3 iš 5“
        modifier = Modifier.semantics(mergeDescendants = true) {
            contentDescription = priorityDescription
        },
    ) {
        Text(stringResource(R.string.priority), style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.width(8.dp))
        repeat(5) { i -> // 5 žvaigždutės, i = 0..4
            Icon(
                // užpildyta žvaigždė, jei i < prioritetas; kitaip – tik kontūras
                if (i < priority) Icons.Filled.Star else Icons.Outlined.StarOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

/**
 * Dialogas „Nauja vieta“.
 * Pavadinimas tikrinamas funkcija validateName (Logic.kt): tuščias arba
 * ilgesnis nei 40 simbolių – laukas tampa raudonas su klaidos tekstu.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddPlaceDialog(
    onDismiss: () -> Unit,
    onAdd: (String, Category, TimeOfDay, Int, Boolean) -> Unit,
) {
    // Formos laukų būsena
    var name by rememberSaveable { mutableStateOf("") }
    var nameTouched by rememberSaveable { mutableStateOf(false) } // klaidą rodome tik pradėjus rašyti
    var category by rememberSaveable { mutableStateOf(Category.NATURE) }
    var categoryExpanded by rememberSaveable { mutableStateOf(false) }
    var timeOfDay by rememberSaveable { mutableStateOf(TimeOfDay.SUNSET) }
    var priority by rememberSaveable { mutableFloatStateOf(3f) }
    var outdoor by rememberSaveable { mutableStateOf(true) }

    // Tikriname pavadinimą (Logic.kt → validateName): null = gerai, kitaip – klaida
    val nameError = if (nameTouched) validateName(name) else null
    val priorityDescription = stringResource(R.string.priority)
    val outdoorDescription = stringResource(R.string.outdoor_cd)

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Filled.AddLocationAlt, contentDescription = null) },
        title = { Text(stringResource(R.string.new_place)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                // TEKSTINIS LAUKAS su tikrinimu
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameTouched = true
                    },
                    label = { Text(stringResource(R.string.name)) },
                    singleLine = true,
                    isError = nameError != null, // raudonas, jei yra klaida
                    // Po lauku: klaidos tekstas ARBA simbolių skaitiklis „5 / 40“
                    // ?: – „jei kairėje null, imk dešinę“
                    supportingText = {
                        Text(
                            if (nameError != null) {
                                stringResource(nameError.message, NAME_MAX_LENGTH) // klaidos tekstas iš strings.xml
                            } else {
                                "${name.trim().length} / $NAME_MAX_LENGTH"
                            },
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                )

                // IŠSKLEIDŽIAMAS SĄRAŠAS (kategorija)
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,                  // ar sąrašas atidarytas
                    onExpandedChange = { categoryExpanded = it }, // paspaudus laukelį – atidaro / uždaro
                ) {
                    // Laukas, rodantis pasirinktą kategoriją (rašyti į jį negalima)
                    OutlinedTextField(
                        value = stringResource(category.label),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.category)) },
                        leadingIcon = { Icon(category.icon, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable) // sąrašas atsidaro po šiuo lauku
                            .fillMaxWidth(),
                    )
                    // Pats sąrašas su visomis kategorijomis
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false },
                    ) {
                        Category.entries.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(stringResource(option.label)) },
                                leadingIcon = { Icon(option.icon, contentDescription = null) },
                                onClick = {
                                    category = option         // įsimename pasirinkimą
                                    categoryExpanded = false  // uždarome sąrašą
                                },
                            )
                        }
                    }
                }

                // RADIJO MYGTUKAI (paros metas)
                Spacer(Modifier.height(16.dp))
                Text(stringResource(R.string.time_of_day), style = MaterialTheme.typography.labelLarge)
                // selectableGroup – prieinamumui pasako, kad tai viena pasirinkimų grupė
                Column(Modifier.selectableGroup()) {
                    TimeOfDay.entries.forEach { option -> // Saulėtekis, Diena, Saulėlydis, Naktis
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable( // visa eilutė paspaudžiama, ne tik apskritimas
                                    selected = timeOfDay == option,
                                    onClick = { timeOfDay = option },
                                    role = Role.RadioButton,
                                )
                                .padding(vertical = 2.dp),
                        ) {
                            // onClick = null, nes paspaudimą tvarko visa eilutė (.selectable)
                            RadioButton(selected = timeOfDay == option, onClick = null)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(option.label))
                        }
                    }
                }

                // SLANKIKLIS (prioritetas 1–5)
                Spacer(Modifier.height(12.dp))
                Text(stringResource(R.string.priority_value, priority.roundToInt()), style = MaterialTheme.typography.labelLarge)
                Slider(
                    value = priority,                  // kur stovi rutuliukas
                    onValueChange = { priority = it }, // pajudinus – įrašome naują reikšmę
                    valueRange = 1f..5f,               // nuo 1 iki 5
                    steps = 3, // 3 tarpiniai taškai → reikšmės 1, 2, 3, 4, 5
                    modifier = Modifier.semantics { contentDescription = priorityDescription },
                )

                // JUNGIKLIS (lauke / patalpoje)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(if (outdoor) R.string.outdoor else R.string.indoor), Modifier.weight(1f))
                    Switch(
                        checked = outdoor,                  // true – lauke
                        onCheckedChange = { outdoor = it }, // perjungus – įrašome naują reikšmę
                        modifier = Modifier.semantics { contentDescription = outdoorDescription },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                nameTouched = true // jei nieko neįvesta – parodys klaidą
                // perduodame duomenis į PlanScreen → viewModel.addPlace(...)
                onAdd(name, category, timeOfDay, priority.roundToInt(), outdoor)
            }) { Text(stringResource(R.string.add)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    )
}
