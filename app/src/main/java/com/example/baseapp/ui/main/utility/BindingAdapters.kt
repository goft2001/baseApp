package com.example.baseapp.ui.main.utility

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.InsetDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.os.Build
import android.text.style.*
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.bumptech.glide.Glide
import com.bumptech.glide.load.Key.CHARSET
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapResource
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.util.Util
import com.example.baseapp.ui.main.utility.Utility.pxToDp
import java.nio.ByteBuffer
import java.security.MessageDigest
import kotlin.math.roundToInt

object BindingAdapters {

    //bitmap 형변환
    abstract class BitmapTransformation :
        Transformation<Bitmap> {
        override fun transform(
            context: Context, resource: Resource<Bitmap?>, outWidth: Int, outHeight: Int,
        ): Resource<Bitmap?> {
            require(Util.isValidDimensions(outWidth, outHeight)) {
                ("Cannot apply transformation on width: "
                        + outWidth
                        + " or height: "
                        + outHeight
                        + " less than or equal to zero and not Target.SIZE_ORIGINAL")
            }
            val bitmapPool = Glide.get(context).bitmapPool
            val toTransform = resource.get()
            val targetWidth = if (outWidth == Target.SIZE_ORIGINAL) toTransform.width else outWidth
            val targetHeight =
                if (outHeight == Target.SIZE_ORIGINAL) toTransform.height else outHeight
            val transformed = transform(context, bitmapPool, toTransform, targetWidth, targetHeight)
            return if (toTransform == transformed) {
                resource
            } else {
                BitmapResource.obtain(transformed, bitmapPool)!!
            }
        }

        protected abstract fun transform(
            context: Context, pool: BitmapPool, toTransform: Bitmap, outWidth: Int, outHeight: Int,
        ): Bitmap
    }

