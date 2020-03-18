package com.lyrebirdstudio.croppylib.ui

import android.graphics.Bitmap
import android.graphics.RectF

data class CroppedBitmapData(
    val croppedBitmapRect: RectF,
    val bitmapRect: RectF,
    val sourceBitmap: Bitmap?
)