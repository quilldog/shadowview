package com.pierce.shadowview

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.support.annotation.RequiresApi

@RequiresApi(17)
internal open class ShadowViewApi17Impl : ShadowViewBaseImpl() {

    override fun initStatic() {
        RoundRectDrawableWithShadow.sRoundRectHelper = object :RoundRectDrawableWithShadow.RoundRectHelper{
            override fun drawRoundRect(canvas: Canvas, bounds: RectF, cornerRadius: Float, paint: Paint) {
                canvas.drawRoundRect(bounds, cornerRadius, cornerRadius, paint)
            }
        }
    }
}
