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
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CameraAlt
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import lt.vilniustech.fotokelione.PlannerViewModel
import lt.vilniustech.fotokelione.ui.screens.BudgetScreen
import lt.vilniustech.fotokelione.ui.screens.GearScreen
import lt.vilniustech.fotokelione.ui.screens.PlanScreen

/*
 * ============================================================================
 *  FotoKelioneApp.kt – PROGRAMĖLĖS „RĖMAS“
 * ============================================================================
 *  Čia sujungiama visa programėlė:
 *   - Top App Bar (viršutinė juosta) su meniu „Autorius“ → AuthorDialog
 *   - NAVIGACIJA tarp 3 ekranų (NavHost):
 *       telefone  → apatinė juosta  NavigationBar
 *       planšetėje → šoninė juosta  NavigationRail
 *   - Ženkliukai (Badge) ant navigacijos ikonų: kiek liko nepadaryta
 *   - Snackbar vieta (pranešimai apačioje)
 *
 *  Visi trys ekranai naudoja TĄ PATĮ PlannerViewModel, todėl duomenys
 *  išlieka pereinant iš vieno ekrano į kitą.
 * ============================================================================
 */

// Autoriaus duomenys (rodomi dialoge „Autorius“)
const val AUTHOR_NAME = "Gabrielius"
const val AUTHOR_SURNAME = "Vaišvila"
const val AUTHOR_GROUP = "MKDf-23/2"
const val AUTHOR_UNIVERSITY = "VILNIUS TECH"

/**
 * Trys programėlės ekranai.
 * route – ekrano „adresas“ navigacijai, label – pavadinimas,
 * icon / selectedIcon – ikona (pasirinkta ikona užpildyta).
 */
private enum class Destination(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
) {
    PLAN("plan", "Planas", Icons.Outlined.Map, Icons.Filled.Map),
    GEAR("gear", "Įranga", Icons.Outlined.CameraAlt, Icons.Filled.CameraAlt),
    BUDGET("budget", "Biudžetas", Icons.Outlined.AccountBalanceWallet, Icons.Filled.AccountBalanceWallet),
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
        Destination.BUDGET -> 0
    }

    Scaffold(
        // ---------- TOP APP BAR ----------
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.PhotoCamera,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("FotoKelionė")
                    }
                },
                actions = {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Daugiau parinkčių")
                    }
                    // MENIU su punktu „Autorius“
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text("Autorius") },
                            leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                showAuthor = true
                            },
                        )
                    }
                },
            )
        },
        // ---------- APATINĖ JUOSTA (tik telefone) ----------
        bottomBar = {
            if (!useRail) {
                NavigationBar {
                    Destination.entries.forEach { destination ->
                        val selected = currentRoute == destination.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = { navigate(destination) },
                            icon = { DestinationIcon(destination, selected, badgeCount(destination)) },
                            label = { Text(destination.label) },
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
                            label = { Text(destination.label) },
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
        title = { Text("Autorius") },
        text = {
            Column {
                Text("$AUTHOR_NAME $AUTHOR_SURNAME", style = MaterialTheme.typography.titleMedium)
                Text("Grupė: $AUTHOR_GROUP")
                Text(AUTHOR_UNIVERSITY)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Uždaryti") }
        },
    )
}

/** Navigacijos ikona su ŽENKLIUKU (Badge), jei yra nepadarytų dalykų. */
@Composable
private fun DestinationIcon(destination: Destination, selected: Boolean, count: Int) {
    BadgedBox(
        badge = {
            if (count > 0) {
                Badge(Modifier.semantics { contentDescription = "${destination.label}: liko $count" }) {
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
