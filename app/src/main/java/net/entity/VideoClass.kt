package net.entity

import android.graphics.Bitmap

data class VideoClass(
    var VideoPath: String = "",
    var DisplayDuration: String = "",
    var TrimDuration: Int = 10,
    var FileName: String = "",
    var FileSize: String = "",
    var ThumbBitmap: Bitmap? = null
)
