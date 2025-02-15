package coom.moosik.mooo.extensions

import android.R
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import coom.moosik.mooo.features.CommonDialog
import coom.moosik.mooo.features.KeyBoardVisibilityListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun Activity.getStatusBarHeight(): Int {
    val rect = Rect()
    this.window.decorView.getWindowVisibleDisplayFrame(rect)
    return rect.top
}
fun Activity.isExplicitDeniedPermissions(vararg permissions : String) =
    getExplicitDeniedPermissions(*permissions).isNotEmpty()

fun Activity.getExplicitDeniedPermissions(vararg permissions : String) : Array<String> {
    val deniedPermissionSet = mutableSetOf<String>()
    for (permission in permissions) {
        var isDenied = true
        isDenied = (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)).and(isDenied)
        isDenied = (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED).and(isDenied)
        if (isDenied) {
            deniedPermissionSet.add(permission)
        }
    }
    return deniedPermissionSet.toTypedArray()
}

fun Activity.setKeyboardEventListener(listener: KeyBoardVisibilityListener) {
    val softInputMethod: Int = this.window.attributes.softInputMode
    if (WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE != softInputMethod &&
        WindowManager.LayoutParams.SOFT_INPUT_ADJUST_UNSPECIFIED != softInputMethod) {
        return
    }
    val activityRoot: View = (this.findViewById<ViewGroup>(R.id.content)!!).getChildAt(0)
    val layoutListener : ViewTreeObserver.OnGlobalLayoutListener = object : ViewTreeObserver.OnGlobalLayoutListener {
        private var wasOpened = false
        override fun onGlobalLayout() {
            val r = Rect()
            activityRoot.getWindowVisibleDisplayFrame(r)

            var screenHeight = activityRoot.rootView.height
            if (screenHeight > 0 && hasSoftNavigationBar()) {
                val navigationBarHeight: Int = getNavigationBarHeight()
                screenHeight -= navigationBarHeight
            }
            val keyboardHeight = screenHeight - r.bottom
            val keyboardOpen = keyboardHeight >= 100
            if (keyboardOpen != wasOpened) {
                wasOpened = keyboardOpen
                if (keyboardOpen) {
                    listener.onKeyBoardShow(keyboardHeight)
                }
                else
                {
                    listener.onKeyBoardHide()
                }
            }
        }
    }
    activityRoot.viewTreeObserver.addOnGlobalLayoutListener(layoutListener)

    application.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(p0: Activity, p1: Bundle?) {
        }

        override fun onActivityStarted(p0: Activity) {
        }

        override fun onActivityResumed(p0: Activity) {
        }

        override fun onActivityPaused(p0: Activity) {
        }

        override fun onActivityStopped(p0: Activity) {
        }

        override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
        }

        override fun onActivityDestroyed(p0: Activity) {
            activityRoot.viewTreeObserver.removeOnGlobalLayoutListener(layoutListener)
            application.unregisterActivityLifecycleCallbacks(this)
        }
    })
}

fun Context.runDelay(sleep: Long, runTask: (() -> Unit)) = CoroutineScope(Dispatchers.Main).launch {
    delay(sleep)

    runTask.invoke()
}
fun Context.showToast(resourceId: Int) =
    Toast.makeText(this, resourceId, Toast.LENGTH_SHORT).show()

fun Context.showToast(text: String) =
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
fun Context.showDialog(
    vararg messages : String?, buttonCount : CommonDialog.ButtonCount = CommonDialog.ButtonCount.ONE, listener : ((confirmed : Boolean) -> Unit)? = null)
{
    try
    {
        val dialog = CommonDialog(this, *messages, buttonCount = buttonCount)
        dialog.setOnDismissListener {
            listener?.invoke(dialog.mConfirmed)
        }
        dialog.show()
    }
    catch (e : Exception) {
        e.printStackTrace()
    }
}

fun Context.showTwoButtonDialog(
    vararg messages : String?, listener : ((confirmed : Boolean) -> Unit)? = null)
{
    try
    {
        val dialog = CommonDialog(this, *messages, buttonCount = CommonDialog.ButtonCount.TWO)
        dialog.setOnDismissListener {
            listener?.invoke(dialog.mConfirmed)
        }
        dialog.show()
    }
    catch (e : Exception) {
        e.printStackTrace()
    }
}