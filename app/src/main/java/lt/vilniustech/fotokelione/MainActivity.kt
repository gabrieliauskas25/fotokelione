package lt.vilniustech.fotokelione // paketas – „aplankas“, kuriam priklauso šis failas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import lt.vilniustech.fotokelione.ui.FotoKelioneApp
import lt.vilniustech.fotokelione.ui.theme.FotoKelioneTheme

/**
 * PROGRAMOS PRADŽIA. Android paleidžia šią klasę, kai atidarote programėlę.
 *  - enableEdgeToEdge(): turinys piešiamas per visą ekraną (po būsenos juosta)
 *  - setContent { ... }: nuo čia prasideda Compose UI
 *  - calculateWindowSizeClass(): nustato, ar tai telefonas, ar planšetė
 *
 * AndroidManifest.xml faile ši klasė nurodyta kaip LAUNCHER –
 * todėl ji atsidaro paspaudus programėlės ikoną.
 */
class MainActivity : ComponentActivity() { // ComponentActivity – Android ekranas, palaikantis Compose

    // calculateWindowSizeClass dar pažymėta kaip „eksperimentinė“, todėl reikia @OptIn
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) { // onCreate – kviečiama, kai ekranas sukuriamas
        super.onCreate(savedInstanceState) // būtinai iškviečiame tėvinės klasės onCreate
        enableEdgeToEdge() // programėlė užima visą ekraną (ir po laikrodžio / baterijos juosta)
        setContent { // viskas šių skliaustų viduje – Compose UI
            FotoKelioneTheme { // mūsų tema (spalvos, šriftai) – žr. ui/theme/Theme.kt
                // Paleidžiame visą programėlę ir perduodame ekrano dydžio klasę
                // (Compact = telefonas, Medium/Expanded = planšetė ar gulsčias ekranas)
                FotoKelioneApp(windowSizeClass = calculateWindowSizeClass(this))
            }
        }
    }
}
