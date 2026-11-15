package lt.vilniustech.fotokelione

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/*
 * ============================================================================
 *  CardViewModel.kt – VIZITINĖS KORTELĖS DUOMENYS (2 etapas)
 * ============================================================================
 *  Laiko vartotojo pridėtus CV įrašus, portfolio darbus ir nuotrauką.
 *
 *  DUOMENŲ SAUGOJIMAS (Data Storage):
 *   - CV ir portfolio įrašai saugomi SharedPreferences (telefono atmintyje,
 *     failas „vizitine_kortele“). Sąrašas paverčiamas JSON tekstu.
 *   - Nuotrauka saugoma failu filesDir/nuotrauka.jpg.
 *  Todėl viskas išlieka ir uždarius / iš naujo paleidus programėlę.
 *
 *  AndroidViewModel (o ne ViewModel) – nes reikia `application` (Context),
 *  kad pasiektume SharedPreferences ir failus.
 * ============================================================================
 */

/** Vienas portfolio darbas: pavadinimas ir nuotraukos adresas internete. */
data class PortfolioItem(val title: String, val imageUrl: String)

/**
 * Numatytųjų portfolio nuotraukų adresai internete (viešai prieinami).
 * Pavadinimai – strings.xml → portfolio_titles (ta pačia tvarka).
 * Čia galite įrašyti nuorodas į SAVO nuotraukas.
 */
val DEFAULT_PORTFOLIO_URLS = listOf(
    "https://picsum.photos/id/1015/600/400",
    "https://picsum.photos/id/1018/600/400",
    "https://picsum.photos/id/1044/600/400",
)

class CardViewModel(application: Application) : AndroidViewModel(application) {

    // SharedPreferences – paprasta „raktas → reikšmė“ saugykla telefone
    private val prefs = application.getSharedPreferences("vizitine_kortele", Context.MODE_PRIVATE)

    // Failas, kuriame saugoma kamera padaryta nuotrauka
    private val photoFile = File(application.filesDir, "nuotrauka.jpg")

    /** Vartotojo pridėti CV įrašai (numatytieji – strings.xml). */
    val userCv = mutableStateListOf<String>().apply { addAll(loadCv()) }

    /** Vartotojo pridėti portfolio darbai. */
    val userPortfolio = mutableStateListOf<PortfolioItem>().apply { addAll(loadPortfolio()) }

    /** Kamera padaryta nuotrauka (null – rodoma numatytoji iš res/drawable). */
    var photo by mutableStateOf(loadPhoto())
        private set

    // ---------------------------------------------------------------------
    // CV
    // ---------------------------------------------------------------------

    /** Prideda CV įrašą ir iškart išsaugo. Grąžina false, jei tekstas tuščias. */
    fun addCv(text: String): Boolean {
        if (text.isBlank()) return false
        userCv += text.trim()
        prefs.edit().putString("cv", JSONArray(userCv.toList()).toString()).apply()
        return true
    }

    private fun loadCv(): List<String> {
        val json = prefs.getString("cv", null) ?: return emptyList() // dar nieko neišsaugota
        val array = JSONArray(json)
        return List(array.length()) { i -> array.getString(i) }
    }

    // ---------------------------------------------------------------------
    // PORTFOLIO
    // ---------------------------------------------------------------------

    /** Prideda portfolio darbą ir iškart išsaugo. Grąžina false, jei duomenys netinkami. */
    fun addPortfolio(title: String, url: String): Boolean {
        if (title.isBlank() || !isValidUrl(url)) return false
        userPortfolio += PortfolioItem(title.trim(), url.trim())
        // Kiekvieną darbą paverčiame JSON objektu {"title": ..., "url": ...}
        val array = JSONArray()
        userPortfolio.forEach { array.put(JSONObject().put("title", it.title).put("url", it.imageUrl)) }
        prefs.edit().putString("portfolio", array.toString()).apply()
        return true
    }

    private fun loadPortfolio(): List<PortfolioItem> {
        val json = prefs.getString("portfolio", null) ?: return emptyList()
        val array = JSONArray(json)
        return List(array.length()) { i ->
            val obj = array.getJSONObject(i)
            PortfolioItem(obj.getString("title"), obj.getString("url"))
        }
    }

    // ---------------------------------------------------------------------
    // NUOTRAUKA (iš kameros)
    // ---------------------------------------------------------------------

    /** Išsaugo kameros nuotrauką į failą ir parodo ją kortelėje. */
    fun savePhoto(bitmap: Bitmap) {
        photoFile.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }
        photo = bitmap
    }

    private fun loadPhoto(): Bitmap? =
        if (photoFile.exists()) BitmapFactory.decodeFile(photoFile.path) else null
}
