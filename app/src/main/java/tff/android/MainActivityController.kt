package tff.android

import android.Manifest.permission.CAMERA
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.view.ViewPropertyAnimator
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import tff.android.scan.camera.CameraPreviewController

class MainActivityController(
    private val activity: MainActivity,
    private val cameraPreviewController: CameraPreviewController
) {

    private var scanPromptAnimator: ViewPropertyAnimator? = null

    private var torchEnabled = false
    private val flashOffDrawable = ContextCompat.getDrawable(activity, R.drawable.flash_off)
    private val flashOnDrawable = ContextCompat.getDrawable(activity, R.drawable.flash_on)

    private val requestPermissionLauncher =
        activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            //Nothing to do
        }

    init {
        checkPermission()
    }

    @Suppress("IntroduceWhenSubject")
    private fun checkPermission() {
        when {
            ContextCompat.checkSelfPermission(activity, CAMERA) == PERMISSION_GRANTED -> {
                //Nothing to do
            }
            else -> {
                requestPermissionLauncher.launch(CAMERA)
            }
        }
    }

    fun onActivityResumed() {
        activity.binding.apply {
            torch.setOnClickListener { switchTorch() }
            close.setOnClickListener { activity.finish() }

            if (activity.isCameraPermissionGranted()) {
                if (torchEnabled) {
                    switchTorch()
                }
            }
        }
    }

    fun onActivityPaused() {
        scanPromptAnimator?.cancel()
        scanPromptAnimator = null
    }

    private fun switchTorch() {
        activity.binding.apply {
            torchEnabled = !torchEnabled
            cameraPreviewController.enableTorch(torchEnabled)
            torch.setImageDrawable(if (torchEnabled) flashOnDrawable else flashOffDrawable)
        }
    }
}


private fun Context.isCameraPermissionGranted() = ContextCompat.checkSelfPermission(this, CAMERA) == PERMISSION_GRANTED