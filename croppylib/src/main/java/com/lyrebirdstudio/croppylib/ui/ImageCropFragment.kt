package com.lyrebirdstudio.croppylib.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.lyrebirdstudio.croppylib.R
import com.lyrebirdstudio.croppylib.databinding.FragmentImageCropBinding
import com.lyrebirdstudio.croppylib.main.CropRequest
import com.lyrebirdstudio.croppylib.state.CropFragmentViewState
import com.lyrebirdstudio.croppylib.util.delegate.inflate

class ImageCropFragment : Fragment() {

    private val binding: FragmentImageCropBinding by inflate(R.layout.fragment_image_crop)

    private lateinit var viewModel: ImageCropViewModel

    var onApplyClicked: ((CroppedBitmapData) -> Unit)? = null

    var onCancelClicked: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(ImageCropViewModel::class.java)

        val cropRequest = arguments?.getParcelable(KEY_BUNDLE_CROP_REQUEST) ?: CropRequest.empty()
        viewModel.setCropRequest(cropRequest)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        viewModel.getCropRequest()?.let {
            binding.cropView.setTheme(it.croppyTheme)
            binding.recyclerViewAspectRatios.setActiveColor(it.croppyTheme.accentColor)
            binding.recyclerViewAspectRatios.excludeAspectRatio(*it.excludedAspectRatios.toTypedArray())
        }

        binding.recyclerViewAspectRatios.setItemSelectedListener {
            binding.cropView.setAspectRatio(it.aspectRatioItem.aspectRatio)
            viewModel.onAspectRatioChanged(it.aspectRatioItem.aspectRatio)
        }

        binding.imageViewCancel.setOnClickListener {
            onCancelClicked?.invoke()
        }

        binding.imageViewApply.setOnClickListener {
            onApplyClicked?.invoke(binding.cropView.getCroppedData())
        }

        with(binding.cropView) {

            onInitialized = {
                viewModel.updateCropSize(binding.cropView.getCropSizeOriginal())
            }

            observeCropRectOnOriginalBitmapChanged = {
                viewModel.updateCropSize(binding.cropView.getCropSizeOriginal())
            }
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel
            .getCropViewStateLiveData()
            .observe(viewLifecycleOwner, Observer(this@ImageCropFragment::renderViewState))

        viewModel
            .getResizedBitmapLiveData()
            .observe(viewLifecycleOwner, Observer { resizedBitmap ->
                if (resizedBitmap.bitmap == null) {
                    activity?.let {
                        Toast.makeText(it.applicationContext, R.string.crop_error_message, Toast.LENGTH_LONG).show()
                        it.finish()
                    }
                    return@Observer
                }

                if (ViewCompat.isLaidOut(binding.cropView)) {
                    binding.cropView.setBitmap(resizedBitmap.bitmap)
                } else {
                    binding.cropView.doOnPreDraw { binding.cropView.setBitmap(resizedBitmap.bitmap) }
                }
            })

    }

    override fun onResume() {
        super.onResume()
        binding.recyclerViewAspectRatios.reset()
    }

    private fun renderViewState(cropFragmentViewState: CropFragmentViewState) {
        binding.viewState = cropFragmentViewState
        binding.executePendingBindings()
    }

    companion object {

        private const val KEY_BUNDLE_CROP_REQUEST = "KEY_BUNDLE_CROP_REQUEST"

        @JvmStatic
        fun newInstance(cropRequest: CropRequest): ImageCropFragment {
            return ImageCropFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(KEY_BUNDLE_CROP_REQUEST, cropRequest)
                }
            }
        }
    }

}