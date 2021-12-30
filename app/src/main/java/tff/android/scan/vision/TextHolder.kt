package tff.android.scan.vision

import android.graphics.Rect
import java.time.ZonedDateTime

class TextHolder(val original: String, val translated: String, var boundingBox: Rect, var lastUpdated: ZonedDateTime, var count: Int = 0)