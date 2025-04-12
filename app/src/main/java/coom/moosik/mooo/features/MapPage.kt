package coom.moosik.mooo.features

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.AssetManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.util.TypedValue
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.FabPosition
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
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
import coom.moosik.mooo.extensions.isExplicitDeniedPermissions
import coom.moosik.mooo.extensions.isPermissionGranted
import coom.moosik.mooo.extensions.showTwoButtonDialog
import coom.moosik.mooo.model.Marker
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStreamReader
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.PointMode.Companion.Polygon
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.Visibility
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.CameraMoveStartedReason
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.MarkerInfoWindow
import com.google.maps.android.compose.MarkerInfoWindowContent
import com.google.maps.android.compose.Polygon
import coom.moosik.mooo.composable.notoSansFonts
import coom.moosik.mooo.extensions.isNumeric
import coom.moosik.mooo.extensions.openBrowser
import coom.moosik.mooo.extensions.showToast
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import java.util.Locale

typealias SimpleListener = (Boolean) -> Unit
typealias ListenerForResult = (ActivityResult) -> Unit
typealias PermissionListenerForResult = (requestCode: Int, permissions: Array<out String>, grantResults: IntArray) -> Unit

class MapPage : CommonPage() {

    private val model : MapPageViewModel by viewModels()

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

