package com.pierce.shadowview

import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.*
import android.graphics.Paint.Style
import android.graphics.Path.FillType
import android.graphics.Shader.TileMode
import android.graphics.drawable.Drawable
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.max


internal class RoundRectDrawableWithShadow(resources: Resources, backgroundColor: ColorStateList, radius: Float, shadowSize: Float, maxShadowSize: Float, shadowColorStart: ColorStateList?, shadowColorEnd: ColorStateList?) : Drawable() {
    private val mInsetShadow: Int
    private val mPaint: Paint
    private val mCornerShadowPaint: Paint
    private val mEdgeShadowPaint: Paint
    private val mContentBounds: RectF
    private var mCornerRadius: Float = 0.toFloat()
    private var mCornerShadowPath: Path? = null
    private var mRawMaxShadowSize: Float = 0.toFloat()
    private var mShadowSize: Float = 0.toFloat()
    private var mRawShadowSize: Float = 0.toFloat()
    private var mBackground: ColorStateList? = null
    private var mDirty = true
    private val mShadowStartColor: ColorStateList
    private val mShadowEndColor: ColorStateList
    private var mAddPaddingForCorners = false//cardView源码中，是为true，在21以下，会往阴影内部偏离一个padding
    private var mPrintedShadowClipWarning = false

    var cornerRadius: Float
        get() = this.mCornerRadius
        set(radius) {
            var radius = radius
            if (radius < 0.0f) {
                throw IllegalArgumentException("Invalid radius $radius. Must be >= 0")
            } else {
                radius = (radius + 0.5f).toInt().toFloat()
                if (this.mCornerRadius != radius) {
                    this.mCornerRadius = radius
                    this.mDirty = true
                    this.invalidateSelf()
                }
            }
        }

    var shadowSize: Float
        get() = this.mRawShadowSize
        set(size) = this.setShadowSize(size, this.mRawMaxShadowSize)

    var maxShadowSize: Float
        get() = this.mRawMaxShadowSize
        set(size) = this.setShadowSize(this.mRawShadowSize, size)

    val minWidth: Float
        get() {
            val content = 2.0f * max(this.mRawMaxShadowSize, this.mCornerRadius + this.mInsetShadow.toFloat() + this.mRawMaxShadowSize / 2.0f)
            return content + (this.mRawMaxShadowSize + this.mInsetShadow.toFloat()) * 2.0f
        }

    val minHeight: Float
        get() {
            val content = 2.0f * max(this.mRawMaxShadowSize, this.mCornerRadius + this.mInsetShadow.toFloat() + this.mRawMaxShadowSize * 1.5f / 2.0f)
            return content + (this.mRawMaxShadowSize * 1.5f + this.mInsetShadow.toFloat()) * 2.0f
        }

    var color: ColorStateList?
        get() = this.mBackground
        set(color) {
            this.setBackground(color)
            this.invalidateSelf()
        }

