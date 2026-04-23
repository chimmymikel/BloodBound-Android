// FILE: app/src/main/java/com/bloodbound/app/core/ui/GradientTextView.kt
package com.bloodbound.app.core.ui

import android.content.Context
import android.graphics.LinearGradient
import android.graphics.Shader
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

/**
 * TextView that renders text with a horizontal linear gradient.
 * Set gradientStartColor and gradientEndColor, then call applyGradient().
 * onSizeChanged auto-applies so it also works in XML layouts.
 */
class GradientTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AppCompatTextView(context, attrs, defStyle) {

    var gradientStartColor: Int = 0xFF2563EB.toInt()
    var gradientEndColor: Int   = 0xFF1E40AF.toInt()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0) applyGradient()
    }

    fun applyGradient() {
        if (width == 0) { post { applyGradient() }; return }
        paint.shader = LinearGradient(
            0f, 0f, width.toFloat(), 0f,
            intArrayOf(gradientStartColor, gradientEndColor),
            null,
            Shader.TileMode.CLAMP
        )
        invalidate()
    }
}