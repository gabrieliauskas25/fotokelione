# FotoKelionė

Android programėlė, padedanti fotografams pasiruošti fotografavimo kelionei: vietų planavimas, įrangos pakavimas ir biudžeto skaičiavimas.

Autorius: Gabrielius Vaišvila, grupė MKDf-23/2, VILNIUS TECH.

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

---

# GIS darbas: pradinis darbas su žemėlapiais

Aplanke [`gis/`](gis/) yra ArcGIS Maps SDK for JavaScript (4.33) aplikacija su **lietuviškais duomenimis**:
Lietuvos apskritys (Esri Living Atlas), 57 miestai, 17 lankytinų vietų ir adresų paieška Lietuvoje.

| Puslapis | Turinys |
| --- | --- |
| `gis/index.html` | Turinys ir užduočių atitikmenų lentelė |
| `gis/map2d.html` | 1 (2D), 2, 3, 4 (2D vėliava), 5, 6 (buferis, maršrutas), 7 (4 vizualizacijos), 8 (užklausa) |
| `gis/scene3d.html` | 1 (3D), 2, 3, 4 (3D Gedimino bokštas), 5 (3D paieška), 9 (realistinė aplinka), 10 (kamera) |

Paleidimas:

```bash
cd gis
python3 -m http.server 8000   # atidarykite http://localhost:8000
```

Maršrutas pagal nutylėjimą skaičiuojamas OSRM servisu. Įrašius ArcGIS API raktą į `gis/js/config.js`,
naudojamas Esri Route servisas.
