package coom.moosik.mooo.features

import android.content.res.AssetManager
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.constraintlayout.compose.ConstraintLayout
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.opencsv.CSVReader
import com.opencsv.exceptions.CsvException
import coom.moosik.mooo.R
import coom.moosik.mooo.composable.ViewModelDemoTheme
import coom.moosik.mooo.model.Marker
import java.io.IOException
import java.io.InputStreamReader


class MapPage : CommonPage() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MapsInitializer.initialize(applicationContext)

        setContent {
            ViewModelDemoTheme {
                Surface(modifier = Modifier.fillMaxSize(),  color = MaterialTheme.colors.background) {
                    MapPageLayout()
                }
            }
        }
    }

    @Throws(IOException::class, CsvException::class)
    private fun loadData() : ArrayList<Marker> {
        val assetManager: AssetManager = this.assets
        val inputStream = assetManager.open("adata0.csv")
        val csvReader = CSVReader(InputStreamReader(inputStream, "utf-8"))

        val markers = arrayListOf<Marker>()
        val allContent = csvReader.readAll() as List<Array<String>>
        for (content in allContent) {
            if (content[0].isNumeric()) {
                markers.add(Marker(
                    latitude = content[0].toDouble(),
                    longitude = content[1].toDouble(),
                    type = content[4],
                    irm1 = content[7]))
            }
        }
        return markers
    }

    @Preview
    @Composable
    fun MapPageLayout(modifier: Modifier = Modifier) {

        val iconBitmap = BitmapDescriptorFactory.fromResource(R.drawable.bhs)

        val markers = loadData()

        ConstraintLayout(modifier = modifier
            .fillMaxSize()
            .background(Color.White)) {

            val latingZoom = LatLng(37.7387295, 127.0458908)
            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(latingZoom, 15f)
            }
            val titleBarRef = createRef()

            GoogleMap(
                modifier = Modifier
                    .fillMaxSize()
                    .constrainAs(titleBarRef) {
                        absoluteLeft.linkTo(parent.absoluteLeft)
                        top.linkTo(parent.top)
                    },
                cameraPositionState = cameraPositionState) {

                for (marker in markers) {

                    val iconIdentifier = resources.getIdentifier(marker.type, "drawable", packageName)
                    if (iconIdentifier != 0) {
                        val icon = BitmapDescriptorFactory.fromResource(iconIdentifier)

                        val latLng = LatLng(marker.latitude, marker.longitude)
                        Marker(
                            state = MarkerState(position = latLng),
                            title = marker.irm1,
                            icon = icon
                        )
                    }

                }
            }
        }
    }

    fun String.isNumeric(): Boolean {
        val regex = "-?\\d+(\\.\\d+)?".toRegex()
        return this.matches(regex)
    }
}
