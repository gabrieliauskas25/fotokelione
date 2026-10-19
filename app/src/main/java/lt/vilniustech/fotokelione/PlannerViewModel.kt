package lt.vilniustech.fotokelione

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

data class BudgetForm(
    val nights: String = "",
    val flight: String = "",
    val hotelPerNight: String = "",
    val dailyExpenses: String = "",
    val transport: Transport = Transport.PUBLIC,
    val includeReserve: Boolean = true,
)

data class GearInputErrors(val name: NameError?, val weightInvalid: Boolean) {
    val isValid: Boolean get() = name == null && !weightInvalid
}

enum class BudgetField { NIGHTS, FLIGHT, HOTEL, DAILY }

class PlannerViewModel : ViewModel() {

    private var nextId = 1L

    // ---------- Planas ----------

    val places = mutableStateListOf(
        Place(nextId++, "Trakų pilis", Category.ARCHITECTURE, TimeOfDay.SUNRISE, 5, outdoor = true),
        Place(nextId++, "Gedimino prospektas", Category.CITY, TimeOfDay.NIGHT, 3, outdoor = true),
        Place(nextId++, "Kuršių nerija", Category.NATURE, TimeOfDay.SUNSET, 4, outdoor = true, done = true),
        Place(nextId++, "Šv. Onos bažnyčia", Category.ARCHITECTURE, TimeOfDay.DAY, 2, outdoor = false),
    )

    var placeFilter by mutableStateOf(PlaceFilter.ALL)

    val visiblePlaces: List<Place> get() = filterPlaces(places, placeFilter)
    val progress: Float get() = placeProgress(places)
    val pendingCount: Int get() = places.count { !it.done }

    /** Grąžina klaidą, jei vieta nepridėta. */
    fun addPlace(
        name: String,
        category: Category,
        timeOfDay: TimeOfDay,
        priority: Int,
        outdoor: Boolean,
    ): NameError? {
        validateName(name)?.let { return it }
        places += Place(nextId++, name.trim(), category, timeOfDay, priority, outdoor)
        return null
    }

    fun togglePlaceDone(id: Long) {
        val index = places.indexOfFirst { it.id == id }
        if (index >= 0) places[index] = places[index].copy(done = !places[index].done)
    }

    /** Pašalina vietą ir grąžina ją su indeksu, kad būtų galima atšaukti. */
    fun removePlace(id: Long): Pair<Int, Place>? {
        val index = places.indexOfFirst { it.id == id }
        if (index < 0) return null
        return index to places.removeAt(index)
    }

    fun restorePlace(index: Int, place: Place) {
        places.add(index.coerceIn(0, places.size), place)
    }

    // ---------- Įranga ----------

    val gear = mutableStateListOf(
        GearItem(nextId++, "Fotoaparatas", 0.7, packed = true),
        GearItem(nextId++, "Objektyvas 24–70 mm", 0.9, packed = true),
        GearItem(nextId++, "Objektyvas 70–200 mm", 1.5),
        GearItem(nextId++, "Trikojis", 1.8),
        GearItem(nextId++, "Dronas", 0.9),
        GearItem(nextId++, "Nešiojamasis kompiuteris", 2.0),
        GearItem(nextId++, "Baterijos ir įkrovikliai", 0.5, packed = true),
        GearItem(nextId++, "Filtrų rinkinys", 0.3),
    )

    var luggageLimitKg by mutableStateOf(8f)

    val packedWeightKg: Double get() = packedWeight(gear)
    val overLimit: Boolean get() = isOverLimit(packedWeightKg, luggageLimitKg.toDouble())
    val unpackedCount: Int get() = gear.count { !it.packed }

    fun toggleGearPacked(id: Long) {
        val index = gear.indexOfFirst { it.id == id }
        if (index >= 0) gear[index] = gear[index].copy(packed = !gear[index].packed)
    }

    /** Prideda daiktą, jei įvestis tinkama, ir grąžina rastas klaidas. */
    fun addGear(name: String, weightText: String): GearInputErrors {
        val weight = parseAmount(weightText)
        val errors = GearInputErrors(validateName(name), weightInvalid = weight == null || weight == 0.0)
        if (errors.isValid) gear += GearItem(nextId++, name.trim(), weight!!)
        return errors
    }

    fun removeGear(id: Long) {
        gear.removeAll { it.id == id }
    }

    // ---------- Biudžetas ----------

    var budgetForm by mutableStateOf(BudgetForm())
        private set
    var budgetErrors by mutableStateOf<Set<BudgetField>>(emptySet())
        private set
    var budgetResult by mutableStateOf<BudgetResult?>(null)
        private set

    fun updateBudget(form: BudgetForm) {
        budgetForm = form
        budgetResult = null
    }

    fun calculate() {
        val form = budgetForm
        val nights = parseNights(form.nights)
        val flight = parseAmount(form.flight)
        val hotel = parseAmount(form.hotelPerNight)
        val daily = parseAmount(form.dailyExpenses)

        budgetErrors = buildSet {
            if (nights == null) add(BudgetField.NIGHTS)
            if (flight == null) add(BudgetField.FLIGHT)
            if (hotel == null) add(BudgetField.HOTEL)
            if (daily == null) add(BudgetField.DAILY)
        }
        budgetResult = if (budgetErrors.isEmpty()) {
            calculateBudget(
                BudgetInput(nights!!, flight!!, hotel!!, daily!!, form.transport, form.includeReserve)
            )
        } else {
            null
        }
    }
}
