package tff.android.scan.vision

import android.graphics.Rect
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import tff.android.MainActivity
import tff.android.Translator
import tff.android.scan.vision.AnalyzerExecutor.EXECUTOR
import timber.log.Timber
import java.time.ZonedDateTime

class TextDetector constructor(private val mainActivity: MainActivity, private val translator: Translator) {

    private val texts = arrayListOf<TextHolder>()
    private val detector: TextRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    fun detectInImage(image: InputImage, onProcessingComplete: () -> Unit) {
        detector.process(image)
            .addOnSuccessListener(EXECUTOR, {
                handleResult(it)
                onProcessingComplete()
            })
            .addOnCanceledListener(EXECUTOR, { Timber.d("TextDetector onCancel") })
            .addOnCompleteListener(EXECUTOR, { Timber.d("TextDetector onComplete") })
            .addOnFailureListener(EXECUTOR, { Timber.e("TextDetector onFailure", it) })
    }

    fun stop() {
        detector.close()
    }

    private var clearMatch = 0

    private fun handleResult(results: Text) {
        cleanupOldTexts()

        var countMatch = 0
        Timber.v("handleResult text:${results.text.replace("\n", " ")}")

        for (block in results.textBlocks) {
            for (line in block.lines) {
                Timber.d("line:${line.text}")
                for (element in line.elements) {
                    val result = processElement(element.text, element.boundingBox)
                    if (result) countMatch++
                }
            }
        }

        Timber.d("countMatch:$countMatch clearMatch:$clearMatch")
        if (countMatch == 0) {
            clearMatch++
        }

        if (clearMatch > 3) {
            clearMatch = 0
            mainActivity.binding.overlay.setTexts(emptyList())
        } else {
            mainActivity.binding.overlay.setTexts(texts.filter { it.count > 3 })
        }
    }

    private fun processElement(text: String, boundingBox: Rect?): Boolean {
        if (boundingBox != null) {
            val translation = translator.translate(text)
            val holder = texts.firstOrNull { it.original == translation.key && it.boundingBox.intersect(boundingBox) }
            if (holder == null) {
                if (translation.translation != null) {
                    texts.add(TextHolder(translation.key, translation.translation, boundingBox, ZonedDateTime.now()))
                    return true
                }
            } else {
                holder.count++
                holder.boundingBox = boundingBox
                holder.lastUpdated = ZonedDateTime.now()
                return true
            }
        }
        return false
    }

    private fun cleanupOldTexts() {
        val expiration = ZonedDateTime.now().minusSeconds(2)
        val itr = texts.iterator()

        while (itr.hasNext()) {
            val holder = itr.next()
            if (holder.hasExpired(expiration)) {
                itr.remove()
            }
        }
    }

    private fun TextHolder.hasExpired(expiration: ZonedDateTime): Boolean = lastUpdated.isBefore(expiration)
}