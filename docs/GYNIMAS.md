# FotoKelionė – gynimo gidas

Šis dokumentas padeda greitai rasti kiekvieną reikalavimą kode ir paaiškinti, kaip jis veikia.

## Kaip greitai rasti kodą Android Studio (Mac)

| Ką daryti | Klavišai |
| --- | --- |
| Rasti bet ką (failą, funkciją) | **Shift Shift** (du kartus) |
| Ieškoti teksto visame projekte | **Cmd + Shift + F** |
| Failo struktūra (visų funkcijų sąrašas) | **Cmd + F12** |
| Pereiti į funkcijos apibrėžimą | **Cmd + paspaudimas** ant pavadinimo |
| Grįžti atgal | **Cmd + [** |

Patarimas: kiekvieno failo viršuje yra komentaras su to failo turiniu, o prie kiekvieno elemento – didžiosiomis raidėmis parašyta, kas tai yra (pvz. `// SLANKIKLIS`, `// RADIJO MYGTUKAI`). Ieškokite **Cmd + Shift + F** žodžiu, pvz. `SLANKIKLIS`.

## Projekto struktūra

```
app/src/main/java/lt/vilniustech/fotokelione/
├── MainActivity.kt          ← programos pradžia (onCreate)
├── Logic.kt                 ← skaičiavimai ir tikrinimas (be UI)
├── PlannerViewModel.kt      ← duomenys ir veiksmai (būsena)
├── CardViewModel.kt         ← 2 etapas: kortelės duomenys + saugojimas telefone
└── ui/
    ├── FotoKelioneApp.kt    ← Top App Bar, meniu „Autorius“, navigacija
    ├── components/
    │   └── Components.kt    ← bendri dizaino elementai (HeroCard ir kt.)
    ├── screens/
    │   ├── PlanScreen.kt    ← 1 ekranas „Planas“
    │   ├── GearScreen.kt    ← 2 ekranas „Įranga“
    │   ├── BudgetScreen.kt  ← 3 ekranas „Biudžetas“
    │   └── CardScreen.kt    ← 4 ekranas „Kortelė“ (2 etapas)
    └── theme/
        └── Theme.kt         ← spalvos, šriftai, kampai
app/src/main/res/
├── values/strings.xml       ← VISI lietuviški tekstai
├── values-en/strings.xml    ← VISI angliški tekstai
└── drawable/                ← ikona, numatytoji nuotrauka, portfolio „kraunama“ / „klaida“ vaizdai
app/src/test/java/lt/vilniustech/fotokelione/
└── LogicTest.kt             ← unit testai
```

**Kaip keliauja duomenys** (svarbiausia suprasti):

```
Vartotojas paspaudžia  →  Ekranas (PlanScreen) kviečia  →  viewModel.togglePlaceDone(id)
                                                               │
                                       pakeičia mutableStateListOf sąrašą
                                                               │
                         Compose pastebi pakeitimą ir PERPIEŠIA ekraną (recomposition)
```

## Reikalavimai → kur jie yra kode

### Ekranai

| Reikalavimas | Failas | Funkcija |
| --- | --- | --- |
| Vietų sąrašas | `PlanScreen.kt` | `PlanScreen`, `PlaceCard` |
| Filtras Visi / Laukia / Atlikta | `PlanScreen.kt` → `Logic.kt` | `FilterRow` → `filterPlaces` |
| Žymėti atlikta | `PlanScreen.kt` → `PlannerViewModel.kt` | `Checkbox` kortelėje → `togglePlaceDone` |
| Šalinti (+ „Atšaukti“) | `PlanScreen.kt` → `PlannerViewModel.kt` | `onDelete` → `removePlace` / `restorePlace` |
| Progreso juosta | `PlanScreen.kt` → `Logic.kt` | `ProgressHeader` → `placeProgress` |
| Pavadinimo tikrinimas | `PlanScreen.kt` → `Logic.kt` | `AddPlaceDialog` → `validateName` |
| Įrangos žymėjimas „supakuota“ | `GearScreen.kt` | `GearCard` → `toggleGearPacked` |
| Bendras svoris ir limitas | `GearScreen.kt` → `Logic.kt` | `WeightSummary` → `packedWeight`, `isOverLimit` |
| Įspėjimas viršijus limitą | `GearScreen.kt` | `WeightSummary` → `AnimatedVisibility(visible = overLimit)` |
| Biudžeto skaičiuoklė | `BudgetScreen.kt` → `PlannerViewModel.kt` → `Logic.kt` | `BudgetScreen` → `calculate` → `calculateBudget` |
| 10 % rezervas | `Logic.kt` | `calculateBudget` (`RESERVE_RATE = 0.10`) |
| Kablelis ir taškas | `Logic.kt` | `parseAmount` (`replace(',', '.')`) |
| Neigiamos / neskaitinės atmetamos | `Logic.kt` | `parseAmount` (`value < 0` → `null`) |

### Elementai (Material 3)

| Elementas | Kur rasti (Cmd + Shift + F) |
| --- | --- |
| Mygtukas (`Button`) | `BudgetScreen.kt` – „Skaičiuoti“ |
| FAB (`ExtendedFloatingActionButton`) | `PlanScreen.kt` – „Nauja vieta“, `GearScreen.kt` – „Naujas daiktas“ |
| Žymimasis langelis (`Checkbox`) | `PlaceCard`, `GearCard` |
| Jungiklis (`Switch`) | `AddPlaceDialog` (lauke), `BudgetScreen` (rezervas) |
| Radijo mygtukai (`RadioButton`) | `AddPlaceDialog` (paros metas), `TransportOption` |
| Slankiklis (`Slider`) | `AddPlaceDialog` (prioritetas), `WeightSummary` (limitas) |
| Filtro žymos (`FilterChip`) | `FilterRow` |
| Tekstiniai laukai (`OutlinedTextField`) | `AddPlaceDialog`, `AddGearDialog`, `AmountField` |
| Išskleidžiamas sąrašas (`ExposedDropdownMenuBox`) | `AddPlaceDialog` – kategorija |
| Tinklelis (`LazyVerticalGrid`) | `PlanScreen`, `GearScreen` |
| Kortelės (`Card`) | `PlaceCard`, `GearCard`, `HeroCard`, `SectionCard` |
| Progreso juostos (`LinearProgressIndicator`) | `ProgressHeader`, `WeightSummary` |
| Ženkliukas (`Badge`) | `FotoKelioneApp.kt` → `DestinationIcon` |
| Dialogai (`AlertDialog`) | `AddPlaceDialog`, `AddGearDialog`, `AuthorDialog` |
| `Snackbar` | `PlanScreen` → `snackbarHostState.showSnackbar` |
| Meniu (`DropdownMenu`) | `FotoKelioneApp.kt` – trijų taškų meniu |

### Animacijos

| Animacija | Kur |
| --- | --- |
| Kortelės išsiplėtimas `animateContentSize` | `PlaceCard` |
| Rodyklės pasisukimas `animateFloatAsState` | `PlaceCard` |
| Rezultato atsiradimas `AnimatedVisibility` | `BudgetScreen` (rezultatas), `WeightSummary` (įspėjimas) |

### Kiti reikalavimai

| Reikalavimas | Kur |
| --- | --- |
| Navigacija `NavHost` | `FotoKelioneApp.kt` |
| Apatinė juosta (telefonas) `NavigationBar` | `FotoKelioneApp.kt` → `bottomBar` |
| Šoninė juosta (planšetė) `NavigationRail` | `FotoKelioneApp.kt` → `if (useRail)` |
| Ekrano dydžio nustatymas | `MainActivity.kt` → `calculateWindowSizeClass`, `FotoKelioneApp.kt` → `useRail` |
| Tinklelis prisitaiko | `GridCells.Adaptive(minSize = ...)` `PlanScreen` ir `GearScreen` |
| Top App Bar + „Autorius“ | `FotoKelioneApp.kt` → `TopAppBar`, `AuthorDialog` |
| Prieinamumas (`contentDescription`) | ieškokite `contentDescription` – yra prie ikonų ir `Checkbox` |
| Logika atskirta | `Logic.kt` + `PlannerViewModel.kt` |
| Unit testai | `LogicTest.kt` |

## 2 ETAPAS – vizitinė kortelė

Naujas skirtukas **„Kortelė“** (`CardScreen.kt`) + kalbos perjungimas visai programėlei.

| Reikalavimas | Failas | Kur / funkcija |
| --- | --- | --- |
| Vizitinė kortelė: nuotrauka, el. paštas | `CardScreen.kt` | `ProfileHeader` (nuotrauka – `res/drawable/nuotrauka`, el. paštas – `AUTHOR_EMAIL` faile `FotoKelioneApp.kt`) |
| Mygtukai CV / Portfolio | `CardScreen.kt` | `SectionButtons` → `CvSection`, `PortfolioSection` |
| **Data Storage** – pridėti įrašai išlieka paleidus iš naujo | `CardViewModel.kt` | `addCv`, `addPortfolio` → `SharedPreferences` (JSON), `loadCv`, `loadPortfolio` |
| **Kontakto forma + Intent** | `CardScreen.kt` → `Logic.kt` | `ContactSection`: `validateContact`, `Intent.ACTION_SENDTO` su `mailto:` |
| **Lokalizavimas** LT / EN | `res/values/strings.xml`, `res/values-en/strings.xml` | kode visur `stringResource(R.string....)`; `enum` turi `@StringRes label` (`Logic.kt`) |
| **Kalbos perjungiklis** | `FotoKelioneApp.kt` | meniu ⋮ → „Kalba“ → `switchLanguage` → `AppCompatDelegate.setApplicationLocales` |
| **Skirtingi ekranai / pasukimas** | `CardScreen.kt`, `FotoKelioneApp.kt` | `BoxWithConstraints`: ≥ 600dp – dvi kolonos; `NavigationRail` planšetėje / gulsčiai; `verticalScroll` mažiems ekranams |
| *(+1) Kamera* | `CardScreen.kt` → `CardViewModel.kt` | `rememberLauncherForActivityResult(TakePicturePreview())` → `savePhoto` (failas `nuotrauka.jpg`) |
| *(+1) Nuotraukos iš interneto (Coil)* | `CardScreen.kt` | `PortfolioCard`: `AsyncImage` su `placeholder` (kraunama), `error` (nepavyko) ir mygtuku „Bandyti dar kartą“ (`key(attempt)`) |
| **Publikavimas** | `docs/PUBLIKAVIMAS.md` | žingsniai Play Console; ikona `drawable/ic_launcher`, grafika `docs/play/` |

### 2 etapo klausimai

**Kaip išsaugomi duomenys?**
`SharedPreferences` – paprasta „raktas → reikšmė“ saugykla telefone. Sąrašą paverčiame JSON tekstu (`JSONArray`) ir įrašome raktu `"cv"` arba `"portfolio"`. Paleidus programėlę `CardViewModel` juos nuskaito (`loadCv`). Nuotrauka saugoma failu `filesDir/nuotrauka.jpg`.

**Kodėl `AndroidViewModel`, o ne `ViewModel`?**
`SharedPreferences` ir failams reikia `Context` – `AndroidViewModel` jį turi (`application`).

**Kaip veikia laiško siuntimas?**
`Intent(Intent.ACTION_SENDTO)` su `mailto:` – Android atidaro el. pašto programėlę su užpildytu gavėju (`EXTRA_EMAIL`), tema (`EXTRA_SUBJECT`) ir tekstu (`EXTRA_TEXT`). Jei pašto programėlės nėra – `ActivityNotFoundException` → rodome `Toast`.

**Kaip veikia dvi kalbos?**
Visi tekstai yra `strings.xml`. Android pats pasirenka `values-en/` (anglų) arba `values/` (lietuvių, numatytoji). Kode tekstas gaunamas `stringResource(R.string.pavadinimas)`. Perjungiklis kviečia `AppCompatDelegate.setApplicationLocales(...)` – ekranas persikrauna su kita kalba, pasirinkimas išsaugomas (`AppLocalesMetadataHolderService` manifeste).

**Kaip iškviečiama kamera?**
`ActivityResultContracts.TakePicturePreview()` – atidaro telefono kameros programėlę ir grąžina `Bitmap`. Leidimo `CAMERA` nereikia, nes fotografuoja kita programėlė.

**Kaip kraunamos nuotraukos iš interneto?**
Biblioteka **Coil**: `AsyncImage(model = url, placeholder = ..., error = ...)`. Kol siunčiama – rodomas `vaizdas_kraunamas`, jei nepavyko – `vaizdas_klaida` ir mygtukas „Bandyti dar kartą“, kuris padidina `attempt`; `key(attempt)` priverčia `AsyncImage` siųsti iš naujo. Manifeste – leidimas `INTERNET`.

**Kaip programėlė prisitaiko prie ekrano?**
`BoxWithConstraints` duoda `maxWidth`. Jei ≥ 600dp (planšetė, gulsčias telefonas) – kortelė kairėje, turinys dešinėje; kitaip – viskas stulpelyje su slinkimu.

## Unit testai

Paleisti: Android Studio → dešiniu pelės mygtuku ant `LogicTest.kt` → **Run 'LogicTest'**
(arba terminale `./gradlew testDebugUnitTest`). Iš viso 30 testų.

| Ką tikrina | Testai |
| --- | --- |
| Tuščias pavadinimas | `emptyNameIsRejected`, `blankNameIsRejected` |
| Per ilgas pavadinimas (ribos 40 / 41) | `nameAtMaxLengthIsAccepted`, `nameOverMaxLengthIsRejected` |
| Dalyba iš nulio | `divisionByZeroReturnsZero`, `progressOfEmptyListIsZero`, `budgetWithZeroNightsHasZeroPerDay` |
| Kablelis | `commaIsAcceptedAsDecimalSeparator`, `formattingUsesComma` |
| Neigiami ir ne skaičiai | `negativeAmountIsRejected`, `nonNumericAmountIsRejected` |
| Biudžeto formulė | `budgetFormulaWithoutReserve`, `budgetFormulaWithReserveAddsTenPercent` |
| El. paštas, URL, kontakto forma (2 etapas) | `validEmailIsAccepted`, `invalidEmailIsRejected`, `urlMustStartWithHttp`, `contactFormNeedsAllFields` |

## Tikėtini klausimai ir atsakymai

**Kas yra Jetpack Compose?**
Būdas kurti Android UI Kotlin kodu, be XML. Ekranas aprašomas funkcijomis su `@Composable`. Kai pasikeičia duomenys (būsena), Compose pats perpiešia tik tą dalį, kuri pasikeitė.

**Kas yra būsena (state)?**
Reikšmė, kurią stebi Compose: `mutableStateOf(...)`, `mutableStateListOf(...)`. Ją pakeitus, ekranas atsinaujina automatiškai.

**Kuo skiriasi `remember` ir `rememberSaveable`?**
`remember` prisimena reikšmę, kol ekranas rodomas. `rememberSaveable` – išlieka ir pasukus telefoną. Pvz. dialogo laukai naudoja `rememberSaveable`, kad pasukus ekraną tekstas neišsitrintų.

**Kam reikalingas ViewModel?**
Jis laiko duomenis atskirai nuo ekrano ir išgyvena ekrano pasukimą. Visi trys ekranai naudoja tą patį `PlannerViewModel`, todėl duomenys išlieka pereinant tarp ekranų.

**Kodėl logika atskirta į `Logic.kt`?**
Funkcijos ten neturi jokio UI – gauna reikšmes, grąžina rezultatą. Todėl jas galima patikrinti unit testais nepaleidžiant programėlės.

**Kaip veikia navigacija?**
`NavHost` turi tris maršrutus (`"plan"`, `"gear"`, `"budget"`). Paspaudus juostos punktą kviečiama `navController.navigate(route)`.

**Kaip programėlė prisitaiko prie planšetės?**
`MainActivity` apskaičiuoja `WindowSizeClass`. Jei plotis ne `Compact` (planšetė, gulsčias ekranas), rodoma `NavigationRail` šone vietoj apatinės juostos. Tinkleliai naudoja `GridCells.Adaptive`, todėl stulpelių skaičius priklauso nuo pločio.

**Kaip skaičiuojamas biudžetas?**
`skrydis + naktys × (viešbutis + dienos išlaidos + transportas per dieną)`, prie to pridedama 10 %, jei įjungtas rezervas. Vidurkis per dieną = suma / naktys; jei naktų 0 – rodoma 0 (`safeDivide`).

**Kas nutinka įvedus „abc“ arba „-5“?**
`parseAmount` grąžina `null`, `calculate()` pažymi tą lauką klaida (`budgetErrors`), laukas tampa raudonas, rezultatas nerodomas.

**Kas yra `data class` ir `copy`?**
Klasė duomenims laikyti. `place.copy(done = true)` sukuria naują objektą su pakeistu vienu lauku – taip keičiame vietos būseną sąraše.

**Kas yra `enum class`?**
Fiksuotas reikšmių sąrašas, pvz. `Transport` turi tris variantus, kiekvienas su pavadinimu ir kaina per dieną.

**Kaip veikia „Atšaukti“ po šalinimo?**
`removePlace` grąžina pašalintą vietą ir jos poziciją. Jei Snackbar mygtukas paspaustas (`SnackbarResult.ActionPerformed`), kviečiama `restorePlace`, kuri grąžina vietą į tą pačią poziciją.

## Ekrano nuotraukos

| Planas | Nauja vieta | Įranga | Biudžetas |
| --- | --- | --- | --- |
| ![Planas](ekranai/1_planas.png) | ![Nauja vieta](ekranai/2_nauja_vieta.png) | ![Įranga](ekranai/3_iranga.png) | ![Biudžetas](ekranai/4_biudzetas.png) |

| Kortelė (CV) | Kontakto forma | Kalbos meniu | Anglų kalba |
| --- | --- | --- | --- |
| ![CV](ekranai/5_kortele_cv.png) | ![Kontaktas](ekranai/6_kontaktas.png) | ![Meniu](ekranai/7_kalbos_meniu.png) | ![English](ekranai/8_english.png) |

![Planšetė](ekranai/9_plansete_kortele.png)
