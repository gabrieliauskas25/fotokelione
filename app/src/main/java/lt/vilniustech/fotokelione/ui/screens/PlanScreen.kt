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
    // Ar rodomas dialogas „Nauja vieta“. rememberSaveable – išlieka pasukus ekraną.
    var showAddDialog by rememberSaveable { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 320.dp), // prisitaikantis stulpelių skaičius
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            // span = maxLineSpan → elementas užima visą eilutės plotį
            item(span = { GridItemSpan(maxLineSpan) }) {
                ProgressHeader(
                    done = viewModel.doneCount,
                    total = viewModel.places.size,
                    progress = viewModel.progress,
                )
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                FilterRow(
                    selected = viewModel.placeFilter,
                    countFor = viewModel::countFor,
                    onSelect = { viewModel.placeFilter = it },
                )
            }

            if (viewModel.visiblePlaces.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    EmptyState(Icons.Filled.PhotoCamera, "Šiame sąraše vietų nėra")
                }
            }

            // Kiekvienai matomai vietai – po kortelę. key = id padeda animacijoms.
            items(viewModel.visiblePlaces, key = { it.id }) { place ->
                PlaceCard(
                    place = place,
                    onToggleDone = { viewModel.togglePlaceDone(place.id) },
                    onDelete = {
                        val removed = viewModel.removePlace(place.id) ?: return@PlaceCard
                        // Parodome Snackbar su mygtuku „Atšaukti“
                        scope.launch {
                            snackbarHostState.currentSnackbarData?.dismiss()
                            val result = snackbarHostState.showSnackbar(
                                message = "„${place.name}“ pašalinta",
                                actionLabel = "Atšaukti",
                                duration = SnackbarDuration.Short,
                            )
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
            onClick = { showAddDialog = true },
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            text = { Text("Nauja vieta") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
        )
    }

    if (showAddDialog) {
        AddPlaceDialog(
            onDismiss = { showAddDialog = false },
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
    val percent = (progress * 100).roundToInt()
    HeroCard {
        Text("Kelionės progresas", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.85f))
        Row(verticalAlignment = Alignment.Bottom) {
            Text("$percent %", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.weight(1f))
            Text("Nufotografuota $done iš $total", style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(Modifier.height(12.dp))
        LinearProgressIndicator(
            progress = { progress },
            color = Color.White,
            trackColor = Color.White.copy(alpha = 0.3f),
            strokeCap = StrokeCap.Round,
            gapSize = 0.dp,
            drawStopIndicator = {},
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .semantics { contentDescription = "Progresas $percent procentų" },
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
        PlaceFilter.entries.forEach { filter ->
            FilterChip(
                selected = selected == filter,
                onClick = { onSelect(filter) },
                label = { Text("${filter.label} (${countFor(filter)})") },
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
    var expanded by rememberSaveable(place.id) { mutableStateOf(false) }
    val arrowRotation by animateFloatAsState(if (expanded) 180f else 0f, label = "rodyklė")
    val colors = MaterialTheme.colorScheme

    Card(
        onClick = { expanded = !expanded },
        colors = CardDefaults.cardColors(
            containerColor = if (place.done) colors.surfaceContainerHigh else colors.surfaceContainerLow,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(), // ANIMACIJA: sklandus dydžio pokytis
    ) {
        // --- Viršutinė eilutė: ikona, pavadinimas, varnelė, rodyklė ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
        ) {
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
                    textDecoration = if (place.done) TextDecoration.LineThrough else null,
                    color = if (place.done) colors.onSurfaceVariant else colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    "${place.category.label} · ${place.timeOfDay.label}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                )
            }
            Icon(
                Icons.Filled.ExpandMore,
                contentDescription = if (expanded) "Suskleisti" else "Išskleisti",
                tint = colors.onSurfaceVariant,
                modifier = Modifier.rotate(arrowRotation),
            )
            Checkbox(
                checked = place.done,
                onCheckedChange = { onToggleDone() },
                modifier = Modifier.semantics {
                    contentDescription = "Pažymėti „${place.name}“ kaip nufotografuotą"
                },
            )
        }

        // --- Išskleista dalis: prioritetas, žymos, šalinimo mygtukas ---
        if (expanded) {
            HorizontalDivider(Modifier.padding(horizontal = 16.dp))
            Column(Modifier.padding(16.dp)) {
                PriorityStars(place.priority)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(
                        onClick = {},
                        label = { Text(if (place.outdoor) "Lauke" else "Patalpoje") },
                        leadingIcon = {
                            Icon(
                                if (place.outdoor) Icons.Filled.Park else Icons.Filled.Home,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                        },
                    )
                    AssistChip(
                        onClick = {},
                        label = { Text(place.timeOfDay.label) },
                        leadingIcon = {
                            Icon(place.timeOfDay.icon, contentDescription = null, modifier = Modifier.size(18.dp))
                        },
                    )
                }
                TextButton(onClick = onDelete, modifier = Modifier.align(Alignment.End)) {
                    Icon(Icons.Filled.Delete, contentDescription = null, tint = colors.error)
                    Spacer(Modifier.width(4.dp))
                    Text("Šalinti", color = colors.error)
                }
            }
        }
    }
}

/** Prioritetas žvaigždutėmis: ★★★☆☆ */
@Composable
private fun PriorityStars(priority: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.semantics(mergeDescendants = true) {
            contentDescription = "Prioritetas $priority iš 5"
        },
    ) {
        Text("Prioritetas", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.width(8.dp))
        repeat(5) { i ->
            Icon(
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

    val nameError = if (nameTouched) validateName(name) else null

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Filled.AddLocationAlt, contentDescription = null) },
        title = { Text("Nauja vieta") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                // TEKSTINIS LAUKAS su tikrinimu
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameTouched = true
                    },
                    label = { Text("Pavadinimas") },
                    singleLine = true,
                    isError = nameError != null,
                    supportingText = {
                        Text(nameError?.message ?: "${name.trim().length} / $NAME_MAX_LENGTH")
                    },
                    modifier = Modifier.fillMaxWidth(),
                )

                // IŠSKLEIDŽIAMAS SĄRAŠAS (kategorija)
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it },
                ) {
                    OutlinedTextField(
                        value = category.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Kategorija") },
                        leadingIcon = { Icon(category.icon, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth(),
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false },
                    ) {
                        Category.entries.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.label) },
                                leadingIcon = { Icon(option.icon, contentDescription = null) },
                                onClick = {
                                    category = option
                                    categoryExpanded = false
                                },
                            )
                        }
                    }
                }

                // RADIJO MYGTUKAI (paros metas)
                Spacer(Modifier.height(16.dp))
                Text("Paros metas", style = MaterialTheme.typography.labelLarge)
                Column(Modifier.selectableGroup()) {
                    TimeOfDay.entries.forEach { option ->
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
                            RadioButton(selected = timeOfDay == option, onClick = null)
                            Spacer(Modifier.width(8.dp))
                            Text(option.label)
                        }
                    }
                }

                // SLANKIKLIS (prioritetas 1–5)
                Spacer(Modifier.height(12.dp))
                Text("Prioritetas: ${priority.roundToInt()}", style = MaterialTheme.typography.labelLarge)
                Slider(
                    value = priority,
                    onValueChange = { priority = it },
                    valueRange = 1f..5f,
                    steps = 3, // 3 tarpiniai taškai → reikšmės 1, 2, 3, 4, 5
                    modifier = Modifier.semantics { contentDescription = "Prioritetas" },
                )

                // JUNGIKLIS (lauke / patalpoje)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (outdoor) "Lauke" else "Patalpoje", Modifier.weight(1f))
                    Switch(
                        checked = outdoor,
                        onCheckedChange = { outdoor = it },
                        modifier = Modifier.semantics { contentDescription = "Fotografuojama lauke" },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                nameTouched = true // jei nieko neįvesta – parodys klaidą
                onAdd(name, category, timeOfDay, priority.roundToInt(), outdoor)
            }) { Text("Pridėti") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Atšaukti") }
        },
    )
}
