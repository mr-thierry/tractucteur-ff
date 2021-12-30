package tff.android.scan.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import tff.android.scan.vision.TextHolder


class Overlay(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val textPaint = TextPaint()
    private var texts = emptyList<TextHolder>()
    private val reticleRect = RectF()

    init {
        setLayerType(LAYER_TYPE_HARDWARE, null)

        textPaint.color = Color.WHITE
        textPaint.textSize = 64F
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        buildReticleBox(measuredWidth, measuredHeight, reticleRect)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        drawTexts(canvas)
    }

    private fun drawTexts(canvas: Canvas) {
        synchronized(texts) {
            texts.forEach {
                canvas.drawText(it.translated, reticleRect.left + it.boundingBox.left.toFloat(), reticleRect.top + it.boundingBox.bottom.toFloat(), textPaint)
            }
        }
    }

    fun setTexts(newTexts: List<TextHolder>) {
        synchronized(texts) {
            texts = newTexts
        }
        invalidate()
    }
}

