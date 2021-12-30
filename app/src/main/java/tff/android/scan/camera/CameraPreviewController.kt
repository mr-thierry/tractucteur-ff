package tff.android.scan.camera

import android.content.Context
import android.hardware.display.DisplayManager
import android.hardware.display.DisplayManager.DisplayListener
import android.util.DisplayMetrics
import androidx.camera.core.*
import androidx.camera.core.impl.ImageOutputConfig.RotationValue
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import tff.android.MainActivity
import tff.android.ext.UIThread
import tff.android.scan.vision.AnalyzerExecutor
import tff.android.scan.vision.FrameProcessor
import timber.log.Timber
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class CameraPreviewController constructor(
    private val activity: MainActivity,
    private val frameProcessor: FrameProcessor
) {

    private val displayManager by lazy { activity.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager }

    private var displayId = -1
    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var processCameraProvider: ProcessCameraProvider? = null

    private val displayListener = object : DisplayListenerAdapter() {
        override fun onDisplayChanged(displayId: Int) = activity.binding.root.let { view ->
            if (displayId == this@CameraPreviewController.displayId) {
                Timber.d("CameraFragment onDisplayChanged value:${view.display.rotation}")
                imageAnalyzer?.targetRotation = view.display.rotation
            }
        }
    }

    fun onActivityCreated() {
        displayManager.registerDisplayListener(displayListener, null)
    }

    fun onActivityResumed() {
        bindCameraUseCases()
        waitForCameraPreviewToBeReady()
    }

    private fun waitForCameraPreviewToBeReady() {
        if (activity.binding.scanCamerapreview.display == null) {
            UIThread.postDelayed(16) { waitForCameraPreviewToBeReady() }
        } else {
            displayId = activity.binding.scanCamerapreview.display.displayId
        }
    }

    fun onActivityDestroyed() {
        stopPreview()
    }

    private fun stopPreview() {
        displayManager.unregisterDisplayListener(displayListener)
        frameProcessor.release()
        processCameraProvider?.unbindAll()
    }

    fun enableTorch(enable: Boolean) {
        camera?.cameraControl?.enableTorch(enable)
    }

    private fun bindCameraUseCases() {
        val cameraSelector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()
        val cameraProviderFuture = ProcessCameraProvider.getInstance(activity)

        cameraProviderFuture.addListener({
            processCameraProvider = cameraProviderFuture.get().also { processCameraProvider ->
                val screenAspectRatio = aspectRatio()
                val rotation = activity.binding.scanCamerapreview.display.rotation

                preview = buildPreview(screenAspectRatio, rotation)
                imageAnalyzer = buildAnalyzer(screenAspectRatio, rotation)

                processCameraProvider.unbindAll()

                camera = processCameraProvider.bindToLifecycle(activity, cameraSelector, preview, imageAnalyzer)
                preview?.setSurfaceProvider(activity.binding.scanCamerapreview.surfaceProvider)
            }
        }, ContextCompat.getMainExecutor(activity))
    }

    private fun buildPreview(@AspectRatio.Ratio screenAspectRatio: Int, @RotationValue surfaceRotation: Int): Preview {
        return Preview.Builder()
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(surfaceRotation)
            .build()
    }

    private fun buildAnalyzer(@AspectRatio.Ratio screenAspectRatio: Int, @RotationValue surfaceRotation: Int): ImageAnalysis {
        return ImageAnalysis.Builder()
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(surfaceRotation)
            .build()
            .apply {
                setAnalyzer(
                    AnalyzerExecutor.EXECUTOR,
                    PreviewAnalyzer(frameProcessor, activity)
                )
            }
    }

    @Suppress("DEPRECATION")
    @AspectRatio.Ratio
    private fun aspectRatio(): Int {
        val metrics = DisplayMetrics().also { activity.binding.scanCamerapreview.display.getRealMetrics(it) }

        val previewRatio = max(metrics.widthPixels, metrics.heightPixels).toDouble() / min(metrics.widthPixels, metrics.heightPixels)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    companion object {
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
    }
}

open class DisplayListenerAdapter : DisplayListener {
    override fun onDisplayAdded(displayId: Int) = Unit
    override fun onDisplayRemoved(displayId: Int) = Unit
    override fun onDisplayChanged(displayId: Int) = Unit
}