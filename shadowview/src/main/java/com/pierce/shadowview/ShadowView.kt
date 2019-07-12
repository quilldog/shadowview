package com.pierce.shadowview

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build.VERSION
import android.support.annotation.ColorInt
import android.support.annotation.Px
import android.util.AttributeSet
import android.view.View
import android.view.View.MeasureSpec.*
import android.widget.FrameLayout
import kotlin.math.ceil

class ShadowView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = R.attr.shadowViewStyle) : FrameLayout(context, attrs, defStyleAttr) {
    private var mCompatPadding: Boolean = false
    private var mPreventCornerOverlap: Boolean = false//cardView源码中，是为true，在21以下，会往阴影内部偏离一个padding
    internal var mUserSetMinWidth: Int = 0
    internal var mUserSetMinHeight: Int = 0
    internal val mContentPadding: Rect
    internal val mShadowBounds: Rect
    private val mShadowViewDelegate: ShadowViewDelegate

    var useCompatPadding: Boolean
        get() = this.mCompatPadding
        set(useCompatPadding) {
            if (this.mCompatPadding != useCompatPadding) {
                this.mCompatPadding = useCompatPadding
                IMPL.onCompatPaddingChanged(this.mShadowViewDelegate)
            }
        }

    val contentBackgroundColor: ColorStateList?
        get() = IMPL.getBackgroundColor(this.mShadowViewDelegate)

    val contentPaddingLeft: Int
        @Px
        get() = this.mContentPadding.left

    val contentPaddingRight: Int
        @Px
        get() = this.mContentPadding.right

    val contentPaddingTop: Int
        @Px
        get() = this.mContentPadding.top

    val contentPaddingBottom: Int
        @Px
        get() = this.mContentPadding.bottom

    var radius: Float
        get() = IMPL.getRadius(this.mShadowViewDelegate)
        set(radius) = IMPL.setRadius(this.mShadowViewDelegate, radius)

    var contentElevation: Float
        get() = IMPL.getElevation(this.mShadowViewDelegate)
        set(elevation) = IMPL.setElevation(this.mShadowViewDelegate, elevation)

    var maxContentElevation: Float
        get() = IMPL.getMaxElevation(this.mShadowViewDelegate)
        set(maxElevation) = IMPL.setMaxElevation(this.mShadowViewDelegate, maxElevation)

    var preventCornerOverlap: Boolean
        get() = this.mPreventCornerOverlap
        set(preventCornerOverlap) {
            if (preventCornerOverlap != this.mPreventCornerOverlap) {
                this.mPreventCornerOverlap = preventCornerOverlap
                IMPL.onPreventCornerOverlapChanged(this.mShadowViewDelegate)
            }

        }

    init {
        this.mContentPadding = Rect()
        this.mShadowBounds = Rect()
        this.mShadowViewDelegate = object : ShadowViewDelegate {
            private var drawable: Drawable? = null
            override fun setContentBackground(drawable: Drawable) {
                this.drawable = drawable
                this@ShadowView.background = drawable
            }

            override fun getContentBackground(): Drawable? = drawable

            override val useCompatPadding: Boolean
                get() = this@ShadowView.useCompatPadding

            override val preventCornerOverlap: Boolean
                get() = this@ShadowView.preventCornerOverlap

            override val shadowView: View
                get() = this@ShadowView

            override fun setShadowPadding(left: Int, top: Int, right: Int, bottom: Int) {
                this@ShadowView.mShadowBounds.set(left, top, right, bottom)
                super@ShadowView.setPadding(left + this@ShadowView.mContentPadding.left, top + this@ShadowView.mContentPadding.top, right + this@ShadowView.mContentPadding.right, bottom + this@ShadowView.mContentPadding.bottom)
            }

            override fun setMinWidthHeightInternal(width: Int, height: Int) {
                if (width > this@ShadowView.mUserSetMinWidth) {
                    super@ShadowView.setMinimumWidth(width)
                }

                if (height > this@ShadowView.mUserSetMinHeight) {
                    super@ShadowView.setMinimumHeight(height)
                }
            }
        }


        val a = context.obtainStyledAttributes(attrs, R.styleable.ShadowView, defStyleAttr, R.style.ShadowView)
        val backgroundColor: ColorStateList?
        if (a.hasValue(R.styleable.ShadowView_contentBackgroundColor)) {
            backgroundColor = a.getColorStateList(R.styleable.ShadowView_contentBackgroundColor)
        } else {
            val aa = this.context.obtainStyledAttributes(COLOR_BACKGROUND_ATTR)
            val themeColorBackground = aa.getColor(0, 0)
            aa.recycle()
            val hsv = FloatArray(3)
            Color.colorToHSV(themeColorBackground, hsv)
            backgroundColor = ColorStateList.valueOf(if (hsv[2] > 0.5f) this.resources.getColor(R.color.shadowView_light_background) else this.resources.getColor(R.color.shadowView_dark_background))
        }

        val shadowColorStart = a.getColorStateList(R.styleable.ShadowView_contentShadowColorStart)
        val shadowColorEnd = a.getColorStateList(R.styleable.ShadowView_contentShadowColorEnd)


        val radius = a.getDimension(R.styleable.ShadowView_contentCornerRadius, 0.0f)
        val elevation = a.getDimension(R.styleable.ShadowView_contentElevation, 0.0f)
        var maxElevation = a.getDimension(R.styleable.ShadowView_contentMaxElevation, 0.0f)
        this.mCompatPadding = a.getBoolean(R.styleable.ShadowView_contentUseCompatPadding, false)
        this.mPreventCornerOverlap = a.getBoolean(R.styleable.ShadowView_contentPreventCornerOverlap, false)
        val defaultPadding = a.getDimensionPixelSize(R.styleable.ShadowView_contentPadding, 0)
        this.mContentPadding.left = a.getDimensionPixelSize(R.styleable.ShadowView_contentPaddingLeft, defaultPadding)
        this.mContentPadding.top = a.getDimensionPixelSize(R.styleable.ShadowView_contentPaddingTop, defaultPadding)
        this.mContentPadding.right = a.getDimensionPixelSize(R.styleable.ShadowView_contentPaddingRight, defaultPadding)
        this.mContentPadding.bottom = a.getDimensionPixelSize(R.styleable.ShadowView_contentPaddingBottom, defaultPadding)
        if (elevation > maxElevation) {
            maxElevation = elevation
        }

        this.mUserSetMinWidth = a.getDimensionPixelSize(R.styleable.ShadowView_android_minWidth, 0)
        this.mUserSetMinHeight = a.getDimensionPixelSize(R.styleable.ShadowView_android_minHeight, 0)
        a.recycle()
        IMPL.initialize(this.mShadowViewDelegate, context, backgroundColor!!, radius, elevation, maxElevation, shadowColorStart, shadowColorEnd)
    }

