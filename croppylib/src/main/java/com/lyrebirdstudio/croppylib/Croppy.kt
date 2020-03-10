package com.lyrebirdstudio.croppylib

import android.app.Activity
import androidx.fragment.app.Fragment
import com.lyrebirdstudio.croppylib.main.CropRequest
import com.lyrebirdstudio.croppylib.main.CroppyActivity

object Croppy {

    fun start(activity: Activity, cropRequest: CropRequest) {
        CroppyActivity.newIntent(context = activity, cropRequest = cropRequest)
            .also { activity.startActivityForResult(it, cropRequest.requestCode) }
    }

    fun start(fragment: Fragment, cropRequest: CropRequest){
        CroppyActivity.newIntent(context = fragment.requireContext(), cropRequest = cropRequest)
            .also { fragment.startActivityForResult(it, cropRequest.requestCode) }
    }
}