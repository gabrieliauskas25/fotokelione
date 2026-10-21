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
    // Ar rodomas dialogas „Naujas daiktas“. rememberSaveable – išlieka pasukus ekraną.
    var showAddDialog by rememberSaveable { mutableStateOf(false) }

    // Box – elementai gali gulėti vienas ant kito (sąrašas + FAB virš jo)
    Box(Modifier.fillMaxSize()) {
        // TINKLELIS: LazyVerticalGrid piešia tik matomus elementus (taupo atmintį)
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 300.dp), // telefone 1 stulpelis, planšetėje daugiau
            // bottom = 96.dp – vieta apačioje, kad FAB neuždengtų paskutinio daikto
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp), // tarpas tarp stulpelių
            verticalArrangement = Arrangement.spacedBy(10.dp),   // tarpas tarp eilučių
            modifier = Modifier.fillMaxSize(),
        ) {
            // Svorio suvestinė viršuje. span = maxLineSpan → užima visą eilutės plotį
            item(span = { GridItemSpan(maxLineSpan) }) {
                WeightSummary(
                    weightKg = viewModel.packedWeightKg,     // supakuotas svoris (packedWeight iš Logic.kt)
                    limitKg = viewModel.luggageLimitKg,      // limitas iš slankiklio
                    overLimit = viewModel.overLimit,         // ar viršyta (isOverLimit iš Logic.kt)
                    packedCount = viewModel.packedCount,     // kiek daiktų supakuota
                    totalCount = viewModel.gear.size,        // kiek daiktų iš viso
                    onLimitChange = { viewModel.luggageLimitKg = it }, // slankiklis → įrašome naują limitą
                )
            }
            // Kiekvienam daiktui – po kortelę. key = id padeda Compose atpažinti elementus.
            items(viewModel.gear, key = { it.id }) { item ->
                GearCard(
                    item = item,
                    onToggle = { viewModel.toggleGearPacked(item.id) }, // varnelė → supakuota / ne
                    onDelete = { viewModel.removeGear(item.id) },       // ✕ → pašalinti
                )
            }
        }

        // FAB – plaukiojantis mygtukas apačioje dešinėje
        ExtendedFloatingActionButton(
            onClick = { showAddDialog = true }, // atidaro dialogą
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            text = { Text("Naujas daiktas") },
            modifier = Modifier
                .align(Alignment.BottomEnd) // Box viduje – į apatinį dešinį kampą
                .padding(16.dp),
        )
    }

    // Dialogas rodomas tik kai showAddDialog = true
    if (showAddDialog) {
        AddGearDialog(
            onDismiss = { showAddDialog = false }, // „Atšaukti“ arba paspaudus šalia
            onAdd = { name, weight ->
                val errors = viewModel.addGear(name, weight) // bandome pridėti
                if (errors.isValid) showAddDialog = false    // pavyko → uždarome dialogą
                errors                                        // grąžiname klaidas dialogui (raudoni laukai)
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
    val colors = MaterialTheme.colorScheme // temos spalvos (Theme.kt)
    // Viršijus limitą kortelės gradientas tampa raudonas–oranžinis
    HeroCard(
        startColor = if (overLimit) colors.error else colors.primary,
        endColor = if (overLimit) colors.secondary else colors.tertiary,
    ) {
        // Row – elementai išdėstomi eilute (iš kairės į dešinę)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) { // tekstai kairėje, užima visą likusią vietą
                Text("Supakuota", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.85f))
                Text(formatKg(weightKg), style = MaterialTheme.typography.headlineMedium) // pvz. „2,1 kg“
                Text("iš ${formatKg(limitKg.toDouble())} · $packedCount/$totalCount daiktų", style = MaterialTheme.typography.bodyMedium)
            }
            // Lagamino ikona dešinėje
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
            // progress – skaičius nuo 0 iki 1: svoris / limitas (pvz. 2,1 / 8 = 0,26 → 26 %)
            // safeDivide – kad nedalintų iš nulio; coerceIn – kad neišeitų už 0..1 ribų
            progress = { safeDivide(weightKg, limitKg.toDouble()).toFloat().coerceIn(0f, 1f) },
            color = Color.White,                         // užpildyta dalis
            trackColor = Color.White.copy(alpha = 0.3f), // neužpildyta dalis (permatoma)
            strokeCap = StrokeCap.Round,                 // apvalūs galai
            gapSize = 0.dp,                              // be tarpo tarp dalių
            drawStopIndicator = {},                      // be taško gale
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
        )
        Spacer(Modifier.height(12.dp))

        // SLANKIKLIS: bagažo limitas nuo 5 iki 30 kg kas 1 kg
        // ${...} – įterpia kintamojo reikšmę į tekstą; roundToInt: 8.0 → 8
        Text("Bagažo limitas: ${limitKg.roundToInt()} kg", style = MaterialTheme.typography.labelLarge)
        Slider(
            value = limitKg, // kur stovi rutuliukas (reikšmė ateina iš ViewModel)
            // pajudinus: it = nauja reikšmė → suapvaliname iki sveikų kg → perduodame į viršų
            onValueChange = { onLimitChange(it.roundToInt().toFloat()) },
            valueRange = 5f..30f, // kairys galas = 5 kg, dešinys = 30 kg
            steps = 24,           // 24 tarpiniai taškai → reikšmės kas 1 kg (5, 6, ..., 30)
            colors = SliderDefaults.colors(
                thumbColor = Color.White,                            // rutuliukas
                activeTrackColor = Color.White,                      // juosta iki rutuliuko
                inactiveTrackColor = Color.White.copy(alpha = 0.3f), // juosta po rutuliuko
                activeTickColor = Color.Transparent,                 // taškeliai paslėpti
                inactiveTickColor = Color.Transparent,
            ),
            // PRIEINAMUMAS: TalkBack perskaitys, kam skirtas slankiklis
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
    // KORTELĖ: paspaudus bet kur – pažymima supakuota / nesupakuota
    Card(
        onClick = onToggle,
        colors = CardDefaults.cardColors(
            // supakuotas daiktas – žalsvas fonas, nesupakuotas – pilkas
            containerColor = if (item.packed) colors.primaryContainer else colors.surfaceContainerLow,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
        ) {
            // ŽYMIMASIS LANGELIS: varnelė = supakuota
            Checkbox(
                checked = item.packed,
                onCheckedChange = { onToggle() },
                // PRIEINAMUMAS: TalkBack pasakys, kokio daikto varnelė
                modifier = Modifier.semantics { contentDescription = "Supakuota: ${item.name}" },
            )
            // Ikona: kuprinė (supakuota) arba dėžė (dar ne)
            Icon(
                if (item.packed) Icons.Filled.Backpack else Icons.Filled.Inventory2,
                contentDescription = null,
                tint = if (item.packed) colors.onPrimaryContainer else colors.onSurfaceVariant,
            )
            Spacer(Modifier.width(12.dp))
            Text(
                item.name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,                     // tik viena eilutė
                overflow = TextOverflow.Ellipsis, // per ilgas tekstas baigiasi „…“
                modifier = Modifier.weight(1f),   // užima visą likusią vietą
            )
            Text(
                formatKg(item.weightKg),
                style = MaterialTheme.typography.labelLarge,
                color = colors.onSurfaceVariant,
            )
            // Šalinimo mygtukas ✕
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Close, contentDescription = "Šalinti „${item.name}“") // PRIEINAMUMAS
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
                // TEKSTINIS LAUKAS: pavadinimas
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it }, // įvedus raidę – atnaujiname būseną
                    label = { Text("Pavadinimas") },
                    singleLine = true,
                    isError = errors?.name != null, // ?. – jei errors null, rezultatas irgi null
                    supportingText = errors?.name?.let { { Text(it.message) } }, // klaidos tekstas
                    modifier = Modifier.fillMaxWidth(),
                )
                // TEKSTINIS LAUKAS: svoris (priima ir kablelį, ir tašką – parseAmount)
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Svoris, kg") },
                    placeholder = { Text("pvz. 0,8") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), // skaičių klaviatūra
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
            // „Pridėti“: bandome pridėti ir įsimename klaidas (jei yra – laukai taps raudoni)
            TextButton(onClick = { errors = onAdd(name, weight) }) { Text("Pridėti") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Atšaukti") }
        },
    )
}
