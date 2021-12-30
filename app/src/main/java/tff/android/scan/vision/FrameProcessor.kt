package tff.android.scan.vision

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import javax.inject.Inject

class FrameProcessor @Inject constructor(private val textDetector: TextDetector) {
    var processingFrame: Boolean = false

    fun processNewImage(bitmap: Bitmap, rotationDegree: Int) {
        processingFrame = true
        analyzeImage(InputImage.fromBitmap(bitmap, rotationDegree))
    }

    fun release() {
        textDetector.stop()
    }

    private fun analyzeImage(image: InputImage) {
        AnalyzerExecutor.EXECUTOR.handler.post {
            textDetector.detectInImage(image) {
                AnalyzerExecutor.EXECUTOR.handler.post { processingFrame = false }
            }
        }
    }
}
