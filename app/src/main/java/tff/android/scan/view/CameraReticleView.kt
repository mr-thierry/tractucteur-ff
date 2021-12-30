package tff.android.scan.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.Style.FILL
import android.graphics.Paint.Style.STROKE
import android.graphics.PorterDuff.Mode.CLEAR
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.annotation.DimenRes
import androidx.core.content.ContextCompat
import tff.android.R

class CameraReticleView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val scrimPaint: Paint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.reticlebg)
    }

    private val boxPaint: Paint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.reticlebox)
        style = STROKE
        strokeWidth = context.resources.getDimensionPixelOffset(R.dimen.reticlebox_strokewidth).toFloat()
    }

    private val eraserPaint: Paint = Paint().apply {
        strokeWidth = boxPaint.strokeWidth
        xfermode = PorterDuffXfermode(CLEAR)
    }

    private val boxCornerRadius: Float = context.dimenF(R.dimen.reticlebox_radius)
    private val boxRect = RectF()

    init {
        setLayerType(LAYER_TYPE_HARDWARE, null)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        buildReticleBox(measuredWidth, measuredHeight, boxRect)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        drawBackground(canvas)
        eraseCenterReticle(canvas)
        drawReticleBox(canvas)
    }

    private fun drawBackground(canvas: Canvas) {
        canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), scrimPaint)
    }

    private fun eraseCenterReticle(canvas: Canvas) {
        eraserPaint.style = FILL
        canvas.drawRoundRect(boxRect, boxCornerRadius, boxCornerRadius, eraserPaint)
        eraserPaint.style = STROKE
        canvas.drawRoundRect(boxRect, boxCornerRadius, boxCornerRadius, eraserPaint)
    }

    private fun drawReticleBox(canvas: Canvas) {
        canvas.drawRoundRect(boxRect, boxCornerRadius, boxCornerRadius, boxPaint)
    }
}

fun buildReticleBox(width: Int, height: Int, boxRect: RectF) {
    val overlayWidth = width.toFloat()
    val overlayHeight = height.toFloat()
    val boxWidth = overlayWidth * 80 / 100
    val boxHeight = overlayHeight * 35 / 100
    val centerX = overlayWidth / 2
    val centerY = overlayHeight / 2

    boxRect.left = centerX - boxWidth / 2
    boxRect.top = centerY - boxHeight / 2
    boxRect.right = centerX + boxWidth / 2
    boxRect.bottom = centerY + boxHeight / 2
}

fun Context.dimenF(@DimenRes id: Int): Float = resources.getDimension(id)