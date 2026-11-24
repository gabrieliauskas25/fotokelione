package lt.vilniustech.fotokelione.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ContactPage
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.ContactPage
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import lt.vilniustech.fotokelione.PlannerViewModel
import lt.vilniustech.fotokelione.R
import lt.vilniustech.fotokelione.ui.screens.BudgetScreen
import lt.vilniustech.fotokelione.ui.screens.CardScreen
import lt.vilniustech.fotokelione.ui.screens.GearScreen
import lt.vilniustech.fotokelione.ui.screens.PlanScreen

/*
 * ============================================================================
 *  FotoKelioneApp.kt – PROGRAMĖLĖS „RĖMAS“
 * ============================================================================
 *  Čia sujungiama visa programėlė:
 *   - Top App Bar (viršutinė juosta) su meniu „Autorius“ → AuthorDialog
 *     ir „Kalba“ → perjungia lietuvių / anglų kalbą (switchLanguage)
 *   - NAVIGACIJA tarp 4 ekranų (NavHost):
 *       telefone  → apatinė juosta  NavigationBar
 *       planšetėje → šoninė juosta  NavigationRail
 *   - Ženkliukai (Badge) ant navigacijos ikonų: kiek liko nepadaryta
 *   - Snackbar vieta (pranešimai apačioje)
 *
 *  Planas, Įranga ir Biudžetas naudoja TĄ PATĮ PlannerViewModel, todėl
 *  duomenys išlieka pereinant iš vieno ekrano į kitą. Kortelė – CardViewModel.
 *
 *  Visi tekstai – res/values/strings.xml (LT) ir res/values-en/strings.xml (EN).
 * ============================================================================
 */

// Autoriaus duomenys (rodomi dialoge „Autorius“)
const val AUTHOR_NAME = "Gabrielius"
const val AUTHOR_SURNAME = "Vaišvila"
const val AUTHOR_GROUP = "MKDf-23/2"
const val AUTHOR_UNIVERSITY = "VILNIUS TECH"
// ČIA ĮRAŠYKITE SAVO EL. PAŠTĄ – jis rodomas kortelėje ir į jį siunčiama kontakto forma
const val AUTHOR_EMAIL = "gabrieliusmhm25@gmail.com"

/**
 * Keturi programėlės ekranai.
 * route – ekrano „adresas“ navigacijai, label – pavadinimas (strings.xml),
 * icon / selectedIcon – ikona (pasirinkta ikona užpildyta).
 */
private enum class Destination(
    val route: String,
    @StringRes val label: Int,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
) {
    PLAN("plan", R.string.nav_plan, Icons.Outlined.Map, Icons.Filled.Map),
    GEAR("gear", R.string.nav_gear, Icons.Outlined.CameraAlt, Icons.Filled.CameraAlt),
    BUDGET("budget", R.string.nav_budget, Icons.Outlined.AccountBalanceWallet, Icons.Filled.AccountBalanceWallet),
    CARD("card", R.string.nav_card, Icons.Outlined.ContactPage, Icons.Filled.ContactPage),
}

/**
 * KALBOS PERJUNGIMAS: jei dabar angliškai – perjungia į lietuvių, kitaip – į anglų.
 * AppCompatDelegate pats perkrauna ekraną su kitos kalbos strings.xml
 * ir įsimena pasirinkimą (išlieka paleidus programėlę iš naujo).
 */
