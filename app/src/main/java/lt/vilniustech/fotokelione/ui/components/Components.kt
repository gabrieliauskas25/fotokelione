package lt.vilniustech.fotokelione.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import lt.vilniustech.fotokelione.Category
import lt.vilniustech.fotokelione.TimeOfDay

/*
 * ============================================================================
 *  Components.kt – BENDRI DIZAINO ELEMENTAI
 * ============================================================================
 *  Maži „kubeliai“, kuriuos naudoja keli ekranai, kad visur būtų vienodas
 *  stilius ir nereikėtų kartoti to paties kodo:
 *
 *   HeroCard       – spalvota antraštės kortelė su gradientu ekrano viršuje
 *   SectionCard    – balta kortelė su pavadinimu (biudžeto formos skyriai)
 *   IconCircle     – ikona spalvotame apskritime (kortelių kairėje)
 *   EmptyState     – pranešimas, kai sąrašas tuščias
 *   Category.icon  – kokia ikona rodoma kiekvienai kategorijai
 *   TimeOfDay.icon – kokia ikona rodoma kiekvienam paros metui
 * ============================================================================
 */

/**
 * Didelė spalvota kortelė su gradientu (perėjimu tarp dviejų spalvų).
 * Naudojama ekranų viršuje: progresas, svoris, biudžeto rezultatas.
 */
@Composable
fun HeroCard(
    modifier: Modifier = Modifier,
    startColor: Color = MaterialTheme.colorScheme.primary,
    endColor: Color = MaterialTheme.colorScheme.tertiary,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent, contentColor = Color.White),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(startColor, endColor)))
                .padding(20.dp),
            content = content,
        )
    }
}

/** Kortelė su pavadinimu ir ikona – formos skyriui. */
@Composable
fun SectionCard(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

/** Ikona spalvotame apskritime. */
@Composable
fun IconCircle(
    icon: ImageVector,
    background: Color,
    tint: Color,
    size: Dp = 44.dp,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(background),
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(size * 0.55f))
    }
}

/** Rodoma, kai sąraše nieko nėra. */
@Composable
fun EmptyState(icon: ImageVector, text: String, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
    ) {
        IconCircle(
            icon = icon,
            background = MaterialTheme.colorScheme.surfaceContainerHigh,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            size = 72.dp,
        )
        Spacer(Modifier.height(12.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** Kokią ikoną rodyti kiekvienai kategorijai. */
val Category.icon: ImageVector
    get() = when (this) {
        Category.NATURE -> Icons.Filled.Forest
        Category.CITY -> Icons.Filled.LocationCity
        Category.ARCHITECTURE -> Icons.Filled.AccountBalance
        Category.PORTRAIT -> Icons.Filled.Face
        Category.NIGHT_SKY -> Icons.Filled.NightsStay
    }

/** Kokią ikoną rodyti kiekvienam paros metui. */
val TimeOfDay.icon: ImageVector
    get() = when (this) {
        TimeOfDay.SUNRISE -> Icons.Filled.WbTwilight
        TimeOfDay.DAY -> Icons.Filled.WbSunny
        TimeOfDay.SUNSET -> Icons.Filled.WbTwilight
        TimeOfDay.NIGHT -> Icons.Filled.Bedtime
    }