    init {
        // 如果没有设置阴影颜色，使用默认颜色
        if (shadowColorStart == null) {
            mShadowStartColor = ColorStateList.valueOf(resources.getColor(R.color.shadowView_default_shadow_start_color))
        } else {
            mShadowStartColor = shadowColorStart
        }
        if (shadowColorEnd == null) {
            mShadowEndColor = ColorStateList.valueOf(resources.getColor(R.color.shadowView_default_shadow_end_color))
        } else {
            mShadowEndColor = shadowColorEnd
        }
        this.mInsetShadow = resources.getDimensionPixelSize(R.dimen.shadowView_compat_inset_shadow)
        this.mPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG)
        this.setBackground(backgroundColor)
        this.mCornerShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG)
        this.mCornerShadowPaint.style = Style.FILL
        this.mCornerRadius = (radius + 0.5f).toInt().toFloat()
        this.mContentBounds = RectF()
        this.mEdgeShadowPaint = Paint(this.mCornerShadowPaint)
        this.mEdgeShadowPaint.isAntiAlias = false
        this.setShadowSize(shadowSize, maxShadowSize)
    }

    private fun setBackground(color: ColorStateList?) {
        this.mBackground = color ?: ColorStateList.valueOf(Color.TRANSPARENT)
        this.mPaint.color = this.mBackground!!.getColorForState(this.state, this.mBackground!!.defaultColor)
    }

    private fun toEven(value: Float): Int {
        val i = (value + 0.5f).toInt()
        return if (i % 2 == 1) i - 1 else i
    }

    fun setAddPaddingForCorners(addPaddingForCorners: Boolean) {
        this.mAddPaddingForCorners = addPaddingForCorners
        this.invalidateSelf()
    }

    override fun setAlpha(alpha: Int) {
        this.mPaint.alpha = alpha
        this.mCornerShadowPaint.alpha = alpha
        this.mEdgeShadowPaint.alpha = alpha
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        this.mDirty = true
    }

    private fun setShadowSize(shadowSize: Float, maxShadowSize: Float) {
        var shadowSize = shadowSize
        var maxShadowSize = maxShadowSize
        if (shadowSize < 0.0f) {
            throw IllegalArgumentException("Invalid shadow size $shadowSize. Must be >= 0")
        } else if (maxShadowSize < 0.0f) {
            throw IllegalArgumentException("Invalid max shadow size $maxShadowSize. Must be >= 0")
        } else {
            shadowSize = this.toEven(shadowSize).toFloat()
            maxShadowSize = this.toEven(maxShadowSize).toFloat()
            if (shadowSize > maxShadowSize) {
                shadowSize = maxShadowSize
                if (!this.mPrintedShadowClipWarning) {
                    this.mPrintedShadowClipWarning = true
                }
            }

            if (this.mRawShadowSize != shadowSize || this.mRawMaxShadowSize != maxShadowSize) {
                this.mRawShadowSize = shadowSize
                this.mRawMaxShadowSize = maxShadowSize
                this.mShadowSize = (shadowSize * 1.5f + this.mInsetShadow.toFloat() + 0.5f).toInt().toFloat()
                this.mDirty = true
                this.invalidateSelf()
            }
        }
    }

    override fun getPadding(padding: Rect): Boolean {
        val vOffset = ceil(calculateVerticalPadding(this.mRawMaxShadowSize, this.mCornerRadius, this.mAddPaddingForCorners).toDouble()).toInt()
        val hOffset = ceil(calculateHorizontalPadding(this.mRawMaxShadowSize, this.mCornerRadius, this.mAddPaddingForCorners).toDouble()).toInt()
        padding.set(hOffset, vOffset, hOffset, vOffset)
        return true
    }

    override fun onStateChange(stateSet: IntArray): Boolean {
        val newColor = this.mBackground!!.getColorForState(stateSet, this.mBackground!!.defaultColor)
        if (this.mPaint.color == newColor) {
            return false
        } else {
            this.mPaint.color = newColor
            this.mDirty = true
            this.invalidateSelf()
            return true
        }
    }

    override fun isStateful(): Boolean {
        return this.mBackground != null && this.mBackground!!.isStateful || super.isStateful()
    }

    override fun setColorFilter(cf: ColorFilter?) {
        this.mPaint.colorFilter = cf
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    override fun draw(canvas: Canvas) {
        if (this.mDirty) {
            this.buildComponents(this.bounds)
            this.mDirty = false
        }

        canvas.translate(0.0f, this.mRawShadowSize / 2.0f)
        this.drawShadow(canvas)
        canvas.translate(0.0f, -this.mRawShadowSize / 2.0f)
        sRoundRectHelper!!.drawRoundRect(canvas, this.mContentBounds, this.mCornerRadius, this.mPaint)
    }

    private fun drawShadow(canvas: Canvas) {
        val edgeShadowTop = -this.mCornerRadius - this.mShadowSize
        val inset = this.mCornerRadius + this.mInsetShadow.toFloat() + this.mRawShadowSize / 2.0f
        val drawHorizontalEdges = this.mContentBounds.width() - 2.0f * inset > 0.0f
        val drawVerticalEdges = this.mContentBounds.height() - 2.0f * inset > 0.0f
        var saved = canvas.save()
        canvas.translate(this.mContentBounds.left + inset, this.mContentBounds.top + inset)
        canvas.drawPath(this.mCornerShadowPath!!, this.mCornerShadowPaint)
        if (drawHorizontalEdges) {
            canvas.drawRect(0.0f, edgeShadowTop, this.mContentBounds.width() - 2.0f * inset, -this.mCornerRadius, this.mEdgeShadowPaint)
        }

        canvas.restoreToCount(saved)
        saved = canvas.save()
        canvas.translate(this.mContentBounds.right - inset, this.mContentBounds.bottom - inset)
        canvas.rotate(180.0f)
        canvas.drawPath(this.mCornerShadowPath!!, this.mCornerShadowPaint)
        if (drawHorizontalEdges) {
            canvas.drawRect(0.0f, edgeShadowTop, this.mContentBounds.width() - 2.0f * inset, -this.mCornerRadius + this.mShadowSize, this.mEdgeShadowPaint)
        }

        canvas.restoreToCount(saved)
        saved = canvas.save()
        canvas.translate(this.mContentBounds.left + inset, this.mContentBounds.bottom - inset)
        canvas.rotate(270.0f)
        canvas.drawPath(this.mCornerShadowPath!!, this.mCornerShadowPaint)
        if (drawVerticalEdges) {
            canvas.drawRect(0.0f, edgeShadowTop, this.mContentBounds.height() - 2.0f * inset, -this.mCornerRadius, this.mEdgeShadowPaint)
        }

        canvas.restoreToCount(saved)
        saved = canvas.save()
        canvas.translate(this.mContentBounds.right - inset, this.mContentBounds.top + inset)
        canvas.rotate(90.0f)
        canvas.drawPath(this.mCornerShadowPath!!, this.mCornerShadowPaint)
        if (drawVerticalEdges) {
            canvas.drawRect(0.0f, edgeShadowTop, this.mContentBounds.height() - 2.0f * inset, -this.mCornerRadius, this.mEdgeShadowPaint)
        }

        canvas.restoreToCount(saved)
    }

    private fun buildShadowCorners() {
        val innerBounds = RectF(-this.mCornerRadius, -this.mCornerRadius, this.mCornerRadius, this.mCornerRadius)
        val outerBounds = RectF(innerBounds)
        outerBounds.inset(-this.mShadowSize, -this.mShadowSize)
        if (this.mCornerShadowPath == null) {
            this.mCornerShadowPath = Path()
        } else {
            this.mCornerShadowPath!!.reset()
        }

        this.mCornerShadowPath!!.fillType = FillType.EVEN_ODD
        this.mCornerShadowPath!!.moveTo(-this.mCornerRadius, 0.0f)
        this.mCornerShadowPath!!.rLineTo(-this.mShadowSize, 0.0f)
        this.mCornerShadowPath!!.arcTo(outerBounds, 180.0f, 90.0f, false)
        this.mCornerShadowPath!!.arcTo(innerBounds, 270.0f, -90.0f, false)
        this.mCornerShadowPath!!.close()
        val startRatio = this.mCornerRadius / (this.mCornerRadius + this.mShadowSize)

        // 获取当前状态下的阴影颜色
        val starColor = mShadowStartColor.getColorForState(state, mShadowStartColor.defaultColor)
        val endColor = mShadowEndColor.getColorForState(state, mShadowEndColor.defaultColor)
        this.mCornerShadowPaint.shader = RadialGradient(0.0f, 0.0f, this.mCornerRadius + this.mShadowSize, intArrayOf(starColor, starColor, endColor), floatArrayOf(0.0f, startRatio, 1.0f), TileMode.CLAMP)
        this.mEdgeShadowPaint.shader = LinearGradient(0.0f, -this.mCornerRadius + this.mShadowSize, 0.0f, -this.mCornerRadius - this.mShadowSize, intArrayOf(starColor, starColor, endColor), floatArrayOf(0.0f, 0.5f, 1.0f), TileMode.CLAMP)
        this.mEdgeShadowPaint.isAntiAlias = false
    }

    private fun buildComponents(bounds: Rect) {
        val verticalOffset = this.mRawMaxShadowSize * 1.5f
        this.mContentBounds.set(bounds.left.toFloat() + this.mRawMaxShadowSize, bounds.top.toFloat() + verticalOffset, bounds.right.toFloat() - this.mRawMaxShadowSize, bounds.bottom.toFloat() - verticalOffset)
        this.buildShadowCorners()
    }

    fun getMaxShadowAndCornerPadding(into: Rect) {
        this.getPadding(into)
    }

    internal interface RoundRectHelper {
        fun drawRoundRect(canvas: Canvas, bounds: RectF, cornerRadius: Float, paint: Paint)
    }

    companion object {
        private val COS_45 = cos(Math.toRadians(45.0))
        private const val SHADOW_MULTIPLIER = 1.5f
        var sRoundRectHelper: RoundRectHelper? = null

        fun calculateVerticalPadding(maxShadowSize: Float, cornerRadius: Float, addPaddingForCorners: Boolean): Float {
            return if (addPaddingForCorners) ((maxShadowSize * 1.5f).toDouble() + (1.0 - COS_45) * cornerRadius.toDouble()).toFloat() else maxShadowSize * 1.5f
        }

        fun calculateHorizontalPadding(maxShadowSize: Float, cornerRadius: Float, addPaddingForCorners: Boolean): Float {
            return if (addPaddingForCorners) (maxShadowSize.toDouble() + (1.0 - COS_45) * cornerRadius.toDouble()).toFloat() else maxShadowSize
        }
    }
}
