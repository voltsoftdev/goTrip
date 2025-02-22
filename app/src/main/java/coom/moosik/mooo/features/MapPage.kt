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
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptor
import coom.moosik.mooo.extensions.isNumeric
import coom.moosik.mooo.extensions.showToast
import kotlinx.coroutines.flow.collectLatest
import java.util.Locale

typealias SimpleListener = (Boolean) -> Unit
typealias ListenerForResult = (ActivityResult) -> Unit
typealias PermissionListenerForResult = (requestCode: Int, permissions: Array<out String>, grantResults: IntArray) -> Unit

class MapPage : CommonPage() {

    val model : MapPageViewModel by viewModels()

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

        val markers by model.markers.collectAsState()

        val currentPosition by model.currentPosition.collectAsState()

        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(currentPosition.second, 15f)
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = "가자구요") },
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        CategorySelectDialog().show(supportFragmentManager, "")
                    },
                    backgroundColor = MaterialTheme.colors.primary,
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu"
                    )
                }
            },
            floatingActionButtonPosition = FabPosition.Start,
            content = { paddingValues ->

                ConstraintLayout(modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color.White)) {

                    val titleBarRef = createRef()

                    LaunchedEffect(currentPosition) {
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(currentPosition.second, 16f)
                    }

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

                        Marker(
                            state = MarkerState(position = currentPosition.second),
                            title = "현재 위치",
                        )
                    }

                    val currentPositionButtonRef = createRef()
                    FloatingActionButton(
                        modifier = Modifier
                            .width(32.dp).height(32.dp)
                            .constrainAs(currentPositionButtonRef) {
                                absoluteLeft.linkTo(parent.absoluteLeft, margin = 15.dp)
                                top.linkTo(parent.top, margin = 15.dp)
                            },
                        onClick = {
                            requestLocationReadPermission { isGranted ->
                                if (isGranted) {
                                    loadDeviceLocation()
                                }
                            }
                        },
                        backgroundColor = MaterialTheme.colors.primary,
                    ) {
                        Icon(
                            imageVector = Icons.Default.GpsFixed,
                            contentDescription = "Menu"
                        )
                    }

                    val currentPositionButtonLabelRef = createRef()
                    Text(text = "현재 위치로 이동하기",
                        fontSize = 14.sp,
                        modifier = Modifier.constrainAs(currentPositionButtonLabelRef) {
                        absoluteLeft.linkTo(currentPositionButtonRef.absoluteRight, margin = 7.5.dp)
                        top.linkTo(currentPositionButtonRef.top)
                        bottom.linkTo(currentPositionButtonRef.bottom)
                    })
                }
            }
        )
    }
}
