package lt.vilniustech.fotokelione

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
 */
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FotoKelioneTheme {
                FotoKelioneApp(windowSizeClass = calculateWindowSizeClass(this))
            }
        }
    }
}