private fun switchLanguage(currentLanguage: String) {
    val newLanguage = if (currentLanguage == "en") "lt" else "en"
    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(newLanguage))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FotoKelioneApp(
    windowSizeClass: WindowSizeClass,
    viewModel: PlannerViewModel = viewModel(),
) {
    // Navigacijos valdiklis: žino, kuris ekranas atidarytas
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val snackbarHostState = remember { SnackbarHostState() }

    // KINTAMAS EKRANO DYDIS: jei ekranas platus (planšetė) – rodome NavigationRail
    val useRail = windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact

    var menuExpanded by remember { mutableStateOf(false) }
    var showAuthor by rememberSaveable { mutableStateOf(false) }
    // Dabartinė kalba, pvz. "lt" arba "en"
    val currentLanguage = LocalConfiguration.current.locales[0].language

    /** Pereina į kitą ekraną (nekaupdamas begalinės „atgal“ istorijos). */
    fun navigate(destination: Destination) {
        navController.navigate(destination.route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    /** Skaičius ženkliukui ant ikonos (0 – ženkliuko nerodome). */
    fun badgeCount(destination: Destination): Int = when (destination) {
        Destination.PLAN -> viewModel.pendingCount
        Destination.GEAR -> viewModel.unpackedCount
        Destination.BUDGET, Destination.CARD -> 0
    }

    Scaffold(
        // ---------- TOP APP BAR ----------
        topBar = {
            // Juosta su spalvotu gradientu ir užapvalintais apatiniais kampais
            val colors = MaterialTheme.colorScheme
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.PhotoCamera, contentDescription = null)
                        Spacer(Modifier.width(10.dp))
                        Text(stringResource(R.string.app_name), fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent, // fonas – gradientas žemiau (modifier)
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White,
                ),
                modifier = Modifier
                    .shadow(8.dp, RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                    .background(
                        Brush.horizontalGradient(listOf(colors.primary, colors.tertiary)),
                        RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                    ),
                actions = {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = stringResource(R.string.more_options))
                    }
                    // MENIU su punktais „Autorius“ ir „Kalba“
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_author)) },
                            leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                showAuthor = true
                            },
                        )
                        // KALBOS PERJUNGIKLIS
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_language)) },
                            leadingIcon = { Icon(Icons.Filled.Language, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                switchLanguage(currentLanguage)
                            },
                        )
                    }
                },
            )
        },
        // ---------- APATINĖ JUOSTA (tik telefone) ----------
        bottomBar = {
            if (!useRail) {
                NavigationBar(modifier = Modifier.shadow(12.dp)) {
                    Destination.entries.forEach { destination ->
                        val selected = currentRoute == destination.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = { navigate(destination) },
                            icon = { DestinationIcon(destination, selected, badgeCount(destination)) },
                            label = { Text(stringResource(destination.label)) },
                        )
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Row(
            Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // ---------- ŠONINĖ JUOSTA (tik planšetėje) ----------
            if (useRail) {
                NavigationRail(windowInsets = WindowInsets(0.dp)) {
                    Destination.entries.forEach { destination ->
                        val selected = currentRoute == destination.route
                        NavigationRailItem(
                            selected = selected,
                            onClick = { navigate(destination) },
                            icon = { DestinationIcon(destination, selected, badgeCount(destination)) },
                            label = { Text(stringResource(destination.label)) },
                        )
                    }
                }
            }

            // ---------- NAVHOST: kuris ekranas rodomas ----------
            NavHost(
                navController = navController,
                startDestination = Destination.PLAN.route,
                modifier = Modifier.weight(1f),
            ) {
                composable(Destination.PLAN.route) { PlanScreen(viewModel, snackbarHostState) }
                composable(Destination.GEAR.route) { GearScreen(viewModel) }
                composable(Destination.BUDGET.route) { BudgetScreen(viewModel) }
                composable(Destination.CARD.route) { CardScreen() }
            }
        }
    }

    if (showAuthor) {
        AuthorDialog(onDismiss = { showAuthor = false })
    }
}

/** DIALOGAS „Autorius“: vardas, pavardė, grupė. */
@Composable
private fun AuthorDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Filled.Person, contentDescription = null) },
        title = { Text(stringResource(R.string.menu_author)) },
        text = {
            Column {
                Text("$AUTHOR_NAME $AUTHOR_SURNAME", style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.author_group, AUTHOR_GROUP))
                Text(AUTHOR_UNIVERSITY)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) }
        },
    )
}

/** Navigacijos ikona su ŽENKLIUKU (Badge), jei yra nepadarytų dalykų. */
@Composable
private fun DestinationIcon(destination: Destination, selected: Boolean, count: Int) {
    val badgeText = stringResource(R.string.badge_remaining, stringResource(destination.label), count)
    BadgedBox(
        badge = {
            if (count > 0) {
                Badge(Modifier.semantics { contentDescription = badgeText }) {
                    Text(count.toString())
                }
            }
        },
    ) {
        // Pavadinimą perskaito etiketė (label), todėl ikonai aprašymo nereikia.
        Icon(
            if (selected) destination.selectedIcon else destination.icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
        )
    }
}
