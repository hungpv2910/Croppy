package com.lyrebirdstudio.croppylib.util.bitmap

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.net.Uri
import android.view.Display
import android.view.WindowManager
import androidx.exifinterface.media.ExifInterface
import com.lyrebirdstudio.croppylib.util.extensions.rotateBitmap
import com.lyrebirdstudio.croppylib.util.file.UriUtils.getDefaultOutputUri
import com.lyrebirdstudio.croppylib.util.file.UriUtils.processUri
import com.lyrebirdstudio.croppylib.util.file.inputStream
import io.reactivex.Completable
import io.reactivex.Single
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

object BitmapUtils {
    fun saveBitmap(bitmap: Bitmap?, file: File): Completable {
        return Completable.create {
            try {
                FileOutputStream(file).use { out ->
                    bitmap?.compress(Bitmap.CompressFormat.PNG, 100, out)
                    it.onComplete()
                }
            } catch (e: Exception) {
                it.onError(e)
            }

        }
    }

    fun resize(sourceUri: Uri, context: Context): Single<ResizedBitmap> {
        return Single.create {
            try {
                processUri(context, sourceUri)
                val destUri = getDefaultOutputUri(context)

                val (reqWidth, reqHeight) = calculateMaxBitmapSize(context)
                val orientation =
                    destUri.inputStream(context).use { stream -> getOrientation(stream) }
                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                destUri.inputStream(context).use { stream -> getBitmap(stream, options) }

                options.inSampleSize =
                    calculateInSampleSize(options, reqWidth, reqHeight, orientation)
                options.inJustDecodeBounds = false

                val resizedBitmap = destUri.inputStream(context)
                    .use { stream -> getBitmap(stream, options) }
                    ?.rotateBitmap(orientation)

                if (resizedBitmap == null) it.onError(
                    Exception(
                        "Could not rotate bitmap, seemed an OOM?" +
                                " sourceUri: $sourceUri. transformedSource: $destUri"
                    )
                )
                else it.onSuccess(ResizedBitmap(resizedBitmap))

            } catch (ex: Throwable) {
                it.onError(ex)
            }
        }
    }

    private fun getBitmap(
        inputStream: InputStream?,
        options: BitmapFactory.Options? = null
    ): Bitmap? {
        return try {
            if (inputStream == null) return null
            BitmapFactory.decodeStream(inputStream, null, options)
        } catch (e: OutOfMemoryError) {
            return null
        }
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int,
        orientation: Int
    ): Int {
        // Raw height and width of image
        val (height: Int, width: Int) = options.run {
            when (orientation) {
                ExifInterface.ORIENTATION_TRANSPOSE,
                ExifInterface.ORIENTATION_ROTATE_90,
                ExifInterface.ORIENTATION_TRANSVERSE,
                ExifInterface.ORIENTATION_ROTATE_270 -> outWidth to outHeight
                else -> outHeight to outWidth
            }
        }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {

            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    private fun getOrientation(inputStream: InputStream?): Int {
        val exifInterface: ExifInterface
        var orientation = ExifInterface.ORIENTATION_UNDEFINED
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

    private fun calculateMaxBitmapSize(context: Context): Pair<Int, Int> {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        val display: Display
        val width: Int
        val height: Int
        val size = Point()

        if (wm != null) {
            display = wm.defaultDisplay
            display.getSize(size)
        }

        width = (size.x * 0.75f).toInt()
        height = (size.y * 0.75f).toInt()
        return width to height
    }
}