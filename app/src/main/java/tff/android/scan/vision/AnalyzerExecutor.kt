package tff.android.scan.vision

import android.os.Handler
import android.os.HandlerThread
import java.util.concurrent.Executor

object AnalyzerExecutor {
    val EXECUTOR = HandlerExecutor()

    class HandlerExecutor : Executor {
        private val analyzerThread = HandlerThread("scan").apply { start() }

        val handler = Handler(analyzerThread.looper)

        override fun execute(runnable: Runnable) {
            this.handler.post(runnable)
        }
    }
}


