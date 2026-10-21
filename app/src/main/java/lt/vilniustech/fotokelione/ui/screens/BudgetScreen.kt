package lt.vilniustech.fotokelione.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Commute
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import lt.vilniustech.fotokelione.BudgetField
import lt.vilniustech.fotokelione.BudgetResult
import lt.vilniustech.fotokelione.PlannerViewModel
import lt.vilniustech.fotokelione.Transport
import lt.vilniustech.fotokelione.formatEuro
import lt.vilniustech.fotokelione.ui.components.HeroCard
import lt.vilniustech.fotokelione.ui.components.SectionCard

/*
 * ============================================================================
 *  BudgetScreen.kt – 3 EKRANAS „BIUDŽETAS“ (skaičiuoklė)
 * ============================================================================
 *  Ekrano dalys:
 *   SectionCard „Kelionė“     – naktys, skrydžio kaina (tekstiniai laukai)
 *   SectionCard „Išlaidos“    – viešbutis už naktį, dienos išlaidos
 *   SectionCard „Transportas“ – radijo mygtukai + jungiklis „10 % rezervas“
 *   Mygtukas „Skaičiuoti“     – kviečia viewModel.calculate()
 *   ResultCard                – rezultatas, atsiranda su AnimatedVisibility,
 *                               ekranas pats nuslenka iki jo (LaunchedEffect)
 *
 *  Skaičių tikrinimas: parseAmount / parseNights (Logic.kt).
 *  Formulė: calculateBudget (Logic.kt).
 * ============================================================================
 */

@Composable
fun BudgetScreen(viewModel: PlannerViewModel) {
    val form = viewModel.budgetForm         // ką vartotojas įvedė (tekstas iš laukų)
    val errors = viewModel.budgetErrors     // kurie laukai neteisingi (bus raudoni)
    val scrollState = rememberScrollState() // slinkimo padėtis (kiek nuslinkta žemyn)

    // Kai atsiranda rezultatas – ekranas pats nuslenka žemyn, kad jį matytume.
    // Kortelė išsiskleidžia palaipsniui, todėl sekame, kaip didėja maxValue.
    // LaunchedEffect paleidžia kodą kaskart, kai pasikeičia budgetResult.
    LaunchedEffect(viewModel.budgetResult) {
        if (viewModel.budgetResult != null) { // tik kai rezultatas atsirado (ne kai dingo)
            // snapshotFlow stebi maxValue (kiek toli galima nuslinkti);
            // kai jis didėja – animuotai nuslenkame iki apačios
            snapshotFlow { scrollState.maxValue }.collectLatest { scrollState.animateScrollTo(it) }
        }
    }

    // Column – elementai išdėstomi stulpeliu (vienas po kito iš viršaus į apačią)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally, // centruojame horizontaliai
        modifier = Modifier
            .fillMaxSize()               // užima visą ekraną
            .verticalScroll(scrollState) // ekraną galima slinkti
            .padding(horizontal = 16.dp, vertical = 8.dp), // tarpai nuo kraštų
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp), // 12dp tarpas tarp kortelių
            // planšetėje forma neišsitempia per visą ekraną
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxWidth(),
        ) {
            // ---------- 1 SKYRIUS: Kelionė ----------
            SectionCard(title = "Kelionė", icon = Icons.Filled.Flight) {
                // TEKSTINIS LAUKAS: naktų skaičius
                AmountField(
                    value = form.nights, // ką rodyti lauke
                    // įvedus raidę: sukuriame formos kopiją su nauju lauku ir atiduodame ViewModel'iui
                    onValueChange = { viewModel.updateBudget(form.copy(nights = it)) },
                    label = "Naktų skaičius",
                    icon = Icons.Filled.Nightlight,
                    isError = BudgetField.NIGHTS in errors, // raudonas, jei klaida šiame lauke
                    errorText = "Įveskite sveiką neneigiamą skaičių",
                    keyboardType = KeyboardType.Number, // klaviatūra tik su skaičiais
                )
                // TEKSTINIS LAUKAS: skrydžio kaina
                AmountField(
                    value = form.flight,
                    onValueChange = { viewModel.updateBudget(form.copy(flight = it)) },
                    label = "Skrydžio kaina, €",
                    icon = Icons.Filled.Flight,
                    isError = BudgetField.FLIGHT in errors,
                )
            }

            // ---------- 2 SKYRIUS: Išlaidos ----------
            SectionCard(title = "Išlaidos", icon = Icons.Filled.Hotel) {
                // TEKSTINIS LAUKAS: viešbutis už naktį
                AmountField(
                    value = form.hotelPerNight,
                    onValueChange = { viewModel.updateBudget(form.copy(hotelPerNight = it)) },
                    label = "Viešbutis už naktį, €",
                    icon = Icons.Filled.Hotel,
                    isError = BudgetField.HOTEL in errors,
                )
                // TEKSTINIS LAUKAS: dienos išlaidos (maistas ir kt.)
                AmountField(
                    value = form.dailyExpenses,
                    onValueChange = { viewModel.updateBudget(form.copy(dailyExpenses = it)) },
                    label = "Dienos išlaidos, €",
                    icon = Icons.Filled.Restaurant,
                    isError = BudgetField.DAILY in errors,
                )
            }

            // ---------- 3 SKYRIUS: Transportas ----------
            SectionCard(title = "Transportas", icon = Icons.Filled.Commute) {
                // RADIJO MYGTUKAI: galima pasirinkti tik vieną transporto būdą
                // selectableGroup – pasako prieinamumui, kad tai viena pasirinkimų grupė
                Column(Modifier.selectableGroup()) {
                    Transport.entries.forEach { option -> // kiekvienam transporto būdui – po eilutę
                        TransportOption(
                            transport = option,
                            selected = form.transport == option, // pažymėtas, jei sutampa su pasirinktu
                            onSelect = { viewModel.updateBudget(form.copy(transport = option)) },
                        )
                    }
                }
                HorizontalDivider(Modifier.padding(vertical = 8.dp)) // plona skiriamoji linija
                // JUNGIKLIS: pridėti 10 % rezervą
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Savings, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                    Spacer(Modifier.width(12.dp))
                    Text("Pridėti 10 % rezervą", Modifier.weight(1f))
                    Switch(
                        checked = form.includeReserve, // įjungtas ar išjungtas
                        // it = nauja reikšmė (true/false) – įrašome į formą
                        onCheckedChange = { viewModel.updateBudget(form.copy(includeReserve = it)) },
                        modifier = Modifier.semantics { contentDescription = "Pridėti 10 % rezervą" },
                    )
                }
            }

            // MYGTUKAS „Skaičiuoti“
            Button(
                onClick = viewModel::calculate, // paspaudus kviečiama PlannerViewModel.calculate()
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            ) {
                Icon(Icons.Filled.Calculate, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Skaičiuoti", style = MaterialTheme.typography.titleMedium)
            }

            // REZULTATAS: atsiranda su animacija (fadeIn + expandVertically)
            val result = viewModel.budgetResult // null – dar neskaičiuota arba yra klaidų
            // Paskutinis rezultatas prisimenamas, kad nedingtų išnykimo animacijos metu.
            var shownResult by remember { mutableStateOf(result) }
            if (result != null) shownResult = result
            AnimatedVisibility(
                visible = result != null,               // rodome tik kai yra rezultatas
                enter = fadeIn() + expandVertically(),  // atsiranda: išryškėja + išsiskleidžia
                exit = fadeOut() + shrinkVertically(),  // dingsta: išblunka + susitraukia
            ) {
                shownResult?.let { ResultCard(it) }
            }

            Spacer(Modifier.height(24.dp)) // tarpas apačioje, kad niekas nesislėptų
        }
    }
}

