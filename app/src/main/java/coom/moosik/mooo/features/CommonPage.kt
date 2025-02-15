package coom.moosik.mooo.features

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import coom.moosik.mooo.BuildConfig
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

const val REQUEST_PERMISSION = 100

abstract class CommonPage : AppCompatActivity() {

    private lateinit var launcher : ActivityResultLauncher<Intent>

    private var resultListener : ListenerForResult? = null
    private var permissionListener : PermissionListenerForResult? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionListener?.invoke(requestCode, permissions, grantResults)
        permissionListener = null
    }

    fun requestPermissions(permissions: Array<String>, listener: PermissionListenerForResult) {
        permissionListener = listener
        ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSION)
    }

    suspend fun moveToSettingPage(listener: ListenerForResult) = suspendCoroutine {
        val uri = Uri.parse("package:${BuildConfig.APPLICATION_ID}")
        launch(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri)) { result ->
            it.resume(result)
            listener(result)
        }
    }

    fun launch(intent: Intent, listener: ListenerForResult ? = null) {
        resultListener = listener
        launcher.launch(intent)
    }
}