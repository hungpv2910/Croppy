package com.lyrebirdstudio.croppylib.util.file

import android.content.Context
import android.net.Uri
import androidx.annotation.WorkerThread
import androidx.core.net.toUri
import okio.buffer
import okio.sink
import okio.source
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URL

@Suppress("MemberVisibilityCanBePrivate")
object UriUtils {
    const val DEFAULT_PROCESS_URI_FILE_NAME = "process_uri_file.tmp"

    @WorkerThread
    @JvmOverloads
    fun processUri(context: Context, sourceUri: Uri, destUri: Uri = getDefaultOutputUri(context)) {
        when (sourceUri.scheme ?: "") {
            "http", "https" -> handleRemoteResource(context, sourceUri, destUri)
            "content" -> handleContentResource(context, sourceUri, destUri)
            else -> copyFile(context, sourceUri, destUri)
        }
    }

    fun getDefaultOutputUri(context: Context): Uri {
        return File(context.cacheDir, DEFAULT_PROCESS_URI_FILE_NAME).apply {
            if (!exists()) createNewFile()
        }.toUri()
    }

    private fun handleRemoteResource(context: Context, sourceUri: Uri, destUri: Uri) {
        // Download file into destinationUri
        val sink = context.contentResolver.openOutputStream(destUri)?.sink()
        val source = URL(sourceUri.toString()).openStream().source().buffer()
        source.use { sink?.use { sink -> it.readAll(sink) } }
    }

    private fun handleContentResource(context: Context, sourceUri: Uri, destUri: Uri) {
        copyFile(context, sourceUri, destUri)
    }

    private fun copyFile(context: Context, sourceUri: Uri, destUri: Uri) {
        val input = sourceUri.inputStream(context)
        val output = FileOutputStream(File(destUri.path ?: destUri.toString()))
        input?.use { output.use { output -> input.copyTo(output) } }
    }
}

fun Uri.inputStream(context: Context): InputStream? {
    return try {
        context.contentResolver.openInputStream(this)
    } catch (throwable: FileNotFoundException) {
        throw FileNotFoundException("${throwable.message} - uri: ${toString()}")
    }
}