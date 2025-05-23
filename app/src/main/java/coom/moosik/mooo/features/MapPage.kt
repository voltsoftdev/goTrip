package coom.moosik.mooo.features

import android.Manifest
import android.annotation.SuppressLint
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.CameraMoveStartedReason
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerInfoWindow
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import coom.moosik.mooo.R
import coom.moosik.mooo.composable.ViewModelDemoTheme
import coom.moosik.mooo.composable.notoSansFonts
import coom.moosik.mooo.extensions.isExplicitDeniedPermissions
import coom.moosik.mooo.extensions.isPermissionGranted
import coom.moosik.mooo.extensions.openBrowser
import coom.moosik.mooo.extensions.showToast
import coom.moosik.mooo.extensions.showTwoButtonDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.Locale

typealias SimpleListener = (Boolean) -> Unit
typealias ListenerForResult = (ActivityResult) -> Unit
typealias PermissionListenerForResult = (requestCode: Int, permissions: Array<out String>, grantResults: IntArray) -> Unit

class MapPage : CommonPage() {

    private val model : MapPageViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 구글맵 초기화 합니다
        MapsInitializer.initialize(applicationContext)
        // 화면을 구성합니다
        setContent {
            ViewModelDemoTheme {
                Surface(modifier = Modifier.fillMaxSize(),  color = MaterialTheme.colors.background) {
                    MapPageLayout() // 여기 서부터 앱 화면 구성을 시작합니다
                }
            }
        }
        // 위치 정보를 불러 오기 위해 앱 권한을 체크 합니다. 사용자가 권한을 승인한 경우에만 loadDeviceLocation 를 실행합니다
        requestLocationReadPermission { isGranted ->
            if (isGranted) {
                loadDeviceLocation()
            }
        }
    }

    /**
     * 현재 위치를 불러오는 함수입니다.
     */
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
    // 사용에게 위치 정보 승인을 요청 하는 함수 입니다.
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
        // (1) 이미 승인 된 경우
        if (isPermissionGranted(*permissions.toTypedArray())) {
            listener(true)
        } // (2) 사용자가 명시적으로 권한 승인을 거부한 경우 > 앱에서 추가 요청할 수 없으므로 설정화면으로 이동해서 권한 승인을 유도합니다
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
        // (3) 거부가 되어 있으므로 사용자에게 권한 승인을 요청합니다
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
        val finalSearchedMarker by model.finalSearchedMarker.collectAsState()
        val highlightsMarkers by model.highlightsMarkers.collectAsState()

        val selectedMarker by model.selectedMarker.collectAsState() // 내가 선택한 마커들
        val selectedLanguage by model.selectedLanguage.collectAsState() // 선택한 언어 모드

        val languageSelectMode by model.languageSelectMode.collectAsState() // 언어 모드 선택으로 진입 했는지 여부

        val currentPosition by model.currentPosition.collectAsState() // 사용자 현재 위치

        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(currentPosition.second, 15f)
        }

        val configuration = LocalConfiguration.current
        val screenWidthDp = remember { configuration.screenWidthDp.dp } // Dp로 변환

        val density = LocalDensity.current.density
        val screenWidthPx = remember(screenWidthDp) { (screenWidthDp.value * density).toInt() }

        val cameraZoomLevel by remember { derivedStateOf { cameraPositionState.position.zoom } }
        val visibleRegion by remember { mutableStateOf<LatLngBounds?>(null) }

        // 카메라 위치가 변경될 때마다 visibleRegion 업데이트
        LaunchedEffect(cameraPositionState.position) {
            snapshotFlow { cameraPositionState.projection?.visibleRegion }.collectLatest { region ->
                region?.let { model.visibleRegion.tryEmit(it) }
            }
        }

        val cameraMoveStartedReason by remember { derivedStateOf { cameraPositionState.cameraMoveStartedReason } }

        LaunchedEffect(cameraMoveStartedReason) {
            snapshotFlow { cameraMoveStartedReason }.collectLatest {
                if (it == CameraMoveStartedReason.GESTURE) {
                    model.selectMarker(null, null)
                    model.clearSearchedMarker()
                }
            }
        }
        // 화면 전체를 Scaffold 로 구성합니다 (안드로이드 기본 레이아웃)
        Scaffold(
            content = { paddingValues ->
                // 내부를 커다란 박스로 채웁니다
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
                        onMapClick = {
                            model.selectMarker(null, null)
                            model.clearSearchedMarker()
                        },
                        cameraPositionState = cameraPositionState)
                    {

                        for (marker in markers) {
                            var image = ""
                            if (highlightsMarkers.contains(marker)) {
                                image += "x"
                            }
                            image += marker.type

                            val iconIdentifier = resources.getIdentifier(image, "drawable", packageName)
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
                                                    CameraPosition.fromLatLngZoom(selectedMarker.position, 16f)
                                                ), durationMs = 250
                                            )
                                            val projection = cameraPositionState.projection
                                            val anchorPoint = projection?.toScreenLocation(selectedMarker.position)
                                            model.selectMarker(marker, anchorPoint)
                                        }

                                        scope.launch {
                                            delay(1200)
                                            val projection = cameraPositionState.projection
                                            val anchorPoint = projection?.toScreenLocation(selectedMarker.position)
                                            model.selectMarker(marker, anchorPoint)
                                        }
                                        return@MarkerInfoWindow true
                                    },
                                )
                            }
                        }

                        finalSearchedMarker?.let { marker ->
                            var image = ""
                            if (highlightsMarkers.contains(marker)) {
                                image += "x"
                            }
                            image += marker.type

                            val iconIdentifier = resources.getIdentifier(image, "drawable", packageName)
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
                                                    CameraPosition.fromLatLngZoom(selectedMarker.position, 16f)
                                                ), durationMs = 250
                                            )
                                            val projection = cameraPositionState.projection
                                            val anchorPoint = projection?.toScreenLocation(selectedMarker.position)
                                            model.selectMarker(marker, anchorPoint)
                                        }

                                        scope.launch {
                                            delay(1200)
                                            val projection = cameraPositionState.projection
                                            val anchorPoint = projection?.toScreenLocation(selectedMarker.position)
                                            model.selectMarker(marker, anchorPoint)
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
                        model.clearSearchedMarker()

                        val searchDialog = SearchPlaceDialogFragment { selectedMarker ->

                            if (!markers.contains(selectedMarker)) {
                                model.addMarker(selectedMarker)
                            }

                            val latitude = selectedMarker.latitude
                            val longitude = selectedMarker.longitude
                            scope.launch {
                                cameraPositionState.animate(
                                    update = CameraUpdateFactory.newCameraPosition(
                                        CameraPosition.fromLatLngZoom(LatLng(latitude, longitude), 16f)
                                    ),
                                    durationMs = 500
                                )
                            }

                        }
                        searchDialog.show(supportFragmentManager, "")
                    }
                    // 누르면 현재 위치로 이동하는 버튼 노출
                    BorderButton(modifier =  Modifier.width(100.dp)
                        .align(Alignment.TopStart).offset(x= 7.5.dp, y = 65.dp), text = " 현재 위치 ") {

                        model.selectMarker(null, null)
                        model.clearSearchedMarker()

                        requestLocationReadPermission { isGranted ->
                            if (isGranted) {
                                loadDeviceLocation()
                            }
                        }
                    }

                    BorderButton(modifier =  Modifier.wrapContentWidth().offset(x= (-7.5).dp, y = 7.5.dp)
                        .align(Alignment.TopEnd), text = " 목 록 ") {
                        model.selectMarker(null, null)
                        model.clearSearchedMarker()

                        ImageDialogFragment(R.drawable.mok).show(supportFragmentManager, "")
                    }

                    BorderButton(modifier =  Modifier.wrapContentWidth().offset(x= (0).dp, y = 7.5.dp)
                        .align(Alignment.TopCenter), text = " 설 정 ") {
                        model.selectMarker(null, null)
                        model.clearSearchedMarker()

                        CategorySelectDialog().show(supportFragmentManager, "")
                    }

                    // 언어 선택 모드 진입 했다면 , 언어 모드를 선택할 수 있는 레이아웃(버튼 2개 짜리) 를 노출
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
                    // 선택한 마커 정보를 화면에 노출
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
                                    (screenWidthPx / 2) - offsetX.intValue,
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
                            }
                        }
                        // 하이라이트 마커 갯수가 50개 미만인 경우에만 찜하기 기능 노출
                        val highlightsMarkersCount = highlightsMarkers.size
                        if (highlightsMarkersCount < 50) {
                            
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

                            BorderButton(
                                modifier =  Modifier.wrapContentWidth()
                                    .offset(x= (-7.5).dp, y = 65.0.dp)
                                    .align(Alignment.TopEnd),
                                text = favoriteState) {
                                model.toggleHeart(selectedMarker.first)
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
