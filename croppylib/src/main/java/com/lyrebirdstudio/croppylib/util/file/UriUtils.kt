package com.lyrebirdstudio.croppylib.util.file

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.annotation.WorkerThread
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import okio.buffer
import okio.sink
import okio.source
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URL

object UriUtils {
    @WorkerThread
    fun processUri(context: Context, sourceUri: Uri, tempFileName: String? = null): Uri {
        return when (sourceUri.scheme ?: "") {
            "http", "https" -> handleRemoteResource(context, sourceUri, tempFileName)
            "content" -> handleContentResource(context, sourceUri, tempFileName)
            else -> sourceUri
        }
    }

    private fun handleRemoteResource(context: Context, sourceUri: Uri, tempFileName: String?): Uri {
        val tempUri = newFile(context, tempFileName).toUri()

        // Download file into destinationUri
        val sink = context.contentResolver.openOutputStream(tempUri)?.sink()
        val source = URL(sourceUri.toString()).openStream().source().buffer()
        source.use { sink?.use { sink -> it.readAll(sink) } }
        return tempUri
    }

    private fun handleContentResource(
        context: Context,
        sourceUri: Uri,
        tempFileName: String?
    ): Uri {
        val path = if (ContextCompat.checkSelfPermission(
                context, Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            FileUtils.getPath(context, sourceUri)
        } else {
            null
        }

        return if (!path.isNullOrEmpty() && File(path).exists()) {
            Uri.fromFile(File(path))
        } else {
            val tempUri = newFile(context, tempFileName).toUri()
            copyFile(context, sourceUri, tempUri)
            tempUri
        }
    }

    private fun copyFile(context: Context, sourceUri: Uri, destUri: Uri) {
        val input = sourceUri.inputStream(context)
        val output = FileOutputStream(File(destUri.path ?: destUri.toString()))
        input?.use { output.use { output -> input.copyTo(output) } }
    }

    private fun newFile(context: Context, fileName: String?) = File(
        context.filesDir,
        if (fileName.isNullOrBlank()) "process_uri_file.tmp"
        else fileName
    ).apply {
        if (!exists()) createNewFile()
    }
}

fun Uri.inputStream(context: Context): InputStream? {
    return try {
        context.contentResolver.openInputStream(this)
    } catch (throwable: FileNotFoundException) {
        throw FileNotFoundException("File could could not be found! uri: ${toString()}")
    }
}