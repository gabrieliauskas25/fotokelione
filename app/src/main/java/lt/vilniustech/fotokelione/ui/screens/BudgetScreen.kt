package lt.vilniustech.fotokelione.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import lt.vilniustech.fotokelione.BudgetField
import lt.vilniustech.fotokelione.BudgetResult
import lt.vilniustech.fotokelione.PlannerViewModel
import lt.vilniustech.fotokelione.Transport
import lt.vilniustech.fotokelione.formatEuro

@Composable
fun BudgetScreen(viewModel: PlannerViewModel) {
    val form = viewModel.budgetForm
    val errors = viewModel.budgetErrors

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.widthIn(max = 600.dp).fillMaxWidth(),
        ) {
            AmountField(
                value = form.nights,
                onValueChange = { viewModel.updateBudget(form.copy(nights = it)) },
                label = "Naktų skaičius",
                isError = BudgetField.NIGHTS in errors,
                errorText = "Įveskite sveiką neneigiamą skaičių",
                keyboardType = KeyboardType.Number,
            )
            AmountField(
                value = form.flight,
                onValueChange = { viewModel.updateBudget(form.copy(flight = it)) },
                label = "Skrydžio kaina, €",
                isError = BudgetField.FLIGHT in errors,
            )
            AmountField(
                value = form.hotelPerNight,
                onValueChange = { viewModel.updateBudget(form.copy(hotelPerNight = it)) },
                label = "Viešbutis už naktį, €",
                isError = BudgetField.HOTEL in errors,
            )
            AmountField(
                value = form.dailyExpenses,
                onValueChange = { viewModel.updateBudget(form.copy(dailyExpenses = it)) },
                label = "Dienos išlaidos, €",
                isError = BudgetField.DAILY in errors,
            )

            Spacer(Modifier.height(8.dp))
            Text("Transporto būdas", style = MaterialTheme.typography.labelLarge)
            Column(Modifier.selectableGroup()) {
                Transport.entries.forEach { option ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = form.transport == option,
                                onClick = { viewModel.updateBudget(form.copy(transport = option)) },
                                role = Role.RadioButton,
                            )
                            .padding(vertical = 4.dp),
                    ) {
                        RadioButton(selected = form.transport == option, onClick = null)
                        Spacer(Modifier.width(8.dp))
                        Text(option.label, Modifier.weight(1f))
                        Text(
                            "${formatEuro(option.costPerDay)} / d.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Pridėti 10 % rezervą", Modifier.weight(1f))
                Switch(
                    checked = form.includeReserve,
                    onCheckedChange = { viewModel.updateBudget(form.copy(includeReserve = it)) },
                    modifier = Modifier.semantics { contentDescription = "Pridėti 10 % rezervą" },
                )
            }

            Spacer(Modifier.height(8.dp))
            Button(onClick = viewModel::calculate, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Calculate, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Skaičiuoti")
            }

            Spacer(Modifier.height(8.dp))
            val result = viewModel.budgetResult
            // Paskutinis rezultatas išlaikomas, kad jis nedingtų išnykimo animacijos metu.
            var shownResult by remember { mutableStateOf(result) }
            if (result != null) shownResult = result
            AnimatedVisibility(
                visible = result != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                shownResult?.let { ResultCard(it) }
            }
        }
    }
}

@Composable
private fun AmountField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isError: Boolean,
    errorText: String = "Įveskite neneigiamą skaičių (pvz., 12,50)",
    keyboardType: KeyboardType = KeyboardType.Decimal,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        isError = isError,
        supportingText = if (isError) {
            { Text(errorText) }
        } else {
            null
        },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun ResultCard(result: BudgetResult) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            ResultRow("Tarpinė suma", formatEuro(result.subtotal))
            if (result.reserve > 0) ResultRow("Rezervas (10 %)", formatEuro(result.reserve))
            HorizontalDivider(Modifier.padding(vertical = 4.dp))
            ResultRow("Iš viso", formatEuro(result.total), emphasized = true)
            ResultRow("Vidutiniškai per dieną", formatEuro(result.perDay))
        }
    }
}

@Composable
private fun ResultRow(label: String, value: String, emphasized: Boolean = false) {
    val style = if (emphasized) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyLarge
    Row(Modifier.fillMaxWidth()) {
        Text(label, style = style, modifier = Modifier.weight(1f))
        Text(value, style = style, fontWeight = if (emphasized) FontWeight.Bold else null)
    }
}
