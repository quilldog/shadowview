package com.pierce.shadowview

import android.graphics.drawable.Drawable
import android.view.View

internal interface ShadowViewDelegate {

    fun setContentBackground(drawable :Drawable)

    fun getContentBackground():Drawable?

    val useCompatPadding: Boolean

    val preventCornerOverlap: Boolean

    val shadowView: View

    fun setShadowPadding(left: Int, top: Int, right: Int, bottom: Int)

    fun setMinWidthHeightInternal(width: Int, height: Int)
}
