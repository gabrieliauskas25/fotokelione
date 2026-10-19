# FotoKelionė

Android programėlė, padedanti fotografams pasiruošti fotografavimo kelionei: vietų planavimas, įrangos pakavimas ir biudžeto skaičiavimas.

Autorius: Gabrielius, grupė MKDf-23/3, VILNIUS TECH.

## Ekranai

- **Planas** – fotografavimo vietos (kategorija, paros metas, prioritetas, lauke / patalpoje), filtrai *Visi / Laukia / Atlikta*, progreso juosta, pridėjimas per FAB su pavadinimo tikrinimu, šalinimas su „Atšaukti“ Snackbar'e.
- **Įranga** – daiktų žymėjimas supakuotais, supakuoto svorio skaičiavimas, bagažo limito slankiklis ir įspėjimas jį viršijus.
- **Biudžetas** – naktys, skrydis, viešbutis, dienos išlaidos ir transporto būdas → bendra suma ir vidurkis per dieną, pasirenkamas 10 % rezervas. Priimamas kablelis ir taškas.

Telefone naviguojama apatine juosta, planšetėje – `NavigationRail`; tinkleliai patys pritaiko stulpelių skaičių.

## Struktūra

| Failas | Paskirtis |
| --- | --- |
| `Logic.kt` | Grynoji logika: tikrinimas, skaičių apdorojimas, biudžeto formulė |
| `PlannerViewModel.kt` | Būsena ir veiksmai visiems trims ekranams |
| `ui/FotoKelioneApp.kt` | Top App Bar, meniu „Autorius“, navigacija |
| `ui/screens/` | `PlanScreen`, `GearScreen`, `BudgetScreen` |
| `app/src/test/.../LogicTest.kt` | Unit testai |

## Paleidimas

```bash
./gradlew assembleDebug        # APK: app/build/outputs/apk/debug/app-debug.apk
./gradlew testDebugUnitTest    # unit testai
```

Arba atidarykite projektą Android Studio ir paleiskite `app` konfigūraciją.
