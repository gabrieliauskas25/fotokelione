package lt.vilniustech.fotokelione.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/*
 * ============================================================================
 *  Theme.kt – PROGRAMĖLĖS IŠVAIZDA (spalvos, šriftai, kampų apvalumas)
 * ============================================================================
 *  Material 3 tema: visi elementai (mygtukai, kortelės, juostos) spalvas
 *  ima iš čia, todėl pakeitus spalvą čia – ji pasikeičia visur.
 *
 *  Spalvos parinktos „kelionių“ nuotaikai:
 *   primary   – gilus jūros mėlynai žalsvas (pagrindiniai mygtukai, juostos)
 *   secondary – saulėlydžio oranžinė (žvaigždutės, akcentai)
 *   tertiary  – miško žalia (biudžeto rezultatas)
 *  Yra ir tamsi tema (įsijungia, kai telefone įjungtas tamsus režimas).
 * ============================================================================
 */

private val LightColors = lightColorScheme(
    primary = Color(0xFF006A6A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF9CF1F0),
    onPrimaryContainer = Color(0xFF002020),
    secondary = Color(0xFFB35A1F),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDBC8),
    onSecondaryContainer = Color(0xFF341100),
    tertiary = Color(0xFF3B6939),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFBCF0B4),
    onTertiaryContainer = Color(0xFF002204),
    background = Color(0xFFF5FAF9),
    surface = Color(0xFFF5FAF9),
    surfaceContainerLow = Color(0xFFEFF5F4),
    surfaceContainer = Color(0xFFE9EFEE),
    surfaceContainerHigh = Color(0xFFE3E9E8),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF80D5D4),
    onPrimary = Color(0xFF003737),
    primaryContainer = Color(0xFF004F4F),
    onPrimaryContainer = Color(0xFF9CF1F0),
    secondary = Color(0xFFFFB68B),
    onSecondary = Color(0xFF552000),
    secondaryContainer = Color(0xFF8C430A),
    onSecondaryContainer = Color(0xFFFFDBC8),
    tertiary = Color(0xFFA1D39A),
    onTertiary = Color(0xFF0A390F),
    tertiaryContainer = Color(0xFF235024),
    onTertiaryContainer = Color(0xFFBCF0B4),
)

/** Šiek tiek ryškesnės antraštės nei standartinės. */
private val AppTypography = Typography().let { base ->
    base.copy(
        headlineMedium = base.headlineMedium.copy(fontWeight = FontWeight.Bold),
        titleLarge = base.titleLarge.copy(fontWeight = FontWeight.SemiBold),
        titleMedium = base.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    )
}

/** Apvalesni kampai kortelėms ir dialogams. */
private val AppShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
)

/** Tema, kuria „apgaubiama“ visa programėlė (žr. MainActivity). */
@Composable
fun FotoKelioneTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
