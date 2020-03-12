package com.lyrebirdstudio.croppylib.main

import android.net.Uri
import android.os.Parcelable
import com.lyrebirdstudio.aspectratiorecyclerviewlib.aspectratio.model.AspectRatio
import com.lyrebirdstudio.croppylib.R
import kotlinx.android.parcel.Parcelize

@Parcelize
open class CropRequest(
    val sourceUri: Uri,
    val destinationUri: Uri,
    val requestCode: Int,
    val excludedAspectRatios: List<AspectRatio> = arrayListOf(),
    val croppyTheme: CroppyTheme = CroppyTheme(R.color.blue)
) : Parcelable {
    companion object {
        fun empty(): CropRequest =
            CropRequest(Uri.EMPTY, Uri.EMPTY, -1, arrayListOf(), CroppyTheme(R.color.blue))
    }
}


