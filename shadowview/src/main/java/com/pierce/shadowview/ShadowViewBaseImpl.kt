package com.pierce.shadowview

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import kotlin.math.ceil

internal open class ShadowViewBaseImpl : ShadowViewImpl {
    val mCornerRect = RectF()

    override fun initStatic() {
        RoundRectDrawableWithShadow.sRoundRectHelper = object : RoundRectDrawableWithShadow.RoundRectHelper {
            override fun drawRoundRect(canvas: Canvas, bounds: RectF, cornerRadius: Float, paint: Paint) {
                val twoRadius = cornerRadius * 2.0f
                val innerWidth = bounds.width() - twoRadius - 1.0f
                val innerHeight = bounds.height() - twoRadius - 1.0f
                if (cornerRadius >= 1.0f) {
                    val roundedCornerRadius = cornerRadius + 0.5f
                    this@ShadowViewBaseImpl.mCornerRect.set(-roundedCornerRadius, -roundedCornerRadius, roundedCornerRadius, roundedCornerRadius)
                    val saved = canvas.save()
                    canvas.translate(bounds.left + roundedCornerRadius, bounds.top + roundedCornerRadius)
                    canvas.drawArc(this@ShadowViewBaseImpl.mCornerRect, 180.0f, 90.0f, true, paint)
                    canvas.translate(innerWidth, 0.0f)
                    canvas.rotate(90.0f)
                    canvas.drawArc(this@ShadowViewBaseImpl.mCornerRect, 180.0f, 90.0f, true, paint)
                    canvas.translate(innerHeight, 0.0f)
                    canvas.rotate(90.0f)
                    canvas.drawArc(this@ShadowViewBaseImpl.mCornerRect, 180.0f, 90.0f, true, paint)
                    canvas.translate(innerWidth, 0.0f)
                    canvas.rotate(90.0f)
                    canvas.drawArc(this@ShadowViewBaseImpl.mCornerRect, 180.0f, 90.0f, true, paint)
                    canvas.restoreToCount(saved)
                    canvas.drawRect(bounds.left + roundedCornerRadius - 1.0f, bounds.top, bounds.right - roundedCornerRadius + 1.0f, bounds.top + roundedCornerRadius, paint)
                    canvas.drawRect(bounds.left + roundedCornerRadius - 1.0f, bounds.bottom - roundedCornerRadius, bounds.right - roundedCornerRadius + 1.0f, bounds.bottom, paint)
                }
                canvas.drawRect(bounds.left, bounds.top + cornerRadius, bounds.right, bounds.bottom - cornerRadius, paint)
            }
        }
    }

    override fun initialize(shadowViewDelegate: ShadowViewDelegate, context: Context, backgroundColor: ColorStateList, radius: Float, elevation: Float, maxElevation: Float, shadowColorStart: ColorStateList?, shadowColorEnd: ColorStateList?) {
        val background = this.createBackground(context, backgroundColor, radius, elevation, maxElevation, shadowColorStart, shadowColorEnd)
        background.setAddPaddingForCorners(shadowViewDelegate.preventCornerOverlap)
        shadowViewDelegate.setContentBackground(background)
        this.updatePadding(shadowViewDelegate)
    }

    private fun createBackground(context: Context, backgroundColor: ColorStateList, radius: Float, elevation: Float, maxElevation: Float, shadowColorStart: ColorStateList?, shadowColorEnd: ColorStateList?): RoundRectDrawableWithShadow {
        return RoundRectDrawableWithShadow(context.resources, backgroundColor, radius, elevation, maxElevation, shadowColorStart, shadowColorEnd)
    }

    override fun updatePadding(shadowViewDelegate: ShadowViewDelegate) {
        val shadowPadding = Rect()
        this.getShadowBackground(shadowViewDelegate).getMaxShadowAndCornerPadding(shadowPadding)
        shadowViewDelegate.setMinWidthHeightInternal(ceil(this.getMinWidth(shadowViewDelegate).toDouble()).toInt(), ceil(this.getMinHeight(shadowViewDelegate).toDouble()).toInt())
        shadowViewDelegate.setShadowPadding(shadowPadding.left, shadowPadding.top, shadowPadding.right, shadowPadding.bottom)
    }

    override fun onCompatPaddingChanged(shadowViewDelegate: ShadowViewDelegate) {}

    override fun onPreventCornerOverlapChanged(shadowViewDelegate: ShadowViewDelegate) {
        this.getShadowBackground(shadowViewDelegate).setAddPaddingForCorners(shadowViewDelegate.preventCornerOverlap)
        this.updatePadding(shadowViewDelegate)
    }

    override fun setBackgroundColor(shadowViewDelegate: ShadowViewDelegate, color: ColorStateList?) {
        this.getShadowBackground(shadowViewDelegate).color = color
    }

    override fun getBackgroundColor(shadowViewDelegate: ShadowViewDelegate): ColorStateList? {
        return this.getShadowBackground(shadowViewDelegate).color
    }

    override fun setRadius(shadowViewDelegate: ShadowViewDelegate, radius: Float) {
        this.getShadowBackground(shadowViewDelegate).cornerRadius = radius
        this.updatePadding(shadowViewDelegate)
    }

    override fun getRadius(shadowViewDelegate: ShadowViewDelegate): Float {
        return this.getShadowBackground(shadowViewDelegate).cornerRadius
    }

    override fun setElevation(shadowViewDelegate: ShadowViewDelegate, elevation: Float) {
        this.getShadowBackground(shadowViewDelegate).shadowSize = elevation
    }

    override fun getElevation(shadowViewDelegate: ShadowViewDelegate): Float {
        return this.getShadowBackground(shadowViewDelegate).shadowSize
    }

    override fun setMaxElevation(shadowViewDelegate: ShadowViewDelegate, maxElevation: Float) {
        this.getShadowBackground(shadowViewDelegate).maxShadowSize = maxElevation
        this.updatePadding(shadowViewDelegate)
    }

    override fun getMaxElevation(shadowViewDelegate: ShadowViewDelegate): Float {
        return this.getShadowBackground(shadowViewDelegate).maxShadowSize
    }

    override fun getMinWidth(shadowViewDelegate: ShadowViewDelegate): Float {
        return this.getShadowBackground(shadowViewDelegate).minWidth
    }

    override fun getMinHeight(shadowViewDelegate: ShadowViewDelegate): Float {
        return this.getShadowBackground(shadowViewDelegate).minHeight
    }

    private fun getShadowBackground(shadowViewDelegate: ShadowViewDelegate): RoundRectDrawableWithShadow {
        return shadowViewDelegate.getContentBackground() as RoundRectDrawableWithShadow
    }
}
