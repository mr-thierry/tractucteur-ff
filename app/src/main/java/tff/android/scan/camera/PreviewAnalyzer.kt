package tff.android.scan.camera

import android.graphics.*
import android.media.Image
import android.view.TextureView
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.android.gms.vision.Frame.*
import tff.android.MainActivity
import tff.android.scan.view.CameraReticleView
import tff.android.scan.view.buildReticleBox
import tff.android.scan.vision.FrameProcessor
import java.io.ByteArrayOutputStream

@Suppress("ConstantConditionIf", "unused")
class PreviewAnalyzer(
    private val frameProcessor: FrameProcessor,
    private val activity: MainActivity
) : ImageAnalysis.Analyzer {

    private var textureView: TextureView? = null
    private val reticleBox = RectF()
    private lateinit var cameraReticleView: CameraReticleView

    override fun analyze(imageProxy: ImageProxy) {
        if (frameProcessor.processingFrame) {
            //FrameProcessor is currently processing a previous frame. So ignore this frame (drop it)
        } else {
            if (textureView == null) {
                textureView = activity.binding.scanCamerapreview.getChildAt(0) as TextureView
                cameraReticleView = activity.binding.scanReticle
            }

            textureView?.let {
                val textureViewBitmap = it.getBitmap(it.height, it.width) //I have no idea why the TextureView width/height are reverse
                if (textureViewBitmap != null) {
                    buildReticleBox(textureViewBitmap.width, textureViewBitmap.height, reticleBox)
                    val croppedBitmapBmp = Bitmap.createBitmap(textureViewBitmap, reticleBox.left.toInt(), reticleBox.top.toInt(), reticleBox.width().toInt(), reticleBox.height().toInt())
                    frameProcessor.processNewImage(croppedBitmapBmp, 0)
                }
            }
        }

        imageProxy.close()
    }

    fun Image.toBitmap(): Bitmap {
        val yBuffer = planes[0].buffer // Y
        val uBuffer = planes[1].buffer // U
        val vBuffer = planes[2].buffer // V

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        //U and V are swapped
        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 50, out)
        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    private fun degreesToFirebaseRotation(degrees: Int): Int = when (degrees) {
        0 -> ROTATION_0
        90 -> ROTATION_90
        180 -> ROTATION_180
        270 -> ROTATION_270
        else -> throw Exception("Rotation must be 0, 90, 180, or 270.")
    }
}