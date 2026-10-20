package lt.vilniustech.fotokelione

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

/*
 * ============================================================================
 *  PlannerViewModel.kt – PROGRAMOS BŪSENA (duomenys, kuriuos rodo ekranai)
 * ============================================================================
 *  ViewModel – tai klasė, kuri laiko ekranų duomenis. Ji „išgyvena“ ekrano
 *  pasukimą, todėl pasukus telefoną sąrašai neišsitrina.
 *
 *  `mutableStateOf` ir `mutableStateListOf` – tai Compose BŪSENA.
 *  Kai jų reikšmė pasikeičia, Compose AUTOMATIŠKAI perpiešia ekraną
 *  (tai vadinama „recomposition“).
 *
 *  Ekranai patys duomenų nekeičia – jie kviečia šios klasės funkcijas
 *  (pvz. addPlace, togglePlaceDone). Skaičiavimams naudojamos funkcijos
 *  iš Logic.kt.
 *
 *  Turinys:
 *   1. PLANAS     – vietos, filtras, progresas
 *   2. ĮRANGA     – daiktai, svoris, bagažo limitas
 *   3. BIUDŽETAS  – formos laukai, klaidos, rezultatas
 * ============================================================================
 */

/** Biudžeto formos laukai TEKSTU (taip, kaip juos įvedė vartotojas). */
data class BudgetForm(
    val nights: String = "",
    val flight: String = "",
    val hotelPerNight: String = "",
    val dailyExpenses: String = "",
    val transport: Transport = Transport.PUBLIC,
    val includeReserve: Boolean = true,
)

/** Biudžeto laukai, kuriuose gali būti klaida (raudonas laukas). */
enum class BudgetField { NIGHTS, FLIGHT, HOTEL, DAILY }

/** Naujo daikto formos klaidos. */
data class GearInputErrors(val name: NameError?, val weightInvalid: Boolean) {
    val isValid: Boolean get() = name == null && !weightInvalid
}

class PlannerViewModel : ViewModel() {

    /** Skaitliukas unikaliems id: kiekvienas naujas elementas gauna nextId++. */
    private var nextId = 1L

    // =======================================================================
    // 1. PLANAS
    // =======================================================================

    /** Visų vietų sąrašas (su pradiniais pavyzdžiais). */
    val places = mutableStateListOf(
        Place(nextId++, "Trakų pilis", Category.ARCHITECTURE, TimeOfDay.SUNRISE, 5, outdoor = true),
        Place(nextId++, "Gedimino prospektas", Category.CITY, TimeOfDay.NIGHT, 3, outdoor = true),
        Place(nextId++, "Kuršių nerija", Category.NATURE, TimeOfDay.SUNSET, 4, outdoor = true, done = true),
        Place(nextId++, "Šv. Onos bažnyčia", Category.ARCHITECTURE, TimeOfDay.DAY, 2, outdoor = false),
    )

    /** Šiuo metu pasirinktas filtras (Visi / Laukia / Atlikta). */
    var placeFilter by mutableStateOf(PlaceFilter.ALL)

    /** Vietos, kurias reikia rodyti pagal filtrą. */
    val visiblePlaces: List<Place> get() = filterPlaces(places, placeFilter)

    /** Progresas nuo 0 iki 1 (progreso juostai). */
    val progress: Float get() = placeProgress(places)

    val doneCount: Int get() = places.count { it.done }

    /** Kiek vietų dar laukia (rodoma ženkliuke – Badge). */
    val pendingCount: Int get() = places.count { !it.done }

    /** Kiek vietų atitinka filtrą (rodoma ant filtro žymos). */
    fun countFor(filter: PlaceFilter): Int = filterPlaces(places, filter).size

    /**
     * Prideda naują vietą.
     * Jei pavadinimas netinka – vietos neprideda ir grąžina klaidą.
     * Jei viskas gerai – grąžina `null`.
     */
    fun addPlace(
        name: String,
        category: Category,
        timeOfDay: TimeOfDay,
        priority: Int,
        outdoor: Boolean,
    ): NameError? {
        val error = validateName(name)
        if (error != null) return error

        places += Place(nextId++, name.trim(), category, timeOfDay, priority, outdoor)
        return null
    }

    /** Pažymi vietą atlikta arba nuima žymą (varnelė). */
    fun togglePlaceDone(id: Long) {
        val index = places.indexOfFirst { it.id == id }
        if (index >= 0) {
            places[index] = places[index].copy(done = !places[index].done)
        }
    }

