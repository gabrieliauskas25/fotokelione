package lt.vilniustech.fotokelione.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import lt.vilniustech.fotokelione.CardViewModel
import lt.vilniustech.fotokelione.DEFAULT_PORTFOLIO_URLS
import lt.vilniustech.fotokelione.PortfolioItem
import lt.vilniustech.fotokelione.R
import lt.vilniustech.fotokelione.isValidUrl
import lt.vilniustech.fotokelione.ui.AUTHOR_EMAIL
import lt.vilniustech.fotokelione.ui.AUTHOR_GROUP
import lt.vilniustech.fotokelione.ui.AUTHOR_NAME
import lt.vilniustech.fotokelione.ui.AUTHOR_SURNAME
import lt.vilniustech.fotokelione.ui.AUTHOR_UNIVERSITY
import lt.vilniustech.fotokelione.ui.components.HeroCard
import lt.vilniustech.fotokelione.ui.components.SectionCard
import lt.vilniustech.fotokelione.validateContact

/*
 * ============================================================================
 *  CardScreen.kt – 4 EKRANAS „KORTELĖ“ (vizitinė kortelė, 2 etapas)
 * ============================================================================
 *  Ekrano dalys:
 *   ProfileHeader  – nuotrauka, vardas, el. paštas, mygtukas „Nufotografuoti save“
 *   SectionButtons – mygtukai CV / Portfolio / Susisiekti
 *   CvSection        – CV įrašai + „Pridėti“ (išsaugoma telefone)
 *   PortfolioSection – nuotraukos iš interneto (Coil) + „Pridėti“
 *   ContactSection   – forma, kuri siunčia laišką per kitą programėlę (Intent)
 *
 *  Telefone viskas viename stulpelyje; plačiame ekrane (planšetė, gulsčias
 *  telefonas) – kortelė kairėje, turinys dešinėje (BoxWithConstraints).
 * ============================================================================
 */

/** Kuri dalis rodoma po mygtukais. */
private enum class CardSection { CV, PORTFOLIO, CONTACT }

@Composable
fun CardScreen(viewModel: CardViewModel = viewModel()) {
    // Kuri dalis atidaryta (null – dar nieko nepaspausta). Išlieka pasukus ekraną.
    var section by rememberSaveable { mutableStateOf<CardSection?>(null) }

    // BoxWithConstraints leidžia sužinoti, kiek vietos turime (maxWidth)
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val wide = maxWidth >= 600.dp // planšetė arba gulsčias telefonas

        if (wide) {
            // PLATUS EKRANAS: dvi kolonos
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .weight(0.4f)
                        .verticalScroll(rememberScrollState()),
                ) {
                    ProfileHeader(viewModel)
                    SectionButtons(section) { section = it }
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .weight(0.6f)
                        .verticalScroll(rememberScrollState()),
                ) {
                    SectionContent(section, viewModel)
                }
            }
        } else {
            // SIAURAS EKRANAS (telefonas): viskas vienas po kito
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            ) {
                ProfileHeader(viewModel)
                SectionButtons(section) { section = it }
                SectionContent(section, viewModel)
            }
        }
    }
}

/** Kortelės viršus: nuotrauka, vardas, kontaktai, kameros mygtukas. */
@Composable
private fun ProfileHeader(viewModel: CardViewModel) {
    val context = LocalContext.current
    val noCameraText = stringResource(R.string.no_camera_app)
    val noEmailText = stringResource(R.string.no_email_app)

    // KAMERA: atidaro telefono kameros programėlę ir grąžina nuotrauką (Bitmap)
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview(),
    ) { bitmap ->
        if (bitmap != null) viewModel.savePhoto(bitmap) // null – vartotojas atšaukė
    }

    HeroCard {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            // NUOTRAUKA: iš kameros (jei yra) arba numatytoji iš res/drawable
            val photoModifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .border(3.dp, Color.White, CircleShape)
            val photo = viewModel.photo
            if (photo != null) {
                Image(
                    bitmap = photo.asImageBitmap(),
                    contentDescription = stringResource(R.string.my_photo),
                    contentScale = ContentScale.Crop,
                    modifier = photoModifier,
                )
            } else {
                Image(
                    painter = painterResource(R.drawable.nuotrauka),
                    contentDescription = stringResource(R.string.my_photo),
                    contentScale = ContentScale.Crop,
                    modifier = photoModifier,
                )
            }
            Spacer(Modifier.height(12.dp))
            Text("$AUTHOR_NAME $AUTHOR_SURNAME", style = MaterialTheme.typography.headlineSmall)
            Text(stringResource(R.string.card_role), style = MaterialTheme.typography.bodyMedium)
            Text(
                "${stringResource(R.string.author_group, AUTHOR_GROUP)} · $AUTHOR_UNIVERSITY",
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(8.dp))

            // EL. PAŠTAS: paspaudus atsidaro el. pašto programėlė
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .clickable {
                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$AUTHOR_EMAIL"))
                        try {
                            context.startActivity(intent)
                        } catch (e: ActivityNotFoundException) {
                            Toast.makeText(context, noEmailText, Toast.LENGTH_SHORT).show()
                        }
                    }
                    .padding(6.dp),
            ) {
                Icon(Icons.Filled.Email, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(AUTHOR_EMAIL, style = MaterialTheme.typography.titleSmall)
            }
            Spacer(Modifier.height(8.dp))

            // MYGTUKAS: nufotografuoti save (atidaro kameros programėlę)
            FilledTonalButton(onClick = {
                try {
                    cameraLauncher.launch(null)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(context, noCameraText, Toast.LENGTH_SHORT).show()
                }
            }) {
                Icon(Icons.Filled.CameraAlt, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.take_photo))
            }
        }
    }
}

