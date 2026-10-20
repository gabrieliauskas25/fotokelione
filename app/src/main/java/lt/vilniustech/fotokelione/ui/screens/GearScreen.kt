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
import androidx.compose.material.icons.filled.Backpack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
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
import lt.vilniustech.fotokelione.ui.components.HeroCard
import lt.vilniustech.fotokelione.ui.components.IconCircle
import kotlin.math.roundToInt

/*
 * ============================================================================
 *  GearScreen.kt – 2 EKRANAS „ĮRANGA“ (pakavimas ir svoris)
 * ============================================================================
 *  Ekrano dalys:
 *   WeightSummary – spalvota kortelė: supakuotas svoris, progreso juosta,
 *                   bagažo limito slankiklis ir įspėjimas (AnimatedVisibility),
 *                   kai limitas viršytas
 *   GearCard      – daikto kortelė: varnelė „supakuota“, svoris, šalinimas
 *   FAB „Naujas daiktas“ → AddGearDialog (pavadinimas + svoris)
 *
 *  Svorio skaičiavimas: packedWeight() ir isOverLimit() faile Logic.kt.
 * ============================================================================
 */

@Composable
fun GearScreen(viewModel: PlannerViewModel) {
    var showAddDialog by rememberSaveable { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 300.dp), // telefone 1 stulpelis, planšetėje daugiau
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                WeightSummary(
                    weightKg = viewModel.packedWeightKg,
                    limitKg = viewModel.luggageLimitKg,
                    overLimit = viewModel.overLimit,
                    packedCount = viewModel.packedCount,
                    totalCount = viewModel.gear.size,
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

        ExtendedFloatingActionButton(
            onClick = { showAddDialog = true },
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            text = { Text("Naujas daiktas") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
        )
    }

    if (showAddDialog) {
        AddGearDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, weight ->
                val errors = viewModel.addGear(name, weight)
                if (errors.isValid) showAddDialog = false
                errors
            },
        )
    }
}

/**
 * Svorio suvestinė viršuje.
 * Jei limitas viršytas – kortelė tampa raudona ir atsiranda įspėjimas.
 */
@Composable
private fun WeightSummary(
    weightKg: Double,
    limitKg: Float,
    overLimit: Boolean,
    packedCount: Int,
    totalCount: Int,
    onLimitChange: (Float) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    HeroCard(
        startColor = if (overLimit) colors.error else colors.primary,
        endColor = if (overLimit) colors.secondary else colors.tertiary,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Supakuota", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.85f))
                Text(formatKg(weightKg), style = MaterialTheme.typography.headlineMedium)
                Text("iš ${formatKg(limitKg.toDouble())} · $packedCount/$totalCount daiktų", style = MaterialTheme.typography.bodyMedium)
            }
            IconCircle(
                icon = Icons.Filled.Luggage,
                background = Color.White.copy(alpha = 0.2f),
                tint = Color.White,
                size = 56.dp,
            )
        }
        Spacer(Modifier.height(12.dp))
        // PROGRESO JUOSTA: kiek limito jau užpildyta
        LinearProgressIndicator(
            progress = { safeDivide(weightKg, limitKg.toDouble()).toFloat().coerceIn(0f, 1f) },
            color = Color.White,
            trackColor = Color.White.copy(alpha = 0.3f),
            strokeCap = StrokeCap.Round,
            gapSize = 0.dp,
            drawStopIndicator = {},
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
        )
        Spacer(Modifier.height(12.dp))

        // SLANKIKLIS: bagažo limitas nuo 5 iki 30 kg kas 1 kg
        Text("Bagažo limitas: ${limitKg.roundToInt()} kg", style = MaterialTheme.typography.labelLarge)
        Slider(
            value = limitKg,
            onValueChange = { onLimitChange(it.roundToInt().toFloat()) },
            valueRange = 5f..30f,
            steps = 24,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.White,
                inactiveTrackColor = Color.White.copy(alpha = 0.3f),
                activeTickColor = Color.Transparent,
                inactiveTickColor = Color.Transparent,
            ),
            modifier = Modifier.semantics { contentDescription = "Bagažo limitas kilogramais" },
        )

        // ĮSPĖJIMAS: atsiranda su animacija tik viršijus limitą
        AnimatedVisibility(visible = overLimit) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp),
            ) {
                Icon(Icons.Filled.Warning, contentDescription = "Įspėjimas")
                Spacer(Modifier.width(8.dp))
                Text(
                    "Limitas viršytas ${formatKg(weightKg - limitKg)}!",
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}

/** Vieno daikto kortelė. Paspaudus kortelę arba varnelę – pažymima „supakuota“. */
@Composable
private fun GearCard(item: GearItem, onToggle: () -> Unit, onDelete: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Card(
        onClick = onToggle,
        colors = CardDefaults.cardColors(
            containerColor = if (item.packed) colors.primaryContainer else colors.surfaceContainerLow,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
        ) {
            Checkbox(
                checked = item.packed,
                onCheckedChange = { onToggle() },
                modifier = Modifier.semantics { contentDescription = "Supakuota: ${item.name}" },
            )
            Icon(
                if (item.packed) Icons.Filled.Backpack else Icons.Filled.Inventory2,
                contentDescription = null,
                tint = if (item.packed) colors.onPrimaryContainer else colors.onSurfaceVariant,
            )
            Spacer(Modifier.width(12.dp))
            Text(
                item.name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Text(
                formatKg(item.weightKg),
                style = MaterialTheme.typography.labelLarge,
                color = colors.onSurfaceVariant,
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Close, contentDescription = "Šalinti „${item.name}“")
            }
        }
    }
}

/** Dialogas „Naujas daiktas“: pavadinimas ir svoris (priima ir kablelį, ir tašką). */
@Composable
private fun AddGearDialog(onDismiss: () -> Unit, onAdd: (String, String) -> GearInputErrors) {
    var name by rememberSaveable { mutableStateOf("") }
    var weight by rememberSaveable { mutableStateOf("") }
    // Klaidos atsiranda tik paspaudus „Pridėti“
    var errors by remember { mutableStateOf<GearInputErrors?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Filled.Backpack, contentDescription = null) },
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
                    placeholder = { Text("pvz. 0,8") },
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
