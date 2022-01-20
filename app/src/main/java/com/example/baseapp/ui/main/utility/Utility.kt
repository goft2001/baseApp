@file:Suppress("DEPRECATION")

package com.example.baseapp.ui.main.utility

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.baseapp.databinding.CustomToastBinding
import com.facebook.stetho.common.LogUtil
import com.google.android.material.snackbar.Snackbar

object Utility {
    private const val TAG = "Utility"

    fun showSnackBar(view: View, msg: String?, duration: Int = Snackbar.LENGTH_SHORT) {
        if (!msg.isNullOrEmpty()) {
            try {
                Snackbar.make(view, "", duration).apply {
                    val snackbarView = this.view
                    (snackbarView as? Snackbar.SnackbarLayout)?.apply {
                        findViewById<TextView>(com.google.android.material.R.id.snackbar_text).visibility =
                            View.INVISIBLE
                        val binding = CustomToastBinding.inflate(
                            LayoutInflater.from(view.context),
                            null,
                            false
                        ).apply {
                            itemLl.visibility = View.VISIBLE
                            text.text = msg
                            text.gravity = Gravity.START or Gravity.CENTER_VERTICAL
                        }

                        setBackgroundColor(Color.TRANSPARENT)
                        addView(
                            binding.root,
                            ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        )
                    } ?: setText(msg)
                    show()
                }
            } catch (e: IllegalArgumentException) {
                LogUtil.e(TAG, "showSnackBar() : ${e.message}")
            }
        }
    }

    fun Fragment.hideKeyboard(_view: View? = null) {
        (_view ?: view)?.let { context?.hideKeyboard(it) }
    }

    fun Activity.hideKeyboard(view: View? = null) {
        (view ?: currentFocus)?.let { (this as Context).hideKeyboard(it) }
    }

    fun Context.showKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(view, 0)
    }

    fun Context.hideKeyboard(view: View? = null) {
        if (view == null)
            return

        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    // px -> Dp
    fun Int.pxToDp(context: Context): Int = this.toFloat().pxToDp(context).toInt()

    fun Float.pxToDp(context: Context): Float = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, this, context.resources.displayMetrics
    )

    fun startLayoutAnimation(
        context: Context,
        layout: View,
        isShow: Boolean,
        animRes: Int,
        onClose: (() -> Unit)? = null
    ) {
        var startLayoutAnim = false
        if (isShow != layout.isShown) {
            val anim = AnimationUtils.loadAnimation(context, animRes)
            anim.duration = if (isShow) 100L else 50L
            anim.interpolator = LinearInterpolator()
            anim.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {
                    startLayoutAnim = true
                    if (isShow) {
                        layout.visibility = View.VISIBLE
                    }
                }

                override fun onAnimationEnd(animation: Animation) {
                    startLayoutAnim = false
                    if (!isShow) {
                        layout.visibility = View.INVISIBLE // gone 처리 시 anim 재시작 후 layout 이 깜빡임.
                        onClose?.invoke()
                    }
                }

                override fun onAnimationRepeat(animation: Animation) {}
            })

            if (!startLayoutAnim) {
                layout.startAnimation(anim)
            }
        } else {
            onClose?.invoke()
        }
    }

    /***
     * Display
     */
    fun getStatusBarHeight(context: Context?): Int {
        if (context != null) {
            val resources = context.resources
            val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
            if (resourceId > 0) {
                return resources.getDimensionPixelSize(resourceId)
            }
        }
        return 0
    }
}
