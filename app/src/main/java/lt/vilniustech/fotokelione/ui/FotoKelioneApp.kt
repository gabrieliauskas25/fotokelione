package lt.vilniustech.fotokelione.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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

const val AUTHOR_NAME = "Gabrielius"
const val AUTHOR_SURNAME = "Vaišvila"
const val AUTHOR_GROUP = "MKDf-23/2"
const val AUTHOR_UNIVERSITY = "VILNIUS TECH"

private enum class Destination(val route: String, val label: String, val icon: ImageVector) {
    PLAN("plan", "Planas", Icons.Filled.Map),
    GEAR("gear", "Įranga", Icons.Filled.CameraAlt),
    BUDGET("budget", "Biudžetas", Icons.Filled.AccountBalanceWallet),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FotoKelioneApp(
    windowSizeClass: WindowSizeClass,
    viewModel: PlannerViewModel = viewModel(),
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val snackbarHostState = remember { SnackbarHostState() }
    val useRail = windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact

    var menuExpanded by remember { mutableStateOf(false) }
    var showAuthor by rememberSaveable { mutableStateOf(false) }

    fun navigate(destination: Destination) {
        navController.navigate(destination.route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun badgeCount(destination: Destination): Int = when (destination) {
        Destination.PLAN -> viewModel.pendingCount
        Destination.GEAR -> viewModel.unpackedCount
        Destination.BUDGET -> 0
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FotoKelionė") },
                actions = {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Daugiau parinkčių")
                    }
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
        bottomBar = {
            if (!useRail) {
                NavigationBar {
                    Destination.entries.forEach { destination ->
                        NavigationBarItem(
                            selected = currentRoute == destination.route,
                            onClick = { navigate(destination) },
                            icon = { DestinationIcon(destination.icon, destination.label, badgeCount(destination)) },
                            label = { Text(destination.label) },
                        )
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Row(Modifier.fillMaxSize().padding(padding)) {
            if (useRail) {
                NavigationRail(windowInsets = WindowInsets(0.dp)) {
                    Destination.entries.forEach { destination ->
                        NavigationRailItem(
                            selected = currentRoute == destination.route,
                            onClick = { navigate(destination) },
                            icon = { DestinationIcon(destination.icon, destination.label, badgeCount(destination)) },
                            label = { Text(destination.label) },
                        )
                    }
                }
            }
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
        AlertDialog(
            onDismissRequest = { showAuthor = false },
            icon = { Icon(Icons.Filled.Person, contentDescription = null) },
            title = { Text("Autorius") },
            text = {
                Text("$AUTHOR_NAME $AUTHOR_SURNAME\nGrupė: $AUTHOR_GROUP\n$AUTHOR_UNIVERSITY")
            },
            confirmButton = {
                TextButton(onClick = { showAuthor = false }) { Text("Uždaryti") }
            },
        )
    }
}

@Composable
private fun DestinationIcon(icon: ImageVector, label: String, count: Int) {
    BadgedBox(
        badge = {
            if (count > 0) {
                Badge(Modifier.semantics { contentDescription = "$label: liko $count" }) {
                    Text(count.toString())
                }
            }
        },
    ) {
        // Pavadinimą perskaito NavigationBarItem etiketė, todėl ikona dekoratyvi.
        Icon(icon, contentDescription = null)
    }
}
