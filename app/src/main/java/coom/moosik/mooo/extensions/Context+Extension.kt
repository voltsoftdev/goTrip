package coom.moosik.mooo.extensions

//noinspection SuspiciousImport

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.util.Base64
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import java.security.MessageDigest
import java.util.Locale

fun Context.openBrowser(url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    this.startActivity(intent)
}

fun Context.inflate(resourceId: Int, viewGroup: ViewGroup?, attachRoot: Boolean = false) : View? =
    LayoutInflater.from(this).inflate(resourceId, viewGroup, attachRoot)

fun Context.dpToPx(dp : Float) : Int =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, this.resources.displayMetrics).toInt()

fun Context.isNotificationPermissionGranted() : Boolean {
    val notificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayListOf(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        arrayListOf()
    }
    return isPermissionGranted(*notificationPermission.toTypedArray())
}

fun Context.isPermissionGranted(vararg permissions : String) : Boolean {
    var result = true
    for (permission in permissions) {
        result = (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED).and(result)
    }
    return result
}

fun Context.registerNetworkStatusCallback(listener: ConnectivityManager.NetworkCallback) {
    val connectivityManager =
        this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    connectivityManager.registerDefaultNetworkCallback(listener)
}

fun Context.unregisterNetworkStatusCallback(listener: ConnectivityManager.NetworkCallback) {
    val connectivityManager =
        this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    connectivityManager.unregisterNetworkCallback(listener)
}

fun Context.isNetworkAvailable(): Boolean {
    val connectivityManager =
        this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork = connectivityManager.activeNetworkInfo
    return activeNetwork?.isAvailable ?: false
}

fun Context.keyHashBase64(): String? {

    var packageInfo : PackageInfo? = null

    try
    {
        packageInfo = this.packageManager.getPackageInfo(this.packageName, PackageManager.GET_SIGNATURES)
    }
    catch (e : Exception) {
        e.printStackTrace()
    }

    if (packageInfo != null) {

        for (signature in packageInfo.signatures!!) {
            try
            {
                val messageDigest : MessageDigest = MessageDigest.getInstance("SHA")
                messageDigest.update(signature.toByteArray())
                return Base64.encodeToString(messageDigest.digest(), Base64.DEFAULT)
            }
            catch (e : Exception) {
                e.printStackTrace()
            }
        }
    }

    return null
}

fun Context.screenWidth() : Int =
    resources.displayMetrics.widthPixels

fun Context.screenHeight() : Int =
    resources.displayMetrics.heightPixels

fun Context.hasSoftNavigationBar(): Boolean {

    val viewConfig = ViewConfiguration.get(this)
    var hasSoftNavigationBar = !viewConfig.hasPermanentMenuKey()
    val windowManager = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val display = windowManager.defaultDisplay
    val realDisplayMetrics = DisplayMetrics()
    display.getRealMetrics(realDisplayMetrics)
    val realHeight = realDisplayMetrics.heightPixels
    val realWidth = realDisplayMetrics.widthPixels
    val displayMetrics = DisplayMetrics()
    display.getMetrics(displayMetrics)
    val displayHeight = displayMetrics.heightPixels
    val displayWidth = displayMetrics.widthPixels
    hasSoftNavigationBar = realWidth - displayWidth > 0 || realHeight - displayHeight > 0
    return hasSoftNavigationBar
}

@SuppressLint("InternalInsetResource")
fun Context.getNavigationBarHeight(): Int {
    val resources = this.resources
    val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
    return if (resourceId > 0) {
        resources.getDimensionPixelSize(resourceId)
    } else 0
}

@SuppressLint("DiscouragedApi")
fun Context.getLocalizedString(name : String) : String {
    return try {
        val identifier = resources.getIdentifier(name, "string", packageName)
        resources.getString(identifier)
    } catch (e : Exception) {
        ""
    }
}

const val PREFERENCE_NAME = "goTrip_preference"

fun Activity.getRootView() : View {
    return window.decorView.findViewById(android.R.id.content)
}

fun Context.getPreferenceString(name: String) : String {
    val sharedPreferences = this.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
    return sharedPreferences.getString(name, "") ?: ""
}

fun Context.getPreferenceBoolean(name: String) : Boolean {
    val sharedPreferences = this.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
    return sharedPreferences.getBoolean(name, false)
}

fun Context.getPreferenceLong(name: String, default : Long) : Long {
    val sharedPreferences = this.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
    return sharedPreferences.getLong(name, default)
}

fun Context.getPreferenceFloat(name: String, default : Float) : Float {
    val sharedPreferences = this.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
    return sharedPreferences.getFloat(name, default)
}

@SuppressLint("CommitPrefEdits")
fun Context.putPreference(name: String, value : String?) {
    val sharedPreferences = this.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    editor.putString(name, value)
    editor.apply()
}

@SuppressLint("CommitPrefEdits")
fun Context.putPreference(name: String, value : Boolean) {
    val sharedPreferences = this.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    editor.putBoolean(name, value)
    editor.apply()
}

@SuppressLint("CommitPrefEdits")
fun Context.putPreference(name: String, value : Int) {
    val sharedPreferences = this.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    editor.putInt(name, value)
    editor.apply()
}

@SuppressLint("CommitPrefEdits")
fun Context.putPreference(name: String, value : Float) {
    val sharedPreferences = this.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    editor.putFloat(name, value)
    editor.apply()
}

@SuppressLint("CommitPrefEdits")
fun Context.putPreference(name: String, value : Long) {
    val sharedPreferences = this.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    editor.putLong(name, value)
    editor.apply()
}