/** Skaičiaus įvedimo laukas su ikona ir klaidos tekstu. */
@Composable
private fun AmountField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    isError: Boolean,
    errorText: String = "Įveskite neneigiamą skaičių (pvz., 12,50)",
    keyboardType: KeyboardType = KeyboardType.Decimal,
) {
    OutlinedTextField(
        value = value,                  // dabartinis tekstas lauke
        onValueChange = onValueChange,  // kviečiama įvedus kiekvieną simbolį
        label = { Text(label) },        // užrašas lauko viršuje
        leadingIcon = { Icon(icon, contentDescription = null) }, // ikona kairėje (dekoratyvi)
        singleLine = true,              // tik viena eilutė
        isError = isError,              // true – laukas raudonas
        // supportingText – tekstas po lauku; rodome tik kai yra klaida
        supportingText = if (isError) {
            { Text(errorText) }
        } else {
            null
        },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType), // skaičių klaviatūra
        modifier = Modifier.fillMaxWidth(), // per visą plotį
    )
}

/** Viena transporto pasirinkimo eilutė: ○ ikona  pavadinimas   kaina/d. */
@Composable
private fun TransportOption(transport: Transport, selected: Boolean, onSelect: () -> Unit) {
    // Kiekvienam transporto būdui – sava ikona
    val icon = when (transport) {
        Transport.PUBLIC -> Icons.Filled.DirectionsBus
        Transport.CAR -> Icons.Filled.DirectionsCar
        Transport.WALK -> Icons.AutoMirrored.Filled.DirectionsWalk
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            // visa eilutė paspaudžiama (ne tik mažas apskritimas);
            // role = RadioButton – TalkBack pasako, kad tai radijo mygtukas
            .selectable(selected = selected, onClick = onSelect, role = Role.RadioButton)
            .padding(vertical = 6.dp),
    ) {
        // onClick = null, nes paspaudimą jau tvarko visa eilutė (.selectable)
        RadioButton(selected = selected, onClick = null)
        Spacer(Modifier.width(8.dp))
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(8.dp))
        Text(transport.label, Modifier.weight(1f)) // weight(1f) – užima visą likusią vietą
        Text(
            "${formatEuro(transport.costPerDay)} / d.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Rezultato kortelė: tarpinė suma, rezervas, IŠ VISO, vidurkis per dieną. */
@Composable
private fun ResultCard(result: BudgetResult) {
    HeroCard(
        startColor = MaterialTheme.colorScheme.tertiary,
        endColor = MaterialTheme.colorScheme.primary,
    ) {
        Text("Iš viso", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.85f))
        Text(formatEuro(result.total), style = MaterialTheme.typography.headlineMedium) // didelė suma
        Spacer(Modifier.height(12.dp))
        ResultRow("Tarpinė suma", formatEuro(result.subtotal))
        // rezervo eilutę rodome tik jei rezervas įjungtas
        if (result.reserve > 0) ResultRow("Rezervas (10 %)", formatEuro(result.reserve))
        ResultRow("Vidutiniškai per dieną", formatEuro(result.perDay))
    }
}

/** Viena rezultato eilutė: pavadinimas kairėje, suma dešinėje. */
@Composable
private fun ResultRow(label: String, value: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
    ) {
        // weight(1f) nustumia sumą į dešinį kraštą
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.titleMedium)
    }
}
