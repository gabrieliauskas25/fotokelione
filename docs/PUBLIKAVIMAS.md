# FotoKelionė – publikavimas Google Play

Programėlė publikavimui paruošta: turi ikoną, versiją (`versionCode = 2`, `versionName = "2.0"`), `targetSdk = 35`, dvi kalbas ir prisitaiko prie visų ekranų. Toliau – žingsniai, kuriuos reikia atlikti pačiam (reikia Google paskyros ir mokėjimo kortelės).

## 0. Prieš pradedant

1. `ui/FotoKelioneApp.kt` → `AUTHOR_EMAIL` įrašykite **savo** el. paštą (į jį siunčiama kontakto forma).
2. Savo nuotrauka: ištrinkite `app/src/main/res/drawable/nuotrauka.xml` ir įdėkite `nuotrauka.jpg` į tą patį aplanką.
3. (Nebūtina) `CardViewModel.kt` → `DEFAULT_PORTFOLIO_URLS` pakeiskite nuorodomis į savo nuotraukas internete.

## 1. Google Play Developer paskyra

1. Eikite į https://play.google.com/console/signup ir prisijunkite Google paskyra.
2. Pasirinkite **Yourself** (asmeninė paskyra), užpildykite duomenis, sumokėkite **25 $** (vienkartinis mokestis).
3. Patvirtinkite tapatybę (asmens dokumentas) ir telefono numerį – tai gali užtrukti kelias dienas.

> **Svarbu:** naujoms asmeninėms paskyroms Google reikalauja **uždaro testavimo (Closed testing)** su bent **12 testuotojų 14 dienų**, ir tik tada leidžia publikuoti į „Production“. Atsiskaitymui dažniausiai užtenka parodyti programėlę **Internal testing** arba **Closed testing** kanale – pasitarkite su dėstytoju.

## 2. Pasirašytas AAB failas (Android Studio)

1. **Build → Generate Signed App Bundle or APK…** → pasirinkite **Android App Bundle** → **Next**.
2. **Key store path → Create new…**
   - Failą išsaugokite **ne projekto aplanke** (pvz. `~/raktai/fotokelione.jks`).
   - Sugalvokite slaptažodžius, `Alias: upload`, užpildykite vardą.
   - **Šio failo ir slaptažodžių nepameskite** – be jų negalėsite įkelti atnaujinimų.
3. **Next** → pasirinkite **release** → **Create**.
4. Gautas failas: `app/release/app-release.aab`.

## 3. Programėlė Play Console

1. **Create app**: pavadinimas `FotoKelionė`, kalba – lietuvių, **App**, **Free**, pažymėkite sutikimus.
2. **Dashboard → Set up your app** – užpildykite visus punktus:

| Punktas | Ką atsakyti |
| --- | --- |
| Privacy policy | Nuoroda į privatumo politiką (žr. `docs/privatumo_politika.md` – įkelkite, pvz., į Google Sites ar GitHub Pages) |
| App access | All functionality is available without special access |
| Ads | No, my app does not contain ads |
| Content rating | Užpildykite klausimyną (kategorija – Utility/Productivity, į viską „No“) |
| Target audience | 18+ |
| Data safety | Duomenys nerenkami ir nesiunčiami (viskas saugoma tik telefone); el. laišką vartotojas siunčia per savo pašto programėlę |
| Government apps / Financial features / Health | No |

3. **Store listing** (Grow → Store presence → Main store listing):

| Laukas | Kas įkelti |
| --- | --- |
| Short description (iki 80 simb.) | Fotografo kelionės planas, įrangos sąrašas, biudžetas ir vizitinė kortelė. |
| Full description | Kelionės vietų planas su progresu, įrangos pakavimas ir svorio limitas, biudžeto skaičiuoklė, vizitinė kortelė su CV, portfolio ir kontaktų forma. Lietuvių ir anglų kalbos. |
| App icon 512×512 | `docs/play/ikona_512.png` |
| Feature graphic 1024×500 | `docs/play/reklama_1024x500.png` |
| Phone screenshots (bent 2) | `docs/ekranai/*.png` |
| Tablet screenshots | `docs/ekranai/9_plansete_kortele.png` |

## 4. Įkėlimas

1. **Test and release → Testing → Internal testing** (arba **Closed testing**) → **Create new release**.
2. Įkelkite `app-release.aab`, parašykite „Release notes“ (pvz. „2.0 – vizitinė kortelė, anglų kalba“).
3. **Testers** – įrašykite testuotojų el. paštus (pvz. savo ir dėstytojo).
4. **Review release → Start rollout**. Google peržiūra trunka nuo kelių valandų iki kelių dienų.
5. Po patvirtinimo testuotojai gauna nuorodą į programėlę Google Play.

## 5. Atnaujinimas

Kiekvienai naujai versijai `app/build.gradle.kts` padidinkite `versionCode` (2 → 3 ...) ir pakeiskite `versionName`, tada kartokite 2 ir 4 žingsnius **su tuo pačiu raktu**.
