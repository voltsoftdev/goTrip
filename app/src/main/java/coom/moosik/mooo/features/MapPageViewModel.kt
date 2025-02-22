package coom.moosik.mooo.features

import android.app.Application
import android.content.res.AssetManager
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.opencsv.CSVReader
import com.opencsv.exceptions.CsvException
import coom.moosik.mooo.MooSikApp
import coom.moosik.mooo.extensions.isNumeric
import coom.moosik.mooo.model.Marker
import coom.moosik.mooo.model.SubCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.io.IOException
import java.io.InputStreamReader

class MapPageViewModel(application: Application) : AndroidViewModel(application) {

    private var _currentPosition : MutableStateFlow<Pair<Long, LatLng>> = MutableStateFlow(
        Pair(System.currentTimeMillis(), LatLng(37.574187, 126.976882)))

    val currentPosition : StateFlow<Pair<Long, LatLng>>
        get() = this._currentPosition

    private var allMarkers : MutableStateFlow<List<Marker>> = MutableStateFlow(emptyList())

    private var _markers : MutableStateFlow<List<Marker>> = MutableStateFlow(emptyList())

    val markers : StateFlow<List<Marker>>
        get() = this._markers

    var categories : MutableStateFlow<List<SubCategory>> = MutableStateFlow(emptyList())

    var selectedCategory : MutableStateFlow<Set<String>> = MutableStateFlow(setOf())

    init {
        allMarkers.tryEmit(loadData())

        val list = arrayListOf<SubCategory>()
        list.add(SubCategory(id = "bmg", name = "박물관"))
        list.add(SubCategory(id = "bncj", name = "보는축제"))
        list.add(SubCategory(id = "chcj", name = "체험축제"))
        list.add(SubCategory(id = "cmd", name = "천문대"))
        list.add(SubCategory(id = "cyj", name = "영화,드라마 촬영지"))
        list.add(SubCategory(id = "dbg", name = "둘러볼곳"))
        list.add(SubCategory(id = "dmu", name = "동물원"))
        list.add(SubCategory(id = "dmu", name = "동물원"))
        list.add(SubCategory(id = "dr", name = "dr"))
        list.add(SubCategory(id = "ds", name = "위인동상"))
        list.add(SubCategory(id = "dsg", name = "도서관"))
        list.add(SubCategory(id = "gb", name = "gb"))
        list.add(SubCategory(id = "gnm", name = "고인돌"))
        list.add(SubCategory(id = "gu", name = "gu"))
        list.add(SubCategory(id = "hs", name = "hs"))
        list.add(SubCategory(id = "hsyj", name = "해수욕장"))
        list.add(SubCategory(id = "jg", name = "jg"))
        list.add(SubCategory(id = "mh", name = "mh"))
        list.add(SubCategory(id = "mjgm", name = "mjgm"))
        list.add(SubCategory(id = "mncj", name = "mncj"))
        list.add(SubCategory(id = "ms", name = "ms"))
        list.add(SubCategory(id = "msg", name = "msg"))
        list.add(SubCategory(id = "msj", name = "명승지, 관광지"))
        list.add(SubCategory(id = "nids", name = "nids"))
        list.add(SubCategory(id = "or", name = "올레길,둘레길"))
        list.add(SubCategory(id = "pp", name = "폭포"))
        list.add(SubCategory(id = "sbus", name = "sbus"))
        list.add(SubCategory(id = "sd", name = "미쉐린,식객,맛집"))
        list.add(SubCategory(id = "sd5", name = "미쉐린,식객,맛집 (2)"))
        list.add(SubCategory(id = "sgjt", name = "5일장터"))
        list.add(SubCategory(id = "ski", name = "스키장,눈썰매장"))
        list.add(SubCategory(id = "smu", name = "국보,보물,유적지"))
        list.add(SubCategory(id = "ujj", name = "유적지"))
        list.add(SubCategory(id = "unc", name = "unc"))
        list.add(SubCategory(id = "uuj", name = "유원지,동물원"))
        list.add(SubCategory(id = "yh", name = "yh"))
        categories.value = list

        viewModelScope.launch {
            categories.collectLatest {
                val selected : MutableSet<String> = mutableSetOf()
                it.forEach { category ->
                    if (category.isChecked) {
                        selected.add(category.id)
                    }
                }
                selectedCategory.tryEmit(selected)
            }
        }

        viewModelScope.launch {
            selectedCategory.collectLatest { selected ->
                val markers : ArrayList<Marker> = arrayListOf()

                allMarkers.value.forEach { marker ->
                    if (selected.contains(marker.type)) {
                        markers.add(marker)
                    }
                }

                _markers.tryEmit(markers)
            }
        }
    }

    fun updatePosition(latitude : Double, longitude: Double) {
        _currentPosition.tryEmit(Pair(System.currentTimeMillis(), LatLng(latitude, longitude)))
    }

    fun toggleCategory(id: String) {
        val updatedCategories = categories.value.map { category ->
            if (category.id == id) {
                category.copy(isChecked = !category.isChecked)
            } else {
                category
            }
        }
        categories.value = updatedCategories
    }

    @Throws(IOException::class, CsvException::class)
    private fun loadData() : ArrayList<Marker> {
        val assetManager: AssetManager = getApplication<MooSikApp>().assets
        val inputStream = assetManager.open("adata0.csv")
        val csvReader = CSVReader(InputStreamReader(inputStream, "utf-8"))

        val markers = arrayListOf<Marker>()
        val allContent = csvReader.readAll() as List<Array<String>>
        for (content in allContent) {
            if (content[0].isNumeric()) {
                markers.add(
                    Marker(
                    latitude = content[0].toDouble(),
                    longitude = content[1].toDouble(),
                    type = content[4],
                    irm1 = content[7])
                )
            }
        }
        return markers
    }
}