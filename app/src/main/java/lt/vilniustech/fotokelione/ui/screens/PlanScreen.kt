package lt.vilniustech.fotokelione.ui.screens

import androidx.compose.animation.animateContentSize
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
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
import lt.vilniustech.fotokelione.validateName
import kotlin.math.roundToInt

@Composable
fun PlanScreen(viewModel: PlannerViewModel, snackbarHostState: SnackbarHostState) {
    val scope = rememberCoroutineScope()
    var showAddDialog by rememberSaveable { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 300.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                ProgressHeader(
                    done = viewModel.places.count { it.done },
                    total = viewModel.places.size,
                    progress = viewModel.progress,
                )
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PlaceFilter.entries.forEach { filter ->
                        FilterChip(
                            selected = viewModel.placeFilter == filter,
                            onClick = { viewModel.placeFilter = filter },
                            label = { Text(filter.label) },
                            leadingIcon = if (viewModel.placeFilter == filter) {
                                { Icon(Icons.Filled.Check, contentDescription = null, Modifier.size(18.dp)) }
                            } else {
                                null
                            },
                        )
                    }
                }
            }
            if (viewModel.visiblePlaces.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        "Šiame sąraše vietų nėra.",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(vertical = 24.dp),
                    )
                }
            }
            items(viewModel.visiblePlaces, key = { it.id }) { place ->
                PlaceCard(
                    place = place,
                    onToggleDone = { viewModel.togglePlaceDone(place.id) },
                    onDelete = {
                        val removed = viewModel.removePlace(place.id) ?: return@PlaceCard
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

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Pridėti vietą")
        }
    }

    if (showAddDialog) {
        AddPlaceDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, category, time, priority, outdoor ->
                if (viewModel.addPlace(name, category, time, priority, outdoor) == null) {
                    showAddDialog = false
                }
            },
        )
    }
}

@Composable
private fun ProgressHeader(done: Int, total: Int, progress: Float) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Nufotografuota $done iš $total", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Progresas ${(progress * 100).roundToInt()} %" },
            )
        }
    }
}

@Composable
private fun PlaceCard(place: Place, onToggleDone: () -> Unit, onDelete: () -> Unit) {
    var expanded by rememberSaveable(place.id) { mutableStateOf(false) }

    Card(
        onClick = { expanded = !expanded },
        colors = if (place.done) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        } else {
            CardDefaults.cardColors()
        },
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
        ) {
            Checkbox(
                checked = place.done,
                onCheckedChange = { onToggleDone() },
                modifier = Modifier.semantics {
                    contentDescription = "Pažymėti „${place.name}“ kaip nufotografuotą"
                },
            )
            Column(Modifier.weight(1f)) {
                Text(
                    place.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = if (place.done) TextDecoration.LineThrough else null,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    "${place.category.label} · ${place.timeOfDay.label}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = if (expanded) "Suskleisti" else "Išskleisti",
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Šalinti „${place.name}“")
            }
        }
        if (expanded) {
            Column(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Prioritetas: ", style = MaterialTheme.typography.bodyMedium)
                    Row(Modifier.semantics { contentDescription = "Prioritetas ${place.priority} iš 5" }) {
                        repeat(5) { i ->
                            Icon(
                                Icons.Filled.Star,
                                contentDescription = null,
                                tint = if (i < place.priority) {
                                    MaterialTheme.colorScheme.secondary
                                } else {
                                    MaterialTheme.colorScheme.outlineVariant
                                },
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
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
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddPlaceDialog(
    onDismiss: () -> Unit,
    onAdd: (String, Category, TimeOfDay, Int, Boolean) -> Unit,
) {
    var name by rememberSaveable { mutableStateOf("") }
    var nameTouched by rememberSaveable { mutableStateOf(false) }
    var category by rememberSaveable { mutableStateOf(Category.NATURE) }
    var categoryExpanded by rememberSaveable { mutableStateOf(false) }
    var timeOfDay by rememberSaveable { mutableStateOf(TimeOfDay.SUNSET) }
    var priority by rememberSaveable { mutableFloatStateOf(3f) }
    var outdoor by rememberSaveable { mutableStateOf(true) }

    val nameError = if (nameTouched) validateName(name) else null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nauja vieta") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
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

                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it },
                ) {
                    OutlinedTextField(
                        value = category.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Kategorija") },
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
                                onClick = {
                                    category = option
                                    categoryExpanded = false
                                },
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text("Paros metas", style = MaterialTheme.typography.labelLarge)
                Column(Modifier.selectableGroup()) {
                    TimeOfDay.entries.forEach { option ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
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

                Spacer(Modifier.height(12.dp))
                Text("Prioritetas: ${priority.roundToInt()}", style = MaterialTheme.typography.labelLarge)
                Slider(
                    value = priority,
                    onValueChange = { priority = it },
                    valueRange = 1f..5f,
                    steps = 3,
                    modifier = Modifier.semantics { contentDescription = "Prioritetas" },
                )

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
                nameTouched = true
                onAdd(name, category, timeOfDay, priority.roundToInt(), outdoor)
            }) { Text("Pridėti") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Atšaukti") }
        },
    )
}