    override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {}

    override fun setPaddingRelative(start: Int, top: Int, end: Int, bottom: Int) {}

    fun setContentPadding(@Px left: Int, @Px top: Int, @Px right: Int, @Px bottom: Int) {
        this.mContentPadding.set(left, top, right, bottom)
        IMPL.updatePadding(this.mShadowViewDelegate)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var widthMeasureSpec = widthMeasureSpec
        var heightMeasureSpec = heightMeasureSpec
        if (IMPL !is ShadowViewApi21Impl) {
            val widthMode = View.MeasureSpec.getMode(widthMeasureSpec)
            var heightMode: Int
            when (widthMode) {
                EXACTLY, AT_MOST -> {
                    heightMode = ceil(IMPL.getMinWidth(this.mShadowViewDelegate).toDouble()).toInt()
                    widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(Math.max(heightMode, View.MeasureSpec.getSize(widthMeasureSpec)), widthMode)
                    heightMode = View.MeasureSpec.getMode(heightMeasureSpec)
                    when (heightMode) {
                        EXACTLY, AT_MOST -> {
                            val minHeight = ceil(IMPL.getMinHeight(this.mShadowViewDelegate).toDouble()).toInt()
                            heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(Math.max(minHeight, View.MeasureSpec.getSize(heightMeasureSpec)), heightMode)
                            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
                        }
                        UNSPECIFIED -> super.onMeasure(widthMeasureSpec, heightMeasureSpec)
                        else -> super.onMeasure(widthMeasureSpec, heightMeasureSpec)
                    }
                }
                UNSPECIFIED -> {
                    heightMode = View.MeasureSpec.getMode(heightMeasureSpec)
                    when (heightMode) {
                        EXACTLY, AT_MOST -> {
                            val minHeight = ceil(IMPL.getMinHeight(this.mShadowViewDelegate).toDouble()).toInt()
                            heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(Math.max(minHeight, View.MeasureSpec.getSize(heightMeasureSpec)), heightMode)
                            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
                        }
                        UNSPECIFIED -> super.onMeasure(widthMeasureSpec, heightMeasureSpec)
                        else -> super.onMeasure(widthMeasureSpec, heightMeasureSpec)
                    }
                }
                else -> {
                    heightMode = View.MeasureSpec.getMode(heightMeasureSpec)
                    when (heightMode) {
                        EXACTLY, AT_MOST -> {
                            val minHeight = Math.ceil(IMPL.getMinHeight(this.mShadowViewDelegate).toDouble()).toInt()
                            heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(Math.max(minHeight, View.MeasureSpec.getSize(heightMeasureSpec)), heightMode)
                            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
                        }
                        UNSPECIFIED -> super.onMeasure(widthMeasureSpec, heightMeasureSpec)
                        else -> super.onMeasure(widthMeasureSpec, heightMeasureSpec)
                    }
                }
            }
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }

    }

    override fun setMinimumWidth(minWidth: Int) {
        this.mUserSetMinWidth = minWidth
        super.setMinimumWidth(minWidth)
    }

    override fun setMinimumHeight(minHeight: Int) {
        this.mUserSetMinHeight = minHeight
        super.setMinimumHeight(minHeight)
    }

    fun setContentBackgroundColor(@ColorInt color: Int) {
        IMPL.setBackgroundColor(this.mShadowViewDelegate, ColorStateList.valueOf(color))
    }

    fun setContentBackgroundColor(color: ColorStateList?) {
        IMPL.setBackgroundColor(this.mShadowViewDelegate, color)
    }

    companion object {
        private val COLOR_BACKGROUND_ATTR = intArrayOf(android.R.attr.colorBackground)
        private val IMPL: ShadowViewImpl

        init {
            when {
                VERSION.SDK_INT >= 21 -> IMPL = ShadowViewApi21Impl()
                VERSION.SDK_INT >= 17 -> IMPL = ShadowViewApi17Impl()
                else -> IMPL = ShadowViewBaseImpl()
            }

            IMPL.initStatic()
        }
    }
}