        requestLocationReadPermission { isGranted ->
            if (isGranted) {
                loadDeviceLocation()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun loadDeviceLocation() {
        LocationServices.getFusedLocationProviderClient(this).lastLocation
            .addOnSuccessListener { success: Location? ->
                success?.let { location ->
                    showToast("현재 위치로 이동 합니다")
                    model.updatePosition(location.latitude, location.longitude)
                }
            }
            .addOnFailureListener {
            }
    }

    private fun loadAddress(lat: Double, lng: Double): List<Address>? {
        lateinit var address: List<Address>

        return try {
            val geocoder = Geocoder(this, Locale.KOREA)
            address = geocoder.getFromLocation(lat, lng, 1) as List<Address>
            address
        } catch (e: IOException) {
            null
        }
    }

    private fun requestLocationReadPermission(listener: SimpleListener) {
        val notificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayListOf(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            arrayListOf()
        }
        val locationPermissions = arrayListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION)
        val permissions = arrayListOf<String>()
        permissions.addAll(notificationPermission)
        permissions.addAll(locationPermissions)

        if (isPermissionGranted(*permissions.toTypedArray())) {
            listener(true)
        }
        else if (isExplicitDeniedPermissions(*notificationPermission.toTypedArray()))
        {
            showTwoButtonDialog(
                resources.getString(R.string.request_permission_location_title),
                resources.getString(R.string.request_permission_location_message_to_setting)) {

                if (it) {
                    lifecycleScope.launch {
                        moveToSettingPage {
                            listener(isPermissionGranted(*notificationPermission.toTypedArray()))
                        }
                    }
                }
            }
        }
        else if (isExplicitDeniedPermissions(*locationPermissions.toTypedArray()))
        {
            showTwoButtonDialog(
                resources.getString(R.string.request_permission_location_title),
                resources.getString(R.string.request_permission_location_message_to_setting)) {

                if (it) {
                    lifecycleScope.launch {
                        moveToSettingPage {
                            listener(isPermissionGranted(*locationPermissions.toTypedArray()))
                        }
                    }
                }
            }
        }
        else
        {
            requestPermissions(permissions.toTypedArray()) { _, _, _ ->
                listener(isPermissionGranted(*permissions.toTypedArray()))
            }
        }
    }

    @Preview
    @Composable
    fun MapPageLayout(modifier: Modifier = Modifier) {

        val scope = rememberCoroutineScope()

        val markers by model.markers.collectAsState()
        val highlightsMarkers by model.highlightsMarkers.collectAsState()

        val selectedMarker by model.selectedMarker.collectAsState()
        val selectedLanguage by model.selectedLanguage.collectAsState()

        val languageSelectMode by model.languageSelectMode.collectAsState()

        val currentPosition by model.currentPosition.collectAsState()

        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(currentPosition.second, 15f)
        }

        val cameraZoomLevel by remember { derivedStateOf { cameraPositionState.position.zoom } }
        val visibleRegion = remember { mutableStateOf<LatLngBounds?>(null) }

        // 카메라 위치가 변경될 때마다 visibleRegion 업데이트
        LaunchedEffect(cameraPositionState.position) {
            snapshotFlow { cameraPositionState.projection?.visibleRegion }.collectLatest { region ->
                // visibleRegion.value = region
                region?.let { model.visibleRegion.tryEmit(it) }
            }
        }

        val cameraMoveStartedReason by remember { derivedStateOf { cameraPositionState.cameraMoveStartedReason } }

        LaunchedEffect(cameraMoveStartedReason) {
            snapshotFlow { cameraMoveStartedReason }.collectLatest {
                if (it == CameraMoveStartedReason.GESTURE) {
                    model.selectMarker(null, null)
                }
            }
        }

        Scaffold(
            content = { paddingValues ->

                Box(modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color.White)) {

                    LaunchedEffect(currentPosition) {
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(currentPosition.second, 16f)
                    }

                    GoogleMap(
                        modifier = Modifier
                            .fillMaxSize(),
                        googleMapOptionsFactory = {
                            GoogleMapOptions().mapType(com.google.android.gms.maps.GoogleMap.MAP_TYPE_NORMAL)
                        },
                        onMapClick = { model.selectMarker(null, null) },
                        cameraPositionState = cameraPositionState)
                    {

//                        if (cameraZoomLevel > 18f) {
//                            val polygonPoints = listOf(
//                                LatLng(37.5665, 126.9780), // 서울
//                                LatLng(35.1796, 129.0756), // 부산
//                                LatLng(36.3504, 127.3845), // 대전
//                                LatLng(35.8714, 128.6014), // 대구
//                                LatLng(37.5665, 126.9780),
//                            )
//                            Polygon(
//                                points = polygonPoints,
//                                fillColor = Color.Blue.copy(alpha = 0.3f),
//                                strokeColor = Color.Blue,
//                                strokeWidth = 5f
//                            )
//                        }
//                        else
//                        {
//
//                        }

                        for (marker in markers) {
                            val iconIdentifier = resources.getIdentifier(marker.type, "drawable", packageName)
                            if (iconIdentifier != 0) {

                                val icon = BitmapDescriptorFactory.fromResource(iconIdentifier)
                                val latLng = LatLng(marker.latitude, marker.longitude)

                                MarkerInfoWindow(
                                    state = MarkerState(position = latLng),
                                    icon = icon,
                                    onClick = { selectedMarker ->

                                        scope.launch {
                                            cameraPositionState.animate(
                                                update = CameraUpdateFactory.newCameraPosition(
                                                    CameraPosition.fromLatLngZoom(selectedMarker.position, 16f))
                                            )
                                        }

                                        scope.launch {
                                            delay(1000)

                                            val projection = cameraPositionState.projection
                                            val anchorPoint = projection?.toScreenLocation(selectedMarker.position)

                                            model.selectMarker(marker, anchorPoint)
                                        }

                                        return@MarkerInfoWindow true
                                    },
                                )
                            }
                        }

                        for (highlightsMarker in highlightsMarkers) {

                            val iconIdentifier = resources.getIdentifier("x"+highlightsMarker.type, "drawable", packageName)
                            if (iconIdentifier != 0) {
                                val icon = BitmapDescriptorFactory.fromResource(iconIdentifier)
                                val latLng = LatLng(highlightsMarker.latitude, highlightsMarker.longitude)

                                MarkerInfoWindow(
                                    state = MarkerState(position = latLng),
                                    icon = icon,
                                    onClick = { selectedMarker ->

                                        scope.launch {
                                            cameraPositionState.animate(
                                                update = CameraUpdateFactory.newCameraPosition(
                                                    CameraPosition.fromLatLngZoom(selectedMarker.position, 16f))
                                            )
                                        }

                                        scope.launch {
                                            delay(1000)

                                            val projection = cameraPositionState.projection
                                            val anchorPoint = projection?.toScreenLocation(selectedMarker.position)

                                            model.selectMarker(highlightsMarker, anchorPoint)
                                        }

                                        return@MarkerInfoWindow true
                                    },
                                )
                            }
                        }

                        Marker(
                            state = MarkerState(position = currentPosition.second),
                            title = " 현재 위치 ",
                        )
                    }

                    BorderButton(modifier =  Modifier.width(100.dp)
                        .align(Alignment.TopStart).offset(x= 7.5.dp, y = 7.5.dp), text = " 검 색 ") {
                        model.selectMarker(null, null)

                    }

                    BorderButton(modifier =  Modifier.width(100.dp)
                        .align(Alignment.TopStart).offset(x= 7.5.dp, y = 65.dp), text = " 현재 위치 ") {

                        model.selectMarker(null, null)

                        requestLocationReadPermission { isGranted ->
                            if (isGranted) {
                                loadDeviceLocation()
                            }
                        }
                    }

                    BorderButton(modifier =  Modifier.wrapContentWidth().offset(x= (-7.5).dp, y = 7.5.dp)
                        .align(Alignment.TopEnd), text = " 목 록 ") {
                        model.selectMarker(null, null)

                        CategorySelectDialog().show(supportFragmentManager, "")
                    }


                    if (languageSelectMode) {
                        LanguageSelectLayout(modifier = Modifier
                            .offset(x= (7.5).dp, y = (-7.5).dp)
                            .align(Alignment.BottomStart),
                            onLanguageClick = { language ->
                                model.selectedLanguage.tryEmit(language)
                                model.languageSelectMode.tryEmit(false)
                        })
                    }
                    else
                    {
                        BorderButton(modifier =  Modifier.wrapContentWidth().offset(x= (7.5).dp, y = (-7.5).dp)
                            .align(Alignment.BottomStart), text = selectedLanguage) {
                            model.languageSelectMode.tryEmit(true)
                        }
                    }

                    selectedMarker?.let { selectedMarker ->

                        val offsetX = remember { mutableIntStateOf(0) }
                        val offsetY = remember { mutableIntStateOf(0) }

                        Card(modifier = Modifier.background(Color.Transparent)
                            .align(Alignment.TopStart).onSizeChanged {
                                offsetX.intValue = it.width / 2
                                offsetY.intValue = it.height +
                                        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60f, resources.displayMetrics).toInt()
                            }.absoluteOffset {
                                IntOffset(
                                    selectedMarker.second.x - offsetX.intValue,
                                    selectedMarker.second.y - offsetY.intValue)
                            },
                            shape = RoundedCornerShape(12.dp),
                            backgroundColor = Color.White,
                            elevation = 5.dp,) {

                            Row(modifier = Modifier
                                .wrapContentWidth()
                                .wrapContentHeight()
                                .padding(7.5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.5.dp)) {

                                var imageResource by remember { mutableIntStateOf(R.drawable.check_off) }
                                var favoriteState by remember { mutableStateOf("찜하기") }

                                LaunchedEffect(model.favoriteMarkers) {
                                    model.favoriteMarkers.collectLatest { favoriteMarkers ->
                                        imageResource = if (favoriteMarkers.contains(selectedMarker.first))
                                            R.drawable.check_on else R.drawable.check_off
                                        favoriteState = if (favoriteMarkers.contains(selectedMarker.first))
                                            "찜해제" else "찜하기"
                                    }
                                }

                                val selectedMarkerText = if (selectedLanguage == "한국어")
                                    selectedMarker.first.irm1 else selectedMarker.first.irm3

                                ClickableText(
                                    selectedMarkerText = selectedMarkerText,
                                    onTextClick = {
                                        var tail = selectedMarker.first.kung
                                        if (!tail.startsWith("korea")) {
                                            tail = "korea/${selectedMarker.first.kung}"
                                        }
                                        openBrowser("http://gajaguyo.com/wp/$tail")
                                    }
                                )

//                                IconButton(onClick = { model.toggleHeart(selectedMarker.first) }) {
//                                    Icon(
//                                        modifier = Modifier.height(24.dp).width(24.dp),
//                                        painter = painterResource(id = imageResource), // 이미지 리소스
//                                        contentDescription = null,
//                                        tint = Color.Unspecified
//                                    )
//                                }
                                TextButton(
                                    modifier =  Modifier.wrapContentWidth(),
                                    text = favoriteState) {
                                    model.toggleHeart(selectedMarker.first)
                                }
                            }
                        }
                    }
                }
            }
        )
    }

    @Composable
    fun ClickableText(
        selectedMarkerText: String,
        onTextClick: () -> Unit
    ) {
        Text(
            text = selectedMarkerText,
            color = Color.Black,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = notoSansFonts,
            modifier = Modifier.clickable { onTextClick() } // 클릭 이벤트 추가
        )
    }
}
