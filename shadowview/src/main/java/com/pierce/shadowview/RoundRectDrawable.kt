package com.pierce.shadowview

import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.PorterDuff.Mode
import android.graphics.drawable.Drawable
import android.support.annotation.RequiresApi
import kotlin.math.ceil

@RequiresApi(21)
internal class RoundRectDrawable(backgroundColor: ColorStateList, private var mRadius: Float) : Drawable() {
    private val mPaint: Paint
    private val mBoundsF: RectF
    private val mBoundsI: Rect
    var padding: Float = 0.toFloat()
        private set
    private var mInsetForPadding = false
    private var mInsetForRadius = true
    private var mBackground: ColorStateList? = null
    private var mTintFilter: PorterDuffColorFilter? = null
    private var mTint: ColorStateList? = null
    private var mTintMode: Mode? = null

    var radius: Float
        get() = this.mRadius
        set(radius) {
            if (radius != this.mRadius) {
                this.mRadius = radius
                this.updateBounds(null as Rect?)
                this.invalidateSelf()
            }
        }

    var color: ColorStateList?
        get() = this.mBackground
        set(color) {
            this.setBackground(color)
            this.invalidateSelf()
        }

    init {
        this.mTintMode = Mode.SRC_IN
        this.mPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG)
        this.setBackground(backgroundColor)
        this.mBoundsF = RectF()
        this.mBoundsI = Rect()
    }

    private fun setBackground(color: ColorStateList?) {
        this.mBackground = color ?: ColorStateList.valueOf(Color.TRANSPARENT)
        this.mPaint.color = this.mBackground!!.getColorForState(this.state, this.mBackground!!.defaultColor)
    }

    fun setPadding(padding: Float, insetForPadding: Boolean, insetForRadius: Boolean) {
        if (padding != this.padding || this.mInsetForPadding != insetForPadding || this.mInsetForRadius != insetForRadius) {
            this.padding = padding
            this.mInsetForPadding = insetForPadding
            this.mInsetForRadius = insetForRadius
            this.updateBounds(null as Rect?)
            this.invalidateSelf()
        }
    }

    override fun draw(canvas: Canvas) {
        val paint = this.mPaint
        val clearColorFilter: Boolean
        if (this.mTintFilter != null && paint.colorFilter == null) {
            paint.colorFilter = this.mTintFilter
            clearColorFilter = true
        } else {
            clearColorFilter = false
        }

        canvas.drawRoundRect(this.mBoundsF, this.mRadius, this.mRadius, paint)
        if (clearColorFilter) {
            paint.colorFilter = null
        }
    }

    private fun updateBounds(bounds: Rect?) {
        var bounds = bounds
        if (bounds == null) {
            bounds = this.bounds
        }

        this.mBoundsF.set(bounds!!.left.toFloat(), bounds.top.toFloat(), bounds.right.toFloat(), bounds.bottom.toFloat())
        this.mBoundsI.set(bounds)
        if (this.mInsetForPadding) {
            val vInset = RoundRectDrawableWithShadow.calculateVerticalPadding(this.padding, this.mRadius, this.mInsetForRadius)
            val hInset = RoundRectDrawableWithShadow.calculateHorizontalPadding(this.padding, this.mRadius, this.mInsetForRadius)
            this.mBoundsI.inset(ceil(hInset.toDouble()).toInt(), ceil(vInset.toDouble()).toInt())
            this.mBoundsF.set(this.mBoundsI)
        }

    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        this.updateBounds(bounds)
    }

    override fun getOutline(outline: Outline) {
        outline.setRoundRect(this.mBoundsI, this.mRadius)
    }

    override fun setAlpha(alpha: Int) {
        this.mPaint.alpha = alpha
    }

    override fun setColorFilter(cf: ColorFilter?) {
        this.mPaint.colorFilter = cf
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    override fun setTintList(tint: ColorStateList?) {
        this.mTint = tint
        this.mTintFilter = this.createTintFilter(this.mTint, this.mTintMode)
        this.invalidateSelf()
    }

    override fun setTintMode(tintMode: Mode) {
        this.mTintMode = tintMode
        this.mTintFilter = this.createTintFilter(this.mTint, this.mTintMode)
        this.invalidateSelf()
    }

    override fun onStateChange(stateSet: IntArray): Boolean {
        val newColor = this.mBackground!!.getColorForState(stateSet, this.mBackground!!.defaultColor)
        val colorChanged = newColor != this.mPaint.color
        if (colorChanged) {
            this.mPaint.color = newColor
        }

        if (this.mTint != null && this.mTintMode != null) {
            this.mTintFilter = this.createTintFilter(this.mTint, this.mTintMode)
            return true
        } else {
            return colorChanged
        }
    }

    override fun isStateful(): Boolean {
        return this.mTint != null && this.mTint!!.isStateful || this.mBackground != null && this.mBackground!!.isStateful || super.isStateful()
    }

    private fun createTintFilter(tint: ColorStateList?, tintMode: Mode?): PorterDuffColorFilter? {
        if (tint != null && tintMode != null) {
            val color = tint.getColorForState(this.state, Color.TRANSPARENT)
            return PorterDuffColorFilter(color, tintMode)
        } else {
            return null
        }
    }
}
