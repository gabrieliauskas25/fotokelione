package lt.vilniustech.fotokelione

import java.util.Locale

/*
 * ============================================================================
 *  Logic.kt – PROGRAMOS LOGIKA (be jokio UI)
 * ============================================================================
 *  Čia yra tik duomenų klasės ir paprastos funkcijos: jos gauna reikšmes ir
 *  grąžina rezultatą. Jos nežino nieko apie ekranus, mygtukus ar Android.
 *
 *  Kodėl taip? Tokias funkcijas labai lengva patikrinti unit testais
 *  (žr. app/src/test/.../LogicTest.kt) – nereikia paleisti programėlės.
 *
 *  Turinys:
 *   1. Konstantos
 *   2. Pasirinkimų sąrašai (enum): kategorija, paros metas, filtras, transportas
 *   3. Duomenų klasės: Place (vieta), GearItem (daiktas)
 *   4. Įvesties tikrinimas: validateName, parseAmount, parseNights
 *   5. Skaičiavimai: safeDivide, filterPlaces, placeProgress, packedWeight ...
 *   6. Biudžeto formulė: calculateBudget
 *   7. Formatavimas: formatEuro, formatKg
 * ============================================================================
 */

// ---------------------------------------------------------------------------
// 1. KONSTANTOS
// ---------------------------------------------------------------------------

/** Ilgiausias leidžiamas pavadinimas (simboliais). */
const val NAME_MAX_LENGTH = 40

/** Rezervas biudžete: 10 % = 0.10. */
const val RESERVE_RATE = 0.10

// ---------------------------------------------------------------------------
// 2. PASIRINKIMŲ SĄRAŠAI (enum)
//    enum – tai fiksuotas reikšmių sąrašas. Kiekviena reikšmė turi lietuvišką
//    pavadinimą `label`, kurį rodome ekrane.
// ---------------------------------------------------------------------------

/** Vietos kategorija (rodoma išskleidžiamame sąraše). */
enum class Category(val label: String) {
    NATURE("Gamta"),
    CITY("Miestas"),
    ARCHITECTURE("Architektūra"),
    PORTRAIT("Portretai"),
    NIGHT_SKY("Naktinis dangus"),
}

/** Paros metas fotografavimui (rodomas radijo mygtukais). */
enum class TimeOfDay(val label: String) {
    SUNRISE("Saulėtekis"),
    DAY("Diena"),
    SUNSET("Saulėlydis"),
    NIGHT("Naktis"),
}

/** Filtras plano ekrane (rodomas filtro žymomis – FilterChip). */
enum class PlaceFilter(val label: String) {
    ALL("Visi"),
    PENDING("Laukia"),
    DONE("Atlikta"),
}

/** Transporto būdas ir jo kaina per dieną eurais. */
enum class Transport(val label: String, val costPerDay: Double) {
    PUBLIC("Viešasis transportas", 8.0),
    CAR("Automobilio nuoma", 45.0),
    WALK("Pėsčiomis", 0.0),
}

// ---------------------------------------------------------------------------
// 3. DUOMENŲ KLASĖS
//    data class – klasė duomenims laikyti. `copy(...)` leidžia sukurti kopiją
//    su pakeistu lauku, pvz. place.copy(done = true).
// ---------------------------------------------------------------------------

/** Viena fotografavimo vieta plano sąraše. */
data class Place(
    val id: Long,                 // unikalus numeris (kad žinotume, kurią vietą keisti)
    val name: String,             // pavadinimas, pvz. "Trakų pilis"
    val category: Category,       // kategorija
    val timeOfDay: TimeOfDay,     // kada geriausia fotografuoti
    val priority: Int,            // prioritetas nuo 1 iki 5
    val outdoor: Boolean,         // true – lauke, false – patalpoje
    val done: Boolean = false,    // ar jau nufotografuota
)

/** Vienas įrangos daiktas. */
data class GearItem(
    val id: Long,
    val name: String,
    val weightKg: Double,         // svoris kilogramais
    val packed: Boolean = false,  // ar jau supakuota
)

// ---------------------------------------------------------------------------
// 4. ĮVESTIES TIKRINIMAS
// ---------------------------------------------------------------------------

/** Galimos pavadinimo klaidos ir jų tekstas vartotojui. */
enum class NameError(val message: String) {
    EMPTY("Įveskite pavadinimą"),
    TOO_LONG("Pavadinimas per ilgas (daugiausia $NAME_MAX_LENGTH simbolių)"),
}

/**
 * Patikrina pavadinimą.
 * Grąžina klaidą arba `null`, jei pavadinimas tinkamas.
 *
 * Pavyzdžiai:  ""  → EMPTY,   "   " → EMPTY,   41 simbolis → TOO_LONG,   "Trakai" → null
 */
