package com.pierce.shadowview

import android.content.Context
import android.content.res.ColorStateList

internal interface ShadowViewImpl {
    fun initialize(shadowViewDelegate: ShadowViewDelegate, context: Context, backgroundColor: ColorStateList, radius: Float, elevation: Float, maxElevation: Float, shadowColorStart: ColorStateList?, shadowColorEnd: ColorStateList?)

    fun setRadius(shadowViewDelegate: ShadowViewDelegate, radius: Float)

    fun getRadius(shadowViewDelegate: ShadowViewDelegate): Float

    fun setElevation(shadowViewDelegate: ShadowViewDelegate, elevation: Float)

    fun getElevation(shadowViewDelegate: ShadowViewDelegate): Float

    fun initStatic()

    fun setMaxElevation(shadowViewDelegate: ShadowViewDelegate, maxElevation: Float)

    fun getMaxElevation(shadowViewDelegate: ShadowViewDelegate): Float

    fun getMinWidth(shadowViewDelegate: ShadowViewDelegate): Float

    fun getMinHeight(shadowViewDelegate: ShadowViewDelegate): Float

    fun updatePadding(shadowViewDelegate: ShadowViewDelegate)

    fun onCompatPaddingChanged(shadowViewDelegate: ShadowViewDelegate)

    fun onPreventCornerOverlapChanged(shadowViewDelegate: ShadowViewDelegate)

    fun setBackgroundColor(shadowViewDelegate: ShadowViewDelegate, color: ColorStateList?)

    fun getBackgroundColor(shadowViewDelegate: ShadowViewDelegate): ColorStateList?
}
