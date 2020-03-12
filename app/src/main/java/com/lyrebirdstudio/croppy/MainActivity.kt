package com.lyrebirdstudio.croppy

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import com.lyrebirdstudio.croppy.databinding.ActivityMainBinding
import com.lyrebirdstudio.croppy.file.FileCreator
import com.lyrebirdstudio.croppy.file.FileOperationRequest
import com.lyrebirdstudio.croppylib.Croppy
import com.lyrebirdstudio.croppylib.main.CropRequest
import com.lyrebirdstudio.croppylib.main.CroppyTheme


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.buttonChoose.setOnClickListener {
            pickGallery()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_CROP_IMAGE) {
            data?.data?.let {
                Log.v("TEST", it.toString())
                binding.imageViewCropped.setImageURI(it)
            }
        } else if (requestCode == 999) {
            startCroppy(data?.data ?: return)
        }
    }

    private fun startCroppy(uri: Uri) {

        // Save to given destination uri.
        val destinationUri =
            FileCreator
                .createFile(FileOperationRequest.createRandom(), application.applicationContext)
                .toUri()

        val themeCropRequest = CropRequest(
            sourceUri = uri,
            destinationUri = destinationUri,
            requestCode = RC_CROP_IMAGE,
            croppyTheme = CroppyTheme(R.color.blue)
        )

        Croppy.start(this, themeCropRequest)
    }


    fun pickGallery() {
        startActivityForResult(Intent(Intent.ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        }, 999)
    }

    companion object {
        private const val RC_CROP_IMAGE = 102

    }
}