fun validateName(name: String): NameError? {
    val trimmed = name.trim() // nuimame tarpus pradžioje ir gale
    return when {
        trimmed.isEmpty() -> NameError.EMPTY
        trimmed.length > NAME_MAX_LENGTH -> NameError.TOO_LONG
        else -> null
    }
}

/**
 * Paverčia vartotojo įvestą tekstą skaičiumi (pvz. sumą eurais).
 *  - Priima ir kablelį, ir tašką:  "12,5" → 12.5,  "12.5" → 12.5
 *  - Atmeta (grąžina `null`): tuščią tekstą, raides, neigiamus skaičius.
 */
fun parseAmount(text: String): Double? {
    val normalized = text.trim().replace(',', '.') // kablelį keičiame tašku
    if (normalized.isEmpty()) return null
    val value = normalized.toDoubleOrNull() ?: return null // ne skaičius → null
    if (value.isNaN() || value.isInfinite() || value < 0) return null
    return value
}

/** Naktų skaičius turi būti sveikas ir ne neigiamas: "5" → 5, "2,5" → null, "-1" → null. */
fun parseNights(text: String): Int? = text.trim().toIntOrNull()?.takeIf { it >= 0 }

// ---------------------------------------------------------------------------
// 5. SKAIČIAVIMAI
// ---------------------------------------------------------------------------

/**
 * Saugi dalyba: dalijant iš nulio grąžina 0 (o ne klaidą ar begalybę).
 * Naudojama progresui ir vidurkiui per dieną skaičiuoti.
 */
fun safeDivide(numerator: Double, denominator: Double): Double =
    if (denominator == 0.0) 0.0 else numerator / denominator

/** Grąžina tik tas vietas, kurios atitinka pasirinktą filtrą. */
fun filterPlaces(places: List<Place>, filter: PlaceFilter): List<Place> = when (filter) {
    PlaceFilter.ALL -> places
    PlaceFilter.PENDING -> places.filter { !it.done }
    PlaceFilter.DONE -> places.filter { it.done }
}

/** Kokia dalis vietų jau nufotografuota: nuo 0.0 (nieko) iki 1.0 (visos). */
fun placeProgress(places: List<Place>): Float =
    safeDivide(places.count { it.done }.toDouble(), places.size.toDouble()).toFloat()

/** Sudeda tik SUPAKUOTŲ daiktų svorį. */
fun packedWeight(items: List<GearItem>): Double = items.filter { it.packed }.sumOf { it.weightKg }

/** Ar svoris viršija limitą (lygus limitui – dar ne viršija). */
fun isOverLimit(weightKg: Double, limitKg: Double): Boolean = weightKg > limitKg

// ---------------------------------------------------------------------------
// 6. BIUDŽETO FORMULĖ
// ---------------------------------------------------------------------------

/** Biudžeto skaičiavimo įvestis (jau patikrinti skaičiai). */
data class BudgetInput(
    val nights: Int,
    val flight: Double,
    val hotelPerNight: Double,
    val dailyExpenses: Double,
    val transport: Transport,
    val includeReserve: Boolean,
)

/** Biudžeto skaičiavimo rezultatas. */
data class BudgetResult(
    val subtotal: Double, // suma be rezervo
    val reserve: Double,  // rezervas (0, jei išjungtas)
    val total: Double,    // galutinė suma
    val perDay: Double,   // vidurkis per dieną
)

/**
 * PAGRINDINĖ BIUDŽETO FORMULĖ:
 *
 *   tarpinė suma = skrydis + naktys × (viešbutis + dienos išlaidos + transportas per dieną)
 *   rezervas     = tarpinė suma × 10 %   (jei įjungtas)
 *   iš viso      = tarpinė suma + rezervas
 *   per dieną    = iš viso / naktys      (jei naktų 0 – rodome 0)
 *
 * Pavyzdys: 4 naktys, skrydis 150 €, viešbutis 60 €, išlaidos 25 €, viešasis transportas 8 €:
 *   150 + 4 × (60 + 25 + 8) = 522 €
 */
fun calculateBudget(input: BudgetInput): BudgetResult {
    val costPerNight = input.hotelPerNight + input.dailyExpenses + input.transport.costPerDay
    val subtotal = input.flight + input.nights * costPerNight
    val reserve = if (input.includeReserve) subtotal * RESERVE_RATE else 0.0
    val total = subtotal + reserve
    return BudgetResult(
        subtotal = subtotal,
        reserve = reserve,
        total = total,
        perDay = safeDivide(total, input.nights.toDouble()),
    )
}

// ---------------------------------------------------------------------------
// 7. FORMATAVIMAS (skaičius → gražus tekstas su kableliu)
// ---------------------------------------------------------------------------

/** 12.5 → "12,50 €" */
fun formatEuro(value: Double): String = String.format(Locale.ROOT, "%.2f €", value).replace('.', ',')

/** 1.6 → "1,6 kg" */
fun formatKg(value: Double): String = String.format(Locale.ROOT, "%.1f kg", value).replace('.', ',')
