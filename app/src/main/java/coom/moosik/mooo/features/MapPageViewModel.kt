package coom.moosik.mooo.features

import android.app.Application
import android.content.res.AssetManager
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.opencsv.CSVReader
import com.opencsv.exceptions.CsvException
import coom.moosik.mooo.MooSikApp
import coom.moosik.mooo.extensions.isNumeric
import coom.moosik.mooo.model.Marker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONArray
import java.io.IOException
import java.io.InputStreamReader

class MapPageViewModel(application: Application) : AndroidViewModel(application) {

    private var _currentPosition : MutableStateFlow<Pair<Long, LatLng>> = MutableStateFlow(
        Pair(System.currentTimeMillis(), LatLng(37.574187, 126.976882)))

    val currentPosition : StateFlow<Pair<Long, LatLng>>
        get() = this._currentPosition

    var markers : MutableState<List<Marker>> = mutableStateOf(emptyList())

    init {
        markers.value = loadData()
    }

    fun updatePosition(latitude : Double, longitude: Double) {
        _currentPosition.tryEmit(Pair(System.currentTimeMillis(), LatLng(latitude, longitude)))
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