    /**
     * Pašalina vietą. Grąžina jos buvusią poziciją ir pačią vietą,
     * kad paspaudus „Atšaukti“ (Snackbar) galėtume ją grąžinti atgal.
     */
    fun removePlace(id: Long): Pair<Int, Place>? {
        val index = places.indexOfFirst { it.id == id }
        if (index < 0) return null
        val removed = places.removeAt(index)
        return index to removed
    }

    /** Grąžina pašalintą vietą į tą pačią vietą sąraše („Atšaukti“). */
    fun restorePlace(index: Int, place: Place) {
        places.add(index.coerceIn(0, places.size), place)
    }

    // =======================================================================
    // 2. ĮRANGA
    // =======================================================================

    /** Visų daiktų sąrašas (su pradiniais pavyzdžiais). */
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

    /** Bagažo limitas kilogramais (keičiamas slankikliu). */
    var luggageLimitKg by mutableStateOf(8f)

    /** Supakuotų daiktų svoris. */
    val packedWeightKg: Double get() = packedWeight(gear)

    /** Ar viršytas limitas (tada rodomas įspėjimas). */
    val overLimit: Boolean get() = isOverLimit(packedWeightKg, luggageLimitKg.toDouble())

    val packedCount: Int get() = gear.count { it.packed }

    /** Kiek daiktų dar nesupakuota (rodoma ženkliuke – Badge). */
    val unpackedCount: Int get() = gear.count { !it.packed }

    /** Pažymi daiktą supakuotu arba nuima žymą. */
    fun toggleGearPacked(id: Long) {
        val index = gear.indexOfFirst { it.id == id }
        if (index >= 0) {
            gear[index] = gear[index].copy(packed = !gear[index].packed)
        }
    }

    /**
     * Prideda daiktą, jei pavadinimas ir svoris tinkami.
     * Grąžina klaidas, kad dialogas galėtų pažymėti blogus laukus.
     */
    fun addGear(name: String, weightText: String): GearInputErrors {
        val weight = parseAmount(weightText)
        val errors = GearInputErrors(
            name = validateName(name),
            weightInvalid = weight == null || weight == 0.0, // svoris turi būti > 0
        )
        if (errors.isValid) {
            gear += GearItem(nextId++, name.trim(), weight!!)
        }
        return errors
    }

    /** Pašalina daiktą. */
    fun removeGear(id: Long) {
        gear.removeAll { it.id == id }
    }

    // =======================================================================
    // 3. BIUDŽETAS
    // =======================================================================

    /** Ką vartotojas įvedė formoje. */
    var budgetForm by mutableStateOf(BudgetForm())
        private set

    /** Kurie laukai neteisingi (jie bus raudoni). */
    var budgetErrors by mutableStateOf<Set<BudgetField>>(emptySet())
        private set

    /** Skaičiavimo rezultatas (`null` – dar neskaičiuota). */
    var budgetResult by mutableStateOf<BudgetResult?>(null)
        private set

    /** Kviečiama kaskart, kai vartotojas pakeičia bet kurį formos lauką. */
    fun updateBudget(form: BudgetForm) {
        budgetForm = form
        budgetResult = null // pakeitus duomenis senas rezultatas nebegalioja
    }

    /**
     * Mygtukas „Skaičiuoti“:
     *  1) tekstą paverčia skaičiais (parseNights / parseAmount),
     *  2) jei kuris nors laukas blogas – pažymi klaidą,
     *  3) jei visi geri – skaičiuoja pagal calculateBudget (Logic.kt).
     */
    fun calculate() {
        val form = budgetForm
        val nights = parseNights(form.nights)
        val flight = parseAmount(form.flight)
        val hotel = parseAmount(form.hotelPerNight)
        val daily = parseAmount(form.dailyExpenses)

        val errors = mutableSetOf<BudgetField>()
        if (nights == null) errors += BudgetField.NIGHTS
        if (flight == null) errors += BudgetField.FLIGHT
        if (hotel == null) errors += BudgetField.HOTEL
        if (daily == null) errors += BudgetField.DAILY
        budgetErrors = errors

        budgetResult = if (errors.isEmpty()) {
            calculateBudget(
                BudgetInput(
                    nights = nights!!,
                    flight = flight!!,
                    hotelPerNight = hotel!!,
                    dailyExpenses = daily!!,
                    transport = form.transport,
                    includeReserve = form.includeReserve,
                )
            )
        } else {
            null
        }
    }
}
