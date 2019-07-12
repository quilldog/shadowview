package com.pierce.shadowview

import android.content.Context
import android.content.res.ColorStateList
import android.support.annotation.RequiresApi

@RequiresApi(21)
internal class ShadowViewApi21Impl : ShadowViewApi17Impl() {

    /**
     * api21及以上，没有设置阴影颜色的话，我们用原生的实现方式
     */
    private var useLowerVersion = false

    override fun initialize(shadowViewDelegate: ShadowViewDelegate, context: Context, backgroundColor: ColorStateList, radius: Float, elevation: Float, maxElevation: Float, shadowColorStart: ColorStateList?, shadowColorEnd: ColorStateList?) {
        useLowerVersion = !(shadowColorStart == null && shadowColorEnd == null)
        if (useLowerVersion) {
            super.initialize(shadowViewDelegate, context, backgroundColor, radius, elevation, maxElevation, shadowColorStart, shadowColorEnd)
        } else {
            val background = RoundRectDrawable(backgroundColor, radius)
            shadowViewDelegate.setContentBackground(background)
            val view = shadowViewDelegate.shadowView
            view.clipToOutline = true
            view.elevation = elevation
            setMaxElevation(shadowViewDelegate, maxElevation)
        }
    }

    override fun setRadius(shadowViewDelegate: ShadowViewDelegate, radius: Float) {
        if (useLowerVersion) {
            super.setRadius(shadowViewDelegate, radius)
        } else {
            getContentBackground(shadowViewDelegate).radius = radius
        }
    }

    override fun setMaxElevation(shadowViewDelegate: ShadowViewDelegate, maxElevation: Float) {
        if (useLowerVersion) {
            super.setMaxElevation(shadowViewDelegate, maxElevation)
        } else {
            getContentBackground(shadowViewDelegate).setPadding(maxElevation,
                    shadowViewDelegate.useCompatPadding, shadowViewDelegate.preventCornerOverlap)
            updatePadding(shadowViewDelegate)
        }
    }

    override fun getMaxElevation(shadowViewDelegate: ShadowViewDelegate): Float {
        return if (useLowerVersion) {
            super.getMaxElevation(shadowViewDelegate)
        } else {
            getContentBackground(shadowViewDelegate).padding
        }
    }

    override fun getMinWidth(shadowViewDelegate: ShadowViewDelegate): Float {
        return if (useLowerVersion) {
            super.getMinWidth(shadowViewDelegate)
        } else {
            getRadius(shadowViewDelegate) * 2
        }
    }

    override fun getMinHeight(shadowViewDelegate: ShadowViewDelegate): Float {
        return if (useLowerVersion) {
            super.getMinHeight(shadowViewDelegate)
        } else {
            getRadius(shadowViewDelegate) * 2
        }
    }

    override fun getRadius(shadowViewDelegate: ShadowViewDelegate): Float {
        return if (useLowerVersion) {
            super.getRadius(shadowViewDelegate)
        } else {
            getContentBackground(shadowViewDelegate).radius
        }
    }

    override fun setElevation(shadowViewDelegate: ShadowViewDelegate, elevation: Float) {
        if (useLowerVersion) {
            super.setElevation(shadowViewDelegate, elevation)
        } else {
            shadowViewDelegate.shadowView.elevation = elevation
        }
    }

    override fun getElevation(shadowViewDelegate: ShadowViewDelegate): Float {
        return if (useLowerVersion) {
            super.getElevation(shadowViewDelegate)
        } else {
            shadowViewDelegate.shadowView.elevation
        }
    }

    override fun updatePadding(shadowViewDelegate: ShadowViewDelegate) {
        if (useLowerVersion) {
            super.updatePadding(shadowViewDelegate)
        } else {
            if (!shadowViewDelegate.useCompatPadding) {
                shadowViewDelegate.setShadowPadding(0, 0, 0, 0)
                return
            }
            val elevation = getMaxElevation(shadowViewDelegate)
            val radius = getRadius(shadowViewDelegate)
            val hPadding = Math.ceil(RoundRectDrawableWithShadow
                    .calculateHorizontalPadding(elevation, radius, shadowViewDelegate.preventCornerOverlap).toDouble()).toInt()
            val vPadding = Math.ceil(RoundRectDrawableWithShadow
                    .calculateVerticalPadding(elevation, radius, shadowViewDelegate.preventCornerOverlap).toDouble()).toInt()
            shadowViewDelegate.setShadowPadding(hPadding, vPadding, hPadding, vPadding)
        }
    }

    override fun onCompatPaddingChanged(shadowViewDelegate: ShadowViewDelegate) {
        if (useLowerVersion) {
            super.onCompatPaddingChanged(shadowViewDelegate)
        } else {
            setMaxElevation(shadowViewDelegate, getMaxElevation(shadowViewDelegate))
        }
    }

    override fun onPreventCornerOverlapChanged(shadowViewDelegate: ShadowViewDelegate) {
        if (useLowerVersion) {
            super.onPreventCornerOverlapChanged(shadowViewDelegate)
        } else {
            setMaxElevation(shadowViewDelegate, getMaxElevation(shadowViewDelegate))
        }
    }

    override fun setBackgroundColor(shadowViewDelegate: ShadowViewDelegate, color: ColorStateList?) {
        if (useLowerVersion) {
            super.setBackgroundColor(shadowViewDelegate, color)
        } else {
            getContentBackground(shadowViewDelegate).color = color
        }
    }

    override fun getBackgroundColor(shadowViewDelegate: ShadowViewDelegate): ColorStateList? {
        return if (useLowerVersion) {
            super.getBackgroundColor(shadowViewDelegate)
        } else {
            getContentBackground(shadowViewDelegate).color
        }
    }

    private fun getContentBackground(shadowViewDelegate: ShadowViewDelegate): RoundRectDrawable {
        return shadowViewDelegate.getContentBackground() as RoundRectDrawable
    }
}
