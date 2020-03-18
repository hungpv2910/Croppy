package com.lyrebirdstudio.croppylib.util.bitmap

import android.graphics.Bitmap
import com.lyrebirdstudio.croppylib.ui.CroppedBitmapData
import io.reactivex.Observable
import kotlin.math.roundToInt

object CropUtils {
    fun doCrop(croppedBitmapData: CroppedBitmapData): Observable<Bitmap> {
        return Observable.fromCallable {
            val bitmapRect = croppedBitmapData.bitmapRect
            val croppedBitmapRect = croppedBitmapData.croppedBitmapRect
            val sourceBitmap = croppedBitmapData.sourceBitmap

            val shouldUseOriginal = croppedBitmapRect.contains(bitmapRect) || bitmapRect.intersect(croppedBitmapRect).not()
            if (sourceBitmap != null && shouldUseOriginal) {
                return@fromCallable sourceBitmap
            }

            val cropLeft = if (croppedBitmapRect.left.roundToInt() < bitmapRect.left) {
                bitmapRect.left.toInt()
            } else {
                croppedBitmapRect.left.roundToInt()
            }

            val cropTop = if (croppedBitmapRect.top.roundToInt() < bitmapRect.top) {
                bitmapRect.top.toInt()
            } else {
                croppedBitmapRect.top.roundToInt()
            }

            val cropRight = if (croppedBitmapRect.right.roundToInt() > bitmapRect.right) {
                bitmapRect.right.toInt()
            } else {
                croppedBitmapRect.right.roundToInt()
            }

            val cropBottom = if (croppedBitmapRect.bottom.roundToInt() > bitmapRect.bottom) {
                bitmapRect.bottom.toInt()
            } else {
                croppedBitmapRect.bottom.roundToInt()
            }

            if (sourceBitmap != null) {
                return@fromCallable Bitmap.createBitmap(
                    sourceBitmap, cropLeft, cropTop, cropRight - cropLeft, cropBottom - cropTop
                )
            }

            throw IllegalStateException("Bitmap is null.")
        }
    }
}