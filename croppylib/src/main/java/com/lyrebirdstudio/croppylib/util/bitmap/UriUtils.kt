package com.lyrebirdstudio.croppylib.util.bitmap

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat
import okio.buffer
import okio.sink
import okio.source
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URL

object UriUtils {
    fun processInput(context: Context, input: BitmapUtils.Input) {
        when (input.sourceUri.scheme ?: "") {
            "http", "https" -> handleRemoteResource(context, input)
            "content" -> handleContentResource(context, input)
        }
    }

    private fun handleRemoteResource(context: Context, input: BitmapUtils.Input) {
        // Download file into destinationUri
        val sink = context.contentResolver.openOutputStream(input.destinationUri)?.sink()
        val source = URL(input.sourceUri.toString()).openStream().source().buffer()
        source.use { sink?.use { sink -> it.readAll(sink) } }
        // Assign sourceUri to destinationUri
        input.sourceUri = input.destinationUri
    }

    private fun handleContentResource(context: Context, input: BitmapUtils.Input) {
        val path = getFilePath(context, input)

        if (!path.isNullOrEmpty() && File(path).exists()) {
            input.sourceUri = Uri.fromFile(File(path))
        } else {
            copyFile(context, input.sourceUri, input.destinationUri)
            input.sourceUri = input.destinationUri
        }
    }

    private fun copyFile(context: Context, sourceUri: Uri, destUri: Uri) {
        val input = sourceUri.inputStream(context)
        val output = FileOutputStream(File(destUri.path ?: destUri.toString()))
        input?.use { output.use { output -> input.copyTo(output) } }
    }

    private fun getFilePath(context: Context, input: BitmapUtils.Input): String? {
        return if (
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            FileUtils.getPath(context, input.sourceUri)
        } else {
            null
        }
    }
}

fun Uri.inputStream(context: Context): InputStream? {
    return context.contentResolver.openInputStream(this)
}