package coom.moosik.mooo.features

import android.app.Application
import android.content.res.AssetManager
import android.graphics.Point
import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.VisibleRegion
import com.opencsv.CSVReader
import com.opencsv.exceptions.CsvException
import coom.moosik.mooo.MooSikApp
import coom.moosik.mooo.extensions.getPreferenceString
import coom.moosik.mooo.extensions.isNumeric
import coom.moosik.mooo.extensions.putPreference
import coom.moosik.mooo.model.Category
import coom.moosik.mooo.model.Marker
import coom.moosik.mooo.model.SubCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStreamReader

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class MapPageViewModel(application: Application) : AndroidViewModel(application) {

    private var _currentPosition : MutableStateFlow<Pair<Long, LatLng>> = MutableStateFlow(
        Pair(System.currentTimeMillis(), LatLng(37.574187, 126.976882)))

    val currentPosition : StateFlow<Pair<Long, LatLng>>
        get() = this._currentPosition

    private var allMarkers : MutableStateFlow<List<Marker>> = MutableStateFlow(emptyList())

    private var _markers : MutableStateFlow<List<Marker>> = MutableStateFlow(emptyList())

    val markers : StateFlow<List<Marker>>
        get() = this._markers

    var categories : MutableStateFlow<List<Category>> = MutableStateFlow(emptyList())

    private var selectedCategory : MutableStateFlow<Set<String>> = MutableStateFlow(setOf())

    var selectedMarker : MutableStateFlow<Pair<Marker, Point>?> = MutableStateFlow(null)

    val favoriteMarkers : MutableStateFlow<MutableSet<Marker>> = MutableStateFlow(mutableSetOf())

    val highlightsMarkers : MutableStateFlow<List<Marker>> = MutableStateFlow(emptyList())

    val selectedLanguage : MutableStateFlow<String> = MutableStateFlow("한국어")

    val languageSelectMode : MutableStateFlow<Boolean> = MutableStateFlow(false)

    val visibleRegion: MutableStateFlow<VisibleRegion?> = MutableStateFlow(null)

    private val _searchText = MutableStateFlow("")

    val searchText: StateFlow<String> = _searchText

    private var _searchedMarkers : MutableStateFlow<List<Marker>?> = MutableStateFlow(emptyList())

    val searchedMarkers : StateFlow<List<Marker>?>
        get() = this._searchedMarkers

    private var _finalSearchedMarker : MutableStateFlow<Marker?> = MutableStateFlow(null)

    val finalSearchedMarker : StateFlow<Marker?>
        get() = this._finalSearchedMarker

    init {
        // 사용자가 입력한 텍스트와 마커 정보를 비교해서 검색결과 화면에 노출 합니다
        viewModelScope.launch {
            combine(searchText, allMarkers) { text, all ->
                if (text.isBlank()) {
                    emptyList()
                } else {
                    all.filter { it.irm1.contains(text, ignoreCase = true) }
                }
            }.collectLatest {
                _searchedMarkers.tryEmit(it)
            }
        }
        // 단말기에서 마커를 불러오고 , '가자구요' 서버에서도 마커 정보를 불러 옵니다
        viewModelScope.launch {
            combine(readMarkersFromDevice(), readMarkersFromServer()) { markers1, markers2 ->
                Pair(markers1, markers2) // 두 정보를 합칩니다.
            }.collectLatest {
                val arrayList : ArrayList<Marker> = arrayListOf()
                arrayList.addAll(it.first)
                it.second?.let { list ->
                    arrayList.addAll(list)
                }
                allMarkers.tryEmit(arrayList)
            }
        }

        val favoriteMarkersString = getApplication<MooSikApp>().getPreferenceString("favoriteMarkers")
        favoriteMarkers.tryEmit(favoriteMarkersString.jsonArrayToMarkers())

        val list = arrayListOf<Category>()

        val subCategories1 : MutableSet<SubCategory> = mutableSetOf()
        subCategories1.add(SubCategory(id = "mncj", img = "mncj", name = "먹거리축제", isFixed = true))
        subCategories1.add(SubCategory(id = "chcj", img = "chcj", name = "문화축제"))
        subCategories1.add(SubCategory(id = "bncj", img = "bncj", name = "보는축제"))
        subCategories1.add(SubCategory(id = "sgjt", img = "sgjt", name = "5일장"))
        subCategories1.add(SubCategory(id = "hs", img = "hs", name = "행사"))
        list.add(Category(id = "축제", subCategories = subCategories1.toMutableList()))

        val subCategories2 : MutableSet<SubCategory> = mutableSetOf()
        subCategories2.add(SubCategory(id = "mncj", img = "mncj", name = "먹자골목", isFixed = true))
        subCategories2.add(SubCategory(id = "sd1", img = "sd", name = "미쉐린"))
        subCategories2.add(SubCategory(id = "sd2", img = "sd", name = "식객"))
        subCategories2.add(SubCategory(id = "sd3", img = "sd", name = "맛집"))
        subCategories2.add(SubCategory(id = "sd5", img = "sd5", name = "식당"))
        list.add(Category(id = "먹거리", subCategories = subCategories2.toMutableList()))

        val subCategories3 : MutableSet<SubCategory> = mutableSetOf()
        subCategories3.add(SubCategory(id = "msj1", img = "msj", name = "명승지"))
        subCategories3.add(SubCategory(id = "msj2", img = "msj", name = "명승지"))
        subCategories3.add(SubCategory(id = "gu", img = "gu", name = "국립공원"))
        subCategories3.add(SubCategory(id = "cygnm", img = "cygnm", name = "천연기념물"))
        subCategories3.add(SubCategory(id = "pp", img = "pp", name = "폭포"))
        subCategories3.add(SubCategory(id = "bhs", img = "bhs", name = "보호수"))
        list.add(Category(id = "자연", subCategories = subCategories3.toMutableList()))

        val subCategories4 : MutableSet<SubCategory> = mutableSetOf()
        subCategories4.add(SubCategory(id = "dmu", img = "dmu", name = "동물원"))
        subCategories4.add(SubCategory(id = "smu", img = "smu", name = "식물원"))
        subCategories4.add(SubCategory(id = "uuj", img = "uuj", name = "유원지"))
        subCategories4.add(SubCategory(id = "msg", img = "msg", name = "미술관"))
        subCategories4.add(SubCategory(id = "bmg1", img = "bmg", name = "국립박물관")) // 국립박물관에 맞는 이미지 이름을 img 에 넣어주면 됩니다
        subCategories4.add(SubCategory(id = "bmg2", img = "bmg", name = "사립박물관")) // 사립박물관에 맞는 이미지 이름을 img 에 넣어주면 됩니다
        subCategories4.add(SubCategory(id = "cmd", img = "cmd", name = "천문대"))
        subCategories4.add(SubCategory(id = "unc", img = "unc", name = "유네스코지정"))
        subCategories4.add(SubCategory(id = "dsg", img = "dsg", name = "도서관"))
        subCategories4.add(SubCategory(id = "jg", img = "jg", name = "종교시설"))
        list.add(Category(id = "문화공간", subCategories = subCategories4.toMutableList()))

        val subCategories5 : MutableSet<SubCategory> = mutableSetOf()
        subCategories5.add(SubCategory(id = "nids", img = "nids", name = "놀이동산"))
        subCategories5.add(SubCategory(id = "or", img = "or", name = "올레길"))
        subCategories5.add(SubCategory(id = "uuj", img = "uuj", name = "유원지"))
        subCategories5.add(SubCategory(id = "hsyj", img = "hsyj", name = "해수욕장"))
        subCategories5.add(SubCategory(id = "ski", img = "ski", name = "스키장"))
        subCategories5.add(SubCategory(id = "cyj", img = "cyj", name = "드라마 촬영지"))
        subCategories5.add(SubCategory(id = "dbg", img = "dbg", name = "둘러볼곳"))
        list.add(Category(id = "여가, 레저", subCategories = subCategories5.toMutableList()))

        val subCategories6 : MutableSet<SubCategory> = mutableSetOf()
        subCategories6.add(SubCategory(id = "gb1", img = "gb", name = "국보"))
        subCategories6.add(SubCategory(id = "gb2", img = "gb", name = "보물"))
        subCategories6.add(SubCategory(id = "sjj", img = "sjj", name = "사적지"))
        subCategories6.add(SubCategory(id = "dr", img = "dr", name = "국가등록문화유산(dr)"))
        subCategories6.add(SubCategory(id = "dr1", img = "dr", name = "국가등록문화유산"))
        subCategories6.add(SubCategory(id = "dr2", img = "dr", name = "시도등록문화유산"))
        subCategories6.add(SubCategory(id = "mh1", img = "mh", name = "국가민속문화유산"))
        subCategories6.add(SubCategory(id = "gnm", img = "gnm", name = "시도기념물"))
        subCategories6.add(SubCategory(id = "ds", img = "ds", name = "위인동상"))
        list.add(Category(id = "문화재", subCategories = subCategories6.toMutableList()))

        categories.value = list

        viewModelScope.launch {
            categories.collectLatest {
                val selected : MutableSet<String> = mutableSetOf()
                it.forEach { category ->
                    category.subCategories.forEach { subCategory ->
                        if (subCategory.isChecked) {
                            selected.add(subCategory.img)
                        }
                    }
                }
                selectedCategory.tryEmit(selected)
            }
        }

        viewModelScope.launch {
            combine(selectedCategory, allMarkers, visibleRegion) { selectedCategory, allMarkers, visibleRegion ->
                Triple(selectedCategory, allMarkers, visibleRegion)
            }.collectLatest { triple ->

                val selectedCategory = triple.first
                val allMarkers = triple.second
                val visibleRegion = triple.third
                val currentBounds = visibleRegion?.latLngBounds

                val markers : ArrayList<Marker> = arrayListOf()
                allMarkers.forEach { marker ->
                    val markerLatLng = LatLng(marker.latitude, marker.longitude)
                    if (currentBounds?.contains(markerLatLng) == true && selectedCategory.contains(marker.type)) {
                        markers.add(marker)
                    }
                }
                _markers.tryEmit(markers)
            }
        }

        viewModelScope.launch {
            combine(selectedCategory, finalSearchedMarker, favoriteMarkers) { selectedCategory, finalSearchedMarker, favoriteMarkers ->
                val markers : ArrayList<Marker> = arrayListOf()
                favoriteMarkers.forEach { marker ->
                    if (selectedCategory.contains(marker.type)) {
                        markers.add(marker)
                    }
                    else if (finalSearchedMarker?.irm1 == marker.irm1) {
                        markers.add(marker)
                    }
                }
                markers
            }.collectLatest {
                highlightsMarkers.tryEmit(it)
            }
        }

    }

    fun selectMarker(marker: Marker?, point: Point?) {
        selectedMarker.tryEmit(marker?.let { Pair(marker, point!!) } ?: run { null })
    }

    fun updatePosition(latitude : Double, longitude: Double) {
        _currentPosition.tryEmit(Pair(System.currentTimeMillis(), LatLng(latitude, longitude)))
    }

    fun toggleHeart(marker: Marker?) {
        marker?.let {
            val currentFavorites = favoriteMarkers.value.toMutableSet()
            if (currentFavorites.contains(it)) {
                currentFavorites.remove(it)
            } else {
                currentFavorites.add(it)
            }
            favoriteMarkers.tryEmit(currentFavorites.toMutableSet())
        }

        getApplication<MooSikApp>().putPreference(
            "favoriteMarkers", favoriteMarkers.value.markersToJsonArray())
    }

    fun toggleCategory(id: String) {
        val updatedCategories = categories.value.map { category ->
            if (category.id == id) {
                category.copy(
                    subCategories = category.subCategories.map {
                        if (it.isFixed) {
                            it.copy(isChecked = true)
                        }
                        else
                        {
                            it.copy(isChecked = !category.isChecked)
                        }
                    }
                )
            }
            else
            {
                category
            }
        }
        categories.tryEmit(updatedCategories)
    }

    fun toggleSubCategory(id: String, sub : String) {
        val updatedCategories = categories.value.map { category ->
            if (category.id == id) {
                val subCategories = category.subCategories.map { subCategory ->
                    if (subCategory.id == sub) {
                        subCategory.copy(isChecked = !subCategory.isChecked)
                    }
                    else {
                        subCategory
                    }
                }
                category.copy(subCategories = subCategories)
            }
            else
            {
                category
            }
        }
        categories.tryEmit(updatedCategories)
    }

    fun searchMarkers(keyword: String) {
        _searchText.tryEmit(keyword)
    }

    fun addMarker(marker: Marker) {
        _finalSearchedMarker.tryEmit(marker)
    }

    fun clearSearchedMarker() {
        _finalSearchedMarker.tryEmit(null)
        _searchedMarkers.tryEmit(null)
    }

    private fun readMarkersFromDevice() : Flow<ArrayList<Marker>> = flow {
        emit(loadData())
    }

    @Throws(IOException::class, CsvException::class)
    private suspend fun loadData() : ArrayList<Marker> = suspendCoroutine { sender ->
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
                    kung = content[6],
                    type = content[4].replace("\"dr'","dr"),
                    irm1 = content[7],
                    irm3 = content[7] + "(EN)")
                )
            }
        }
        sender.resume(markers)
    }

    private fun MutableSet<Marker>.markersToJsonArray(): String {
        return Json.encodeToString(this.toList())
    }

    private fun String.jsonArrayToMarkers(): MutableSet<Marker> {
        return if (TextUtils.isEmpty(this))
            mutableSetOf() else Json.decodeFromString<List<Marker>>(this).toMutableSet()
    }
}