package lt.vilniustech.fotokelione.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import lt.vilniustech.fotokelione.GearInputErrors
import lt.vilniustech.fotokelione.GearItem
import lt.vilniustech.fotokelione.PlannerViewModel
import lt.vilniustech.fotokelione.formatKg
import lt.vilniustech.fotokelione.safeDivide
import kotlin.math.roundToInt

@Composable
fun GearScreen(viewModel: PlannerViewModel) {
    var showAddDialog by rememberSaveable { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 170.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                WeightSummary(
                    weightKg = viewModel.packedWeightKg,
                    limitKg = viewModel.luggageLimitKg,
                    overLimit = viewModel.overLimit,
                    onLimitChange = { viewModel.luggageLimitKg = it },
                )
            }
            items(viewModel.gear, key = { it.id }) { item ->
                GearCard(
                    item = item,
                    onToggle = { viewModel.toggleGearPacked(item.id) },
                    onDelete = { viewModel.removeGear(item.id) },
                )
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Pridėti daiktą")
        }
    }

    if (showAddDialog) {
        AddGearDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, weight ->
                viewModel.addGear(name, weight).also { if (it.isValid) showAddDialog = false }
            },
        )
    }
}

@Composable
private fun WeightSummary(
    weightKg: Double,
    limitKg: Float,
    overLimit: Boolean,
    onLimitChange: (Float) -> Unit,
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "Supakuota: ${formatKg(weightKg)} / ${formatKg(limitKg.toDouble())}",
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { safeDivide(weightKg, limitKg.toDouble()).toFloat().coerceIn(0f, 1f) },
                color = if (overLimit) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            Text("Bagažo limitas: ${limitKg.roundToInt()} kg", style = MaterialTheme.typography.labelLarge)
            Slider(
                value = limitKg,
                onValueChange = { onLimitChange(it.roundToInt().toFloat()) },
                valueRange = 5f..30f,
                steps = 24,
                modifier = Modifier.semantics { contentDescription = "Bagažo limitas kilogramais" },
            )
            AnimatedVisibility(visible = overLimit) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Warning, contentDescription = "Įspėjimas")
                        Spacer(Modifier.width(8.dp))
                        Text("Limitas viršytas ${formatKg(weightKg - limitKg)}!")
                    }
                }
            }
        }
    }
}

@Composable
private fun GearCard(item: GearItem, onToggle: () -> Unit, onDelete: () -> Unit) {
    Card(
        onClick = onToggle,
        colors = if (item.packed) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        } else {
            CardDefaults.cardColors()
        },
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(4.dp)) {
            Checkbox(
                checked = item.packed,
                onCheckedChange = { onToggle() },
                modifier = Modifier.semantics { contentDescription = "Supakuota: ${item.name}" },
            )
            Column(Modifier.weight(1f)) {
                Text(item.name, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(
                    formatKg(item.weightKg),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Close, contentDescription = "Šalinti „${item.name}“")
            }
        }
    }
}

@Composable
private fun AddGearDialog(onDismiss: () -> Unit, onAdd: (String, String) -> GearInputErrors) {
    var name by rememberSaveable { mutableStateOf("") }
    var weight by rememberSaveable { mutableStateOf("") }
    var errors by remember { mutableStateOf<GearInputErrors?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Naujas daiktas") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Pavadinimas") },
                    singleLine = true,
                    isError = errors?.name != null,
                    supportingText = errors?.name?.let { { Text(it.message) } },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Svoris, kg") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = errors?.weightInvalid == true,
                    supportingText = if (errors?.weightInvalid == true) {
                        { Text("Įveskite teigiamą skaičių") }
                    } else {
                        null
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { errors = onAdd(name, weight) }) { Text("Pridėti") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Atšaukti") }
        },
    )
}