    class MaskBitmapTransform(
        @DrawableRes
        private val maskId: Int,
    ) : BitmapTransformation() {
        companion object {
            private val ID: String = "kr.co.player.utility.BindingAdapters.MaskedImage"
            private val ID_BYTES: ByteArray = ID.toByteArray(CHARSET)

            private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

            init {
                paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            }
        }

        override fun equals(other: Any?): Boolean {
            if (other is MaskBitmapTransform) {
                return maskId == other.maskId
            }
            return false
        }

        override fun hashCode(): Int {
            return Util.hashCode(
                ID.hashCode(),
                Util.hashCode(maskId)
            )
        }

        override fun updateDiskCacheKey(messageDigest: MessageDigest) {
            messageDigest.update(
                ID_BYTES
            )

            val maskData = ByteBuffer.allocate(4).putInt(maskId).array()
            messageDigest.update(maskData)
        }

        override fun transform(
            context: Context,
            pool: BitmapPool,
            toTransform: Bitmap,
            outWidth: Int,
            outHeight: Int,
        ): Bitmap {
            val width = toTransform.width
            val height = toTransform.height

            val safeConfig =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && Bitmap.Config.RGBA_F16 == toTransform.config) {
                    Bitmap.Config.RGBA_F16
                } else {
                    Bitmap.Config.ARGB_8888
                }

            val result = pool.get(width, height, safeConfig)
            result.setHasAlpha(true)
            result.density = toTransform.density

            val mask: Drawable = ContextCompat.getDrawable(context, maskId)!!

            val canvas = Canvas(result)
            mask.setBounds(0, 0, width, height)
            mask.draw(canvas)

            canvas.drawBitmap(toTransform, 0f, 0f, paint)

            return result
        }
    }

    class LastNoDrawDividerItemDecoration(context: Context, orientation: Int) :
        ItemDecoration() {
        /**
         * @return the [Drawable] for this divider.
         */
        var drawable: Drawable?
            private set

        /**
         * Current orientation. Either [.HORIZONTAL] or [.VERTICAL].
         */
        private var mOrientation = 0
        private val mBounds = Rect()

        /**
         * Sets the orientation for this divider. This should be called if
         * [RecyclerView.LayoutManager] changes orientation.
         *
         * @param orientation [.HORIZONTAL] or [.VERTICAL]
         */
        fun setOrientation(orientation: Int) {
            require(!(orientation != HORIZONTAL && orientation != VERTICAL)) { "Invalid orientation. It should be either HORIZONTAL or VERTICAL" }
            mOrientation = orientation
        }

        /**
         * Sets the [Drawable] for this divider.
         *
         * @param drawable Drawable that should be used as a divider.
         */
        fun setDrawable(drawable: Drawable) {
            this.drawable = drawable
        }

        override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
            val d = drawable
            val layoutManager = parent.layoutManager
            if (layoutManager == null || d == null) {
                return
            }
            if (mOrientation == VERTICAL) {
                drawVertical(c, parent, d)
            } else {
                drawHorizontal(c, parent, d)
            }
        }

        private fun drawVertical(canvas: Canvas, parent: RecyclerView, drawable: Drawable) {
            canvas.save()
            val left: Int
            val right: Int
            if (parent.clipToPadding) {
                left = parent.paddingLeft
                right = parent.width - parent.paddingRight
                canvas.clipRect(
                    left, parent.paddingTop, right,
                    parent.height - parent.paddingBottom
                )
            } else {
                left = 0
                right = parent.width
            }
            val childCount = parent.childCount
            for (i in 0 until childCount - 1) {
                val child = parent.getChildAt(i)
                parent.getDecoratedBoundsWithMargins(child, mBounds)
                val bottom = mBounds.bottom + child.translationY.roundToInt()
                val top = bottom - drawable.intrinsicHeight
                drawable.setBounds(left, top, right, bottom)
                drawable.draw(canvas)
            }
            canvas.restore()
        }

        private fun drawHorizontal(canvas: Canvas, parent: RecyclerView, drawable: Drawable) {
            canvas.save()
            val top: Int
            val bottom: Int
            if (parent.clipToPadding) {
                top = parent.paddingTop
                bottom = parent.height - parent.paddingBottom
                canvas.clipRect(
                    parent.paddingLeft, top,
                    parent.width - parent.paddingRight, bottom
                )
            } else {
                top = 0
                bottom = parent.height
            }
            val childCount = parent.childCount
            for (i in 0 until childCount - 1) {
                val child = parent.getChildAt(i)
                parent.getDecoratedBoundsWithMargins(child, mBounds)
                val right = mBounds.right + child.translationX.roundToInt()
                val left = right - drawable.intrinsicWidth
                drawable.setBounds(left, top, right, bottom)
                drawable.draw(canvas)
            }
            canvas.restore()
        }

        override fun getItemOffsets(
            outRect: Rect, view: View, parent: RecyclerView,
            state: RecyclerView.State,
        ) {
            val itemPosition = parent.getChildAdapterPosition(view)
            if (drawable == null || itemPosition == state.itemCount - 1) {
                outRect.setEmpty()
                return
            }

            if (mOrientation == VERTICAL) {
                outRect.set(0, 0, 0, drawable!!.intrinsicHeight)
            } else {
                outRect.set(0, 0, drawable!!.intrinsicWidth, 0)
            }
        }

        companion object {
            const val HORIZONTAL = LinearLayout.HORIZONTAL
            const val VERTICAL = LinearLayout.VERTICAL
            private const val TAG = "DividerItem"
            private val ATTRS = intArrayOf(android.R.attr.listDivider)
        }

        /**
         * Creates a divider [RecyclerView.ItemDecoration] that can be used with a
         * [LinearLayoutManager].
         *
         * @param context Current context, it will be used to access resources.
         * @param orientation Divider orientation. Should be [.HORIZONTAL] or [.VERTICAL].
         */
        init {
            val a = context.obtainStyledAttributes(ATTRS)
            drawable = a.getDrawable(0)
            if (drawable == null) {
                Log.w(
                    TAG, "@android:attr/listDivider was not set in the theme used for this "
                            + "DividerItemDecoration. Please set that attribute all call setDrawable()"
                )
            }
            a.recycle()
            setOrientation(orientation)
        }
    }

    @BindingAdapter(
        value = ["dividerW", "dividerH", "dividerLastDraw", "dividerPadding", "dividerPaddingWidth", "dividerPaddingHeight", "dividerPaddingRigth", "dividerPaddingLeft", "dividerPaddingTop", "dividerPaddingBottom", "dividerColor"],
        requireAll = false
    )
    @JvmStatic
    fun RecyclerView.setDivider(
        dividerWidth: Float = 0f,
        dividerHeight: Float = 0f,
        dividerLastDraw: Boolean = false,
        dividerPadding: Float? = null,
        dividerPaddingWidth: Float? = null,
        dividerPaddingHeight: Float? = null,
        dividerPaddingRight: Float? = null,
        dividerPaddingLeft: Float? = null,
        dividerPaddingTop: Float? = null,
        dividerPaddingBottom: Float? = null,
        @ColorRes colorResId: Int? = null,
    ) {
        val layoutManager = layoutManager as? LinearLayoutManager ?: return

        val paddingWidth =
            dividerPadding ?: dividerPaddingWidth
        if (paddingWidth == null) {
            dividerPaddingWidth?.div(2)
        }
        val paddingHeight =
            dividerPadding ?: dividerPaddingHeight
        if (paddingHeight == null) {
            dividerPaddingHeight?.div(2)
        }

        val paddingRight =
            (paddingWidth ?: dividerPaddingRight ?: 0f).pxToDp(context).toInt()
        val paddingLeft =
            (paddingWidth ?: dividerPaddingLeft ?: 0f).pxToDp(context).toInt()
        val paddingTop =
            (paddingHeight ?: dividerPaddingTop ?: 0f).pxToDp(context).toInt()
        val paddingBottom =
            (paddingHeight ?: dividerPaddingBottom ?: 0f).pxToDp(context).toInt()

        val drawable =
            InsetDrawable(ShapeDrawable(RectShape().apply {
                resize(
                    dividerWidth.pxToDp(context),
                    dividerHeight.pxToDp(context)
                )
            }).apply {
                paint.color = if (colorResId != null) {
                    ContextCompat.getColor(context, colorResId)
                } else {
                    Color.TRANSPARENT
                }

                intrinsicWidth = (shape.width).toInt()
                intrinsicHeight = (shape.height).toInt()
            }, paddingLeft, paddingTop, paddingRight, paddingBottom)

        val decoration = if (!dividerLastDraw) {
            LastNoDrawDividerItemDecoration(
                context,
                layoutManager.orientation
            ).apply {
                setDrawable(drawable)
            }
        } else {
            DividerItemDecoration(
                context,
                layoutManager.orientation
            ).apply {
                setDrawable(drawable)
            }
        }

        addItemDecoration(
            decoration
        )
    }
}
