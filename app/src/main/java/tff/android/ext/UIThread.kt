package tff.android.ext

import android.os.Handler
import android.os.Looper

object UIThread {

    private val handler = Handler(Looper.getMainLooper())

    fun postDelayed(delayMillis: Long, runnable: Runnable) {
        handler.postDelayed(runnable, delayMillis)
    }
}
