package coom.moosik.mooo.features

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import coom.moosik.mooo.R
import coom.moosik.mooo.extensions.screenHeight
import coom.moosik.mooo.extensions.screenWidth
import coom.moosik.mooo.extensions.setAnimationEffectBounce

class CommonProgress(context: Context, var message: String? = null) : Dialog(context)
{
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (window != null) {
            window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window!!.setBackgroundDrawableResource(android.R.color.transparent)
            val layoutParam : WindowManager.LayoutParams = window!!.attributes
            layoutParam.width = WindowManager.LayoutParams.MATCH_PARENT
            layoutParam.height = WindowManager.LayoutParams.MATCH_PARENT
            layoutParam.windowAnimations = R.style.AnimationPopupStyle
            layoutParam.dimAmount = 0.0f
            layoutParam.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND

            window!!.attributes = layoutParam
        }

        setContentView(R.layout.view_progress_dialog)

        val imageView : ImageView = findViewById(R.id.splash_image)
        this.startBounceAnimation(imageView)

        val textView : TextView = findViewById(R.id.messageView)
        if (!TextUtils.isEmpty(message)) {
            textView.text = message
        }

        val backgroundView : View = findViewById(R.id.dialogBackgroundView)
        backgroundView.layoutParams.width = context.screenWidth()
        backgroundView.layoutParams.height = context.screenHeight()
        backgroundView.requestLayout()
    }

    private fun startBounceAnimation(imageView: ImageView) {
        val runnable : Runnable = object : Runnable {
            override fun run() {
                if (isShowing) {
                    imageView.setAnimationEffectBounce()
                    imageView.postDelayed(this, 2000)
                }
            }
        }
        imageView.postDelayed(runnable, 500)
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
    }

    fun updateMessage(message : String) {
        val textView : TextView = findViewById(R.id.messageView)
        textView.text = message
    }
}