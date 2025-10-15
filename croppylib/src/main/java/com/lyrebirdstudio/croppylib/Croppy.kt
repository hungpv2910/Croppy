package com.lyrebirdstudio.croppylib

import android.app.Activity
import android.content.Context
import android.view.View
import androidx.fragment.app.Fragment
import com.lyrebirdstudio.croppylib.main.CropRequest
import com.lyrebirdstudio.croppylib.main.CroppyActivity

object Croppy {
    var exceptionReporter: ExceptionReporter? = null
    var croppyActivityLifecycle: OnCroppyActivityLifecycle? = null

    fun start(activity: Activity, cropRequest: CropRequest) {
        CroppyActivity.newIntent(context = activity, cropRequest = cropRequest)
            .also { activity.startActivityForResult(it, cropRequest.requestCode) }
    }

    fun start(fragment: Fragment, cropRequest: CropRequest) {
        CroppyActivity.newIntent(context = fragment.requireContext(), cropRequest = cropRequest)
            .also { fragment.startActivityForResult(it, cropRequest.requestCode) }
    }

    interface ExceptionReporter {
        fun onException(throwable: Throwable)
    }

    interface OnCroppyActivityLifecycle {
        fun onActivityCreated(context: Context, rootView: View, topAdContainer: View, bottomAdContainer: View)
        fun onActivityDestroy(context: Context)
    }
}