/** Trys mygtukai: CV / Portfolio / Susisiekti. Pasirinktas – užpildytas. */
@Composable
private fun SectionButtons(selected: CardSection?, onSelect: (CardSection) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        CardSection.entries.forEach { option ->
            val label = when (option) {
                CardSection.CV -> stringResource(R.string.show_cv)
                CardSection.PORTFOLIO -> stringResource(R.string.show_portfolio)
                CardSection.CONTACT -> stringResource(R.string.show_contact)
            }
            if (option == selected) {
                Button(onClick = { onSelect(option) }, modifier = Modifier.weight(1f)) { Text(label, maxLines = 1) }
            } else {
                OutlinedButton(onClick = { onSelect(option) }, modifier = Modifier.weight(1f)) { Text(label, maxLines = 1) }
            }
        }
    }
}

/** Rodo pasirinktą dalį. */
@Composable
private fun SectionContent(section: CardSection?, viewModel: CardViewModel) {
    when (section) {
        null -> Text(
            stringResource(R.string.choose_section),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
        )
        CardSection.CV -> CvSection(viewModel)
        CardSection.PORTFOLIO -> PortfolioSection(viewModel)
        CardSection.CONTACT -> ContactSection()
    }
}

// ---------------------------------------------------------------------------
// CV
// ---------------------------------------------------------------------------

@Composable
private fun CvSection(viewModel: CardViewModel) {
    var showDialog by rememberSaveable { mutableStateOf(false) }
    // Numatytieji įrašai iš strings.xml + vartotojo pridėti (iš telefono atminties)
    val entries = stringArrayResource(R.array.cv_items).toList() + viewModel.userCv

    SectionCard(title = stringResource(R.string.cv_title), icon = Icons.Filled.Description) {
        entries.forEach { entry ->
            Row(Modifier.padding(vertical = 4.dp)) {
                Text("•  ", color = MaterialTheme.colorScheme.primary)
                Text(entry, style = MaterialTheme.typography.bodyLarge)
            }
        }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = { showDialog = true }, modifier = Modifier.align(Alignment.End)) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(Modifier.width(4.dp))
            Text(stringResource(R.string.new_cv_entry))
        }
    }

    if (showDialog) {
        var text by rememberSaveable { mutableStateOf("") }
        var error by rememberSaveable { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(R.string.new_cv_entry)) },
            text = {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text(stringResource(R.string.cv_entry)) },
                    placeholder = { Text(stringResource(R.string.cv_entry_hint)) },
                    isError = error,
                    supportingText = if (error) {
                        { Text(stringResource(R.string.error_empty)) }
                    } else {
                        null
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    // addCv išsaugo įrašą telefone; false – tekstas tuščias
                    if (viewModel.addCv(text)) showDialog = false else error = true
                }) { Text(stringResource(R.string.add)) }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text(stringResource(R.string.cancel)) }
            },
        )
    }
}

// ---------------------------------------------------------------------------
// PORTFOLIO
// ---------------------------------------------------------------------------

