package lt.vilniustech.fotokelione

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * UNIT TESTAI funkcijoms iš Logic.kt.
 * Kiekvienas @Test – atskiras patikrinimas: assertEquals(tikėtasi, gauta).
 * Paleisti: dešiniu pelės mygtuku ant failo → Run 'LogicTest'.
 */
class LogicTest {

    private val delta = 1e-9

    // ---------- Pavadinimo tikrinimas (ribinės reikšmės) ----------

    @Test
    fun emptyNameIsRejected() {
        assertEquals(NameError.EMPTY, validateName(""))
    }

    @Test
    fun blankNameIsRejected() {
        assertEquals(NameError.EMPTY, validateName("   "))
    }

    @Test
    fun singleCharacterNameIsAccepted() {
        assertNull(validateName("A"))
    }

    @Test
    fun nameAtMaxLengthIsAccepted() {
        assertNull(validateName("a".repeat(NAME_MAX_LENGTH)))
    }

    @Test
    fun nameOverMaxLengthIsRejected() {
        assertEquals(NameError.TOO_LONG, validateName("a".repeat(NAME_MAX_LENGTH + 1)))
    }

    @Test
    fun surroundingWhitespaceDoesNotCountTowardsLength() {
        assertNull(validateName("  " + "a".repeat(NAME_MAX_LENGTH) + "  "))
    }

    // ---------- Skaičių įvestis ----------

    @Test
    fun commaIsAcceptedAsDecimalSeparator() {
        assertEquals(12.5, parseAmount("12,5")!!, delta)
    }

    @Test
    fun dotIsAcceptedAsDecimalSeparator() {
        assertEquals(12.5, parseAmount("12.5")!!, delta)
    }

    @Test
    fun whitespaceAroundAmountIsIgnored() {
        assertEquals(7.0, parseAmount("  7 ")!!, delta)
    }

    @Test
    fun zeroAmountIsAccepted() {
        assertEquals(0.0, parseAmount("0")!!, delta)
    }

    @Test
    fun negativeAmountIsRejected() {
        assertNull(parseAmount("-1"))
        assertNull(parseAmount("-0,01"))
    }

    @Test
    fun nonNumericAmountIsRejected() {
        assertNull(parseAmount("abc"))
        assertNull(parseAmount("12,5,3"))
        assertNull(parseAmount("NaN"))
        assertNull(parseAmount("Infinity"))
    }

    @Test
    fun emptyAmountIsRejected() {
        assertNull(parseAmount(""))
        assertNull(parseAmount("  "))
    }

    @Test
    fun nightsMustBeWholeNonNegativeNumber() {
        assertEquals(0, parseNights("0"))
        assertEquals(5, parseNights(" 5 "))
        assertNull(parseNights("-1"))
        assertNull(parseNights("2,5"))
        assertNull(parseNights(""))
    }

    // ---------- Dalyba iš nulio ----------

    @Test
    fun divisionByZeroReturnsZero() {
        assertEquals(0.0, safeDivide(100.0, 0.0), delta)
    }

    @Test
    fun regularDivisionWorks() {
        assertEquals(25.0, safeDivide(100.0, 4.0), delta)
    }

    @Test
    fun progressOfEmptyListIsZero() {
        assertEquals(0f, placeProgress(emptyList()), 0f)
    }

    @Test
    fun budgetWithZeroNightsHasZeroPerDay() {
        val result = calculateBudget(
            BudgetInput(0, flight = 200.0, hotelPerNight = 50.0, dailyExpenses = 30.0, Transport.CAR, includeReserve = false)
        )
        assertEquals(200.0, result.total, delta)
        assertEquals(0.0, result.perDay, delta)
    }

    // ---------- Biudžeto formulė ----------

    @Test
    fun budgetFormulaWithoutReserve() {
        // 150 + 4 × (60 + 25 + 8) = 522
        val result = calculateBudget(
            BudgetInput(4, flight = 150.0, hotelPerNight = 60.0, dailyExpenses = 25.0, Transport.PUBLIC, includeReserve = false)
        )
        assertEquals(522.0, result.subtotal, delta)
        assertEquals(0.0, result.reserve, delta)
        assertEquals(522.0, result.total, delta)
        assertEquals(130.5, result.perDay, delta)
    }

    @Test
    fun budgetFormulaWithReserveAddsTenPercent() {
        // 100 + 2 × (50 + 0 + 45) = 290; rezervas 29; iš viso 319
        val result = calculateBudget(
            BudgetInput(2, flight = 100.0, hotelPerNight = 50.0, dailyExpenses = 0.0, Transport.CAR, includeReserve = true)
        )
        assertEquals(290.0, result.subtotal, delta)
        assertEquals(29.0, result.reserve, delta)
        assertEquals(319.0, result.total, delta)
        assertEquals(159.5, result.perDay, delta)
    }

    @Test
    fun walkingAddsNoTransportCost() {
        val result = calculateBudget(
            BudgetInput(3, flight = 0.0, hotelPerNight = 10.0, dailyExpenses = 10.0, Transport.WALK, includeReserve = false)
        )
        assertEquals(60.0, result.total, delta)
    }

    // ---------- Filtrai ir įranga ----------

    private val places = listOf(
        Place(1, "A", Category.CITY, TimeOfDay.DAY, 3, outdoor = true, done = true),
        Place(2, "B", Category.NATURE, TimeOfDay.SUNSET, 4, outdoor = true),
        Place(3, "C", Category.PORTRAIT, TimeOfDay.NIGHT, 1, outdoor = false),
    )

    @Test
    fun filtersSplitPlacesByStatus() {
        assertEquals(3, filterPlaces(places, PlaceFilter.ALL).size)
        assertEquals(listOf(2L, 3L), filterPlaces(places, PlaceFilter.PENDING).map { it.id })
        assertEquals(listOf(1L), filterPlaces(places, PlaceFilter.DONE).map { it.id })
    }

    @Test
    fun progressIsShareOfDonePlaces() {
        assertEquals(1f / 3f, placeProgress(places), 1e-6f)
    }

    @Test
    fun onlyPackedItemsCountTowardsWeight() {
        val gear = listOf(
            GearItem(1, "Fotoaparatas", 0.7, packed = true),
            GearItem(2, "Trikojis", 1.8),
            GearItem(3, "Objektyvas", 0.9, packed = true),
        )
        assertEquals(1.6, packedWeight(gear), delta)
    }

    @Test
    fun weightEqualToLimitIsNotOverLimit() {
        assertFalse(isOverLimit(8.0, 8.0))
        assertTrue(isOverLimit(8.01, 8.0))
    }

    @Test
    fun formattingUsesComma() {
        assertEquals("12,50 €", formatEuro(12.5))
        assertEquals("1,6 kg", formatKg(1.6))
    }
}
