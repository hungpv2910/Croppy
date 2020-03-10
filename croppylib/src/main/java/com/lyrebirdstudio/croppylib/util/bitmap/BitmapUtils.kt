package com.lyrebirdstudio.croppylib.util.bitmap

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.net.Uri
import android.view.Display
import android.view.WindowManager
import androidx.exifinterface.media.ExifInterface
import com.lyrebirdstudio.croppylib.ui.CroppedBitmapData
import com.lyrebirdstudio.croppylib.util.extensions.rotateBitmap
import io.reactivex.Completable
import io.reactivex.Single
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import kotlin.math.pow
import kotlin.math.sqrt


object BitmapUtils {

    fun saveBitmap(croppedBitmapData: CroppedBitmapData, file: File): Completable {
        return Completable.create {
            try {
                FileOutputStream(file).use { out ->
                    croppedBitmapData.croppedBitmap?.compress(Bitmap.CompressFormat.PNG, 100, out)
                    it.onComplete()
                }
            } catch (e: Exception) {
                it.onError(e)
            }

        }
    }

    fun resize(uri: Uri, context: Context): Single<ResizedBitmap> {
        return Single.create {
            val maxSize = calculateMaxBitmapSize(context)

            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri), null, options)

            var widthTemp = options.outWidth
            var heightTemp = options.outHeight
            var scale = 1

            while (true) {
                if (widthTemp / 2 < maxSize || heightTemp / 2 < maxSize)
                    break
                widthTemp /= 2
                heightTemp /= 2
                scale *= 2
            }

            val resultOptions = BitmapFactory.Options().apply {
                inSampleSize = scale
            }
            var resizedBitmap = BitmapFactory.decodeStream(
                context.contentResolver.openInputStream(uri),
                null,
                resultOptions
            )

            resizedBitmap = resizedBitmap?.rotateBitmap(getOrientation(context.contentResolver.openInputStream(uri)))

            it.onSuccess(ResizedBitmap(resizedBitmap))
        }
    }

    private fun getOrientation(inputStream: InputStream?): Int {
        val exifInterface: ExifInterface
        var orientation = 0
        try {
            exifInterface = ExifInterface(inputStream!!)
            orientation = exifInterface.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED
            )
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return orientation
    }

    /**
     * This method calculates maximum size of both width and height of bitmap.
     * It is twice the device screen diagonal for default implementation (extra quality to zoom image).
     * Size cannot exceed max texture size.
     *
     * @return - max bitmap size in pixels.
     */
    private fun calculateMaxBitmapSize(context: Context): Int {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        val display: Display
        val width: Int
        val height: Int
        val size = Point()

        if (wm != null) {
            display = wm.defaultDisplay
            display.getSize(size)
        }

        width = size.x
        height = size.y

        // Twice the device screen diagonal as default
        return sqrt(width.toDouble().pow(2.0) + height.toDouble().pow(2.0)).toInt()
    }
}