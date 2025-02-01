package coom.moosik.mooo.features

import android.os.Bundle
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
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import coom.moosik.mooo.composable.ViewModelDemoTheme


class MapPage : CommonPage() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ViewModelDemoTheme {
                Surface(modifier = Modifier.fillMaxSize(),  color = MaterialTheme.colors.background) {
                    MapPageLayout()
                }
            }
        }
    }
}

@Preview
@Composable
fun MapPageLayout(modifier: Modifier = Modifier) {

    ConstraintLayout(modifier = modifier
        .fillMaxSize()
        .background(Color.White)) {

        val latLng = LatLng(37.7387295, 127.0458908)
        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(latLng, 15f)
        }
        val titleBarRef = createRef()

        GoogleMap(
            modifier = Modifier.fillMaxSize().constrainAs(titleBarRef) {
                absoluteLeft.linkTo(parent.absoluteLeft)
                top.linkTo(parent.top)
            },
            cameraPositionState = cameraPositionState) {

            Marker(
                state = MarkerState(position = latLng),
                title = "의정부역",
                snippet = "Uijeongbu subway"
            )
        }
    }
}