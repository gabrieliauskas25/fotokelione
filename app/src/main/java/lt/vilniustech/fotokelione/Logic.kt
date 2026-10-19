package lt.vilniustech.fotokelione

import java.util.Locale

// Grynoji (be Android priklausomybių) programos logika, kad ją būtų paprasta testuoti.

const val NAME_MAX_LENGTH = 40
const val RESERVE_RATE = 0.10

enum class Category(val label: String) {
    NATURE("Gamta"),
    CITY("Miestas"),
    ARCHITECTURE("Architektūra"),
    PORTRAIT("Portretai"),
    NIGHT_SKY("Naktinis dangus"),
}

enum class TimeOfDay(val label: String) {
    SUNRISE("Saulėtekis"),
    DAY("Diena"),
    SUNSET("Saulėlydis"),
    NIGHT("Naktis"),
}

enum class PlaceFilter(val label: String) {
    ALL("Visi"),
    PENDING("Laukia"),
    DONE("Atlikta"),
}

enum class Transport(val label: String, val costPerDay: Double) {
    PUBLIC("Viešasis transportas", 8.0),
    CAR("Automobilio nuoma", 45.0),
    WALK("Pėsčiomis", 0.0),
}

data class Place(
    val id: Long,
    val name: String,
    val category: Category,
    val timeOfDay: TimeOfDay,
    val priority: Int,
    val outdoor: Boolean,
    val done: Boolean = false,
)

data class GearItem(
    val id: Long,
    val name: String,
    val weightKg: Double,
    val packed: Boolean = false,
)

enum class NameError(val message: String) {
    EMPTY("Įveskite pavadinimą"),
    TOO_LONG("Pavadinimas per ilgas (daugiausia $NAME_MAX_LENGTH simbolių)"),
}

/** Grąžina klaidą arba `null`, jei pavadinimas tinkamas. */
fun validateName(name: String): NameError? {
    val trimmed = name.trim()
    return when {
        trimmed.isEmpty() -> NameError.EMPTY
        trimmed.length > NAME_MAX_LENGTH -> NameError.TOO_LONG
        else -> null
    }
}

/**
 * Paverčia vartotojo įvestą sumą skaičiumi. Priimamas ir kablelis, ir taškas.
 * Tuščios, neskaitinės ir neigiamos reikšmės atmetamos (`null`).
 */
fun parseAmount(text: String): Double? {
    val normalized = text.trim().replace(',', '.')
    if (normalized.isEmpty()) return null
    val value = normalized.toDoubleOrNull() ?: return null
    if (value.isNaN() || value.isInfinite() || value < 0) return null
    return value
}

/** Naktų skaičius turi būti sveikas neneigiamas skaičius. */
fun parseNights(text: String): Int? = text.trim().toIntOrNull()?.takeIf { it >= 0 }

/** Dalyba, kuri dalijant iš nulio grąžina 0, o ne begalybę ar NaN. */
fun safeDivide(numerator: Double, denominator: Double): Double =
    if (denominator == 0.0) 0.0 else numerator / denominator

fun filterPlaces(places: List<Place>, filter: PlaceFilter): List<Place> = when (filter) {
    PlaceFilter.ALL -> places
    PlaceFilter.PENDING -> places.filter { !it.done }
    PlaceFilter.DONE -> places.filter { it.done }
}

/** Nufotografuotų vietų dalis nuo 0 iki 1. */
fun placeProgress(places: List<Place>): Float =
    safeDivide(places.count { it.done }.toDouble(), places.size.toDouble()).toFloat()

fun packedWeight(items: List<GearItem>): Double = items.filter { it.packed }.sumOf { it.weightKg }

fun isOverLimit(weightKg: Double, limitKg: Double): Boolean = weightKg > limitKg

data class BudgetInput(
    val nights: Int,
    val flight: Double,
    val hotelPerNight: Double,
    val dailyExpenses: Double,
    val transport: Transport,
    val includeReserve: Boolean,
)

data class BudgetResult(
    val subtotal: Double,
    val reserve: Double,
    val total: Double,
    val perDay: Double,
)

/**
 * Bendra suma = skrydis + naktys × (viešbutis + dienos išlaidos + transportas per dieną),
 * prie kurios pasirinktinai pridedamas 10 % rezervas.
 */
fun calculateBudget(input: BudgetInput): BudgetResult {
    val subtotal = input.flight +
        input.nights * (input.hotelPerNight + input.dailyExpenses + input.transport.costPerDay)
    val reserve = if (input.includeReserve) subtotal * RESERVE_RATE else 0.0
    val total = subtotal + reserve
    return BudgetResult(
        subtotal = subtotal,
        reserve = reserve,
        total = total,
        perDay = safeDivide(total, input.nights.toDouble()),
    )
}

fun formatEuro(value: Double): String = String.format(Locale.ROOT, "%.2f €", value).replace('.', ',')

fun formatKg(value: Double): String = String.format(Locale.ROOT, "%.1f kg", value).replace('.', ',')
