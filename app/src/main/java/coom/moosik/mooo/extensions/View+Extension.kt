package coom.moosik.mooo.extensions

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.drawable.Drawable
import android.text.InputFilter
import android.text.TextUtils
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat


fun View.setAnimationEffectBounce() {
    val animatorSet = AnimatorSet()
    val animator0: Animator =
        ObjectAnimator.ofFloat(this, "ScaleX", this.scaleX, 0.95f, 1.05f, 1.0f)
    animator0.setDuration(600)
    val animator1: Animator =
        ObjectAnimator.ofFloat(this, "ScaleY", this.scaleY, 0.95f, 1.05f, 1.0f)
    animator1.setDuration(600)
    animatorSet.playTogether(animator0, animator1)
    animatorSet.start()
}

fun View.setAnimationEffectBounceAppear() {
    if (this.visibility == View.GONE) {
        this.visibility = View.VISIBLE
        val animatorSet = AnimatorSet()
        val animator0: Animator =
            ObjectAnimator.ofFloat(this, "ScaleX",  0.0f, 1.0f)
        animator0.setDuration(300)
        val animator1: Animator =
            ObjectAnimator.ofFloat(this, "ScaleY",  0.0f, 1.0f)
        animator1.setDuration(300)
        val animator2: Animator =
            ObjectAnimator.ofFloat(this, "alpha", 0.0f, 1.0f)
        animator2.setDuration(300)
        animatorSet.playTogether(animator0, animator1, animator2)
        animatorSet.start()
    }
}

fun View.setAnimationEffectBounceGone() {
    if (this.visibility == View.VISIBLE) {
        val animatorSet = AnimatorSet()
        val animator0: Animator =
            ObjectAnimator.ofFloat(this, "ScaleX", 1.0f, 0.0f)
        animator0.setDuration(300)
        val animator1: Animator =
            ObjectAnimator.ofFloat(this, "ScaleY", 1.0f, 0.0f)
        animator1.setDuration(300)
        val animator2: Animator =
            ObjectAnimator.ofFloat(this, "alpha", 1.0f, 0.0f)
        animator2.setDuration(300)
        animatorSet.playTogether(animator0, animator1, animator2)
        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                visibility = View.GONE
            }
        })
        animatorSet.start()
    }
}

fun EditText.limitLength(maxLength: Int) {
    filters = arrayOf(InputFilter.LengthFilter(maxLength))
}

fun View?.findSuitableParent(): ViewGroup? {
    var view = this
    var fallback: ViewGroup? = null
    do {
        if (view is CoordinatorLayout) {
            return view
        } else if (view is FrameLayout) {
            if (view.id == android.R.id.content) {
                return view
            } else {
                fallback = view
            }
        }

        if (view != null) {
            view = if (view.parent is View) view.parent as View else null
        }
    } while (view != null)

    return fallback
}
