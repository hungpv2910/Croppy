package com.lyrebirdstudio.aspectratiorecyclerviewlib.aspectratio.binding

import android.widget.FrameLayout
import androidx.databinding.BindingAdapter
import com.lyrebirdstudio.aspectratiorecyclerviewlib.aspectratio.AspectRatioItemViewState

@BindingAdapter("aspectSize")
fun setAspectSize(layout: FrameLayout, aspectRatioItemViewState: AspectRatioItemViewState) {
    layout.layoutParams = layout.layoutParams
        .apply {
            height = aspectRatioItemViewState.getAspectHeight(layout.context)
            width = aspectRatioItemViewState.getAspectWidth(layout.context)
        }
}
