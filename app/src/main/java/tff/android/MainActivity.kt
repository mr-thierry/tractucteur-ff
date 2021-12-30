package tff.android

import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import androidx.appcompat.app.AppCompatActivity
import tff.android.databinding.ActivityMainBinding
import tff.android.scan.camera.CameraPreviewController
import tff.android.scan.vision.FrameProcessor
import tff.android.scan.vision.TextDetector
import timber.log.Timber
import timber.log.Timber.DebugTree


class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    private val translator = Translator()
    private val textDetector = TextDetector(this, translator)
    private val frameProcessor = FrameProcessor(textDetector)
    private val cameraPreviewController = CameraPreviewController(this, frameProcessor)
    private lateinit var controller: MainActivityController

    init {
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val title = getString(R.string.app_name)
        val ssb = SpannableStringBuilder(title).apply {
            setSpan(StyleSpan(Typeface.BOLD), 0, title.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        supportActionBar?.title = ssb

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        controller = MainActivityController(this, cameraPreviewController)
        cameraPreviewController.onActivityCreated()
        translator.initialize(this)
    }

    override fun onResume() {
        super.onResume()

        controller.onActivityResumed()
        cameraPreviewController.onActivityResumed()
    }

    override fun onPause() {
        controller.onActivityPaused()
        super.onPause()
    }

    override fun onDestroy() {
        cameraPreviewController.onActivityDestroyed()
        super.onDestroy()
    }
}