@Composable
private fun PortfolioSection(viewModel: CardViewModel) {
    var showDialog by rememberSaveable { mutableStateOf(false) }
    // Numatytieji darbai: pavadinimai iš strings.xml, adresai iš CardViewModel.kt
    val defaults = stringArrayResource(R.array.portfolio_titles)
        .zip(DEFAULT_PORTFOLIO_URLS) { title, url -> PortfolioItem(title, url) }
    val items = defaults + viewModel.userPortfolio

    SectionCard(title = stringResource(R.string.portfolio_title), icon = Icons.Filled.PhotoLibrary) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items.forEach { item -> PortfolioCard(item) }
        }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = { showDialog = true }, modifier = Modifier.align(Alignment.End)) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(Modifier.width(4.dp))
            Text(stringResource(R.string.new_portfolio_item))
        }
    }

    if (showDialog) {
        var title by rememberSaveable { mutableStateOf("") }
        var url by rememberSaveable { mutableStateOf("https://") }
        var checked by rememberSaveable { mutableStateOf(false) } // klaidas rodome tik paspaudus „Pridėti“
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(R.string.new_portfolio_item)) },
            text = {
                Column {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text(stringResource(R.string.name)) },
                        singleLine = true,
                        isError = checked && title.isBlank(),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = url,
                        onValueChange = { url = it },
                        label = { Text(stringResource(R.string.image_url)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                        isError = checked && !isValidUrl(url),
                        supportingText = if (checked && !isValidUrl(url)) {
                            { Text(stringResource(R.string.error_url)) }
                        } else {
                            null
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    checked = true
                    if (viewModel.addPortfolio(title, url)) showDialog = false
                }) { Text(stringResource(R.string.add)) }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text(stringResource(R.string.cancel)) }
            },
        )
    }
}

/**
 * Vienas portfolio darbas: nuotrauka IŠ INTERNETO (Coil biblioteka) ir pavadinimas.
 *  placeholder – rodomas, kol nuotrauka siunčiama (vaizdas_kraunamas.xml)
 *  error       – rodomas, jei nepavyko (vaizdas_klaida.xml), + mygtukas „Bandyti dar kartą“
 */
@Composable
private fun PortfolioCard(item: PortfolioItem) {
    var attempt by remember { mutableIntStateOf(0) }  // keičiant šį skaičių nuotrauka kraunama iš naujo
    var failed by remember { mutableStateOf(false) }  // ar paskutinis bandymas nepavyko

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Box {
            // key(attempt) – pasikeitus attempt, AsyncImage sukuriamas iš naujo ir siunčia vėl
            key(attempt) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.title,
                    placeholder = painterResource(R.drawable.vaizdas_kraunamas),
                    error = painterResource(R.drawable.vaizdas_klaida),
                    onError = { failed = true },    // nepavyko → parodysime „Bandyti dar kartą“
                    onSuccess = { failed = false }, // pavyko → mygtuko nereikia
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(3f / 2f),
                )
            }
            // Jei nepavyko – mygtukas „Bandyti dar kartą“ ant nuotraukos
            if (failed) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(12.dp),
                ) {
                    Text(
                        stringResource(R.string.image_failed),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Button(onClick = {
                        failed = false
                        attempt++
                    }) {
                        Icon(Icons.Filled.Refresh, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.retry))
                    }
                }
            }
        }
        Text(item.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(12.dp))
    }
}

// ---------------------------------------------------------------------------
// KONTAKTO FORMA
// ---------------------------------------------------------------------------

/**
 * Forma „Susisiekite su manimi“.
 * Paspaudus „Siųsti“ atidaroma el. pašto programėlė (Intent.ACTION_SENDTO)
 * su jau užpildytu gavėju, tema ir tekstu – vartotojui lieka tik paspausti „Siųsti“.
 */
@Composable
private fun ContactSection() {
    val context = LocalContext.current
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var question by rememberSaveable { mutableStateOf("") }
    var checked by rememberSaveable { mutableStateOf(false) } // klaidas rodome tik paspaudus „Siųsti“

    val errors = validateContact(name, email, question) // Logic.kt
    val subject = stringResource(R.string.email_subject)
    val body = stringResource(R.string.email_body, name.trim(), email.trim(), question.trim())
    val noEmailText = stringResource(R.string.no_email_app)

    SectionCard(title = stringResource(R.string.contact_title), icon = Icons.Filled.Email) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(R.string.your_name)) },
            singleLine = true,
            isError = checked && errors.name,
            supportingText = if (checked && errors.name) {
                { Text(stringResource(R.string.error_empty)) }
            } else {
                null
            },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(R.string.your_email)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            isError = checked && errors.email,
            supportingText = if (checked && errors.email) {
                { Text(stringResource(R.string.error_email)) }
            } else {
                null
            },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = question,
            onValueChange = { question = it },
            label = { Text(stringResource(R.string.your_question)) },
            minLines = 3,
            isError = checked && errors.question,
            supportingText = if (checked && errors.question) {
                { Text(stringResource(R.string.error_empty)) }
            } else {
                null
            },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                checked = true
                if (errors.isValid) {
                    // INTENT: „mailto:“ – atsidarys tik el. pašto programėlės
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:")
                        putExtra(Intent.EXTRA_EMAIL, arrayOf(AUTHOR_EMAIL)) // gavėjas – aš
                        putExtra(Intent.EXTRA_SUBJECT, subject)
                        putExtra(Intent.EXTRA_TEXT, body)
                    }
                    try {
                        context.startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(context, noEmailText, Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.send))
        }
    }
}
