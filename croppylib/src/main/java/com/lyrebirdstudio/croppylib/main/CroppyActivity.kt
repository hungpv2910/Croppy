package com.lyrebirdstudio.croppylib.main

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.lyrebirdstudio.croppylib.R
import com.lyrebirdstudio.croppylib.databinding.ActivityCroppyBinding
import com.lyrebirdstudio.croppylib.ui.ImageCropFragment

class CroppyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCroppyBinding

    private lateinit var viewModel: CroppyActivityViewModel

    private val progressDialog: ProgressDialog by lazy { ProgressDialog() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_croppy)

        viewModel = ViewModelProviders.of(this).get(CroppyActivityViewModel::class.java)

        val cropRequest = intent.getParcelableExtra(KEY_CROP_REQUEST) ?: CropRequest.empty()

        val cropFragment = if (savedInstanceState == null) {
            ImageCropFragment.newInstance(cropRequest).also { fragment ->
                supportFragmentManager.beginTransaction()
                    .add(R.id.containerCroppy, fragment, ImageCropFragment::class.java.name)
                    .commitAllowingStateLoss()
            }
        } else {
            supportFragmentManager.findFragmentByTag(ImageCropFragment::class.java.name) as? ImageCropFragment
        }

        cropFragment?.apply {
            onApplyClicked = {
                viewModel.saveBitmap(cropRequest = cropRequest, croppedBitmapData = it)
            }

            onCancelClicked = {
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        }


        viewModel.getSaveBitmapLiveData().observe(this, Observer {
            setResult(Activity.RESULT_OK, Intent().apply { data = it })
            finish()
        })

        viewModel.showProgressLiveData().observe(this, Observer { show ->
            progressDialog.safeDismiss()
            if (show) {
                progressDialog.show(supportFragmentManager, null)
            }
        })

        viewModel.errorCropLiveData().observe(this, Observer {
            Toast.makeText(
                applicationContext,
                getString(R.string.crop_error_message),
                Toast.LENGTH_LONG
            ).show()
        })
    }

    companion object {

        private const val KEY_CROP_REQUEST = "KEY_CROP_REQUEST"

        fun newIntent(context: Context, cropRequest: CropRequest): Intent {
            return Intent(context, CroppyActivity::class.java)
                .apply {
                    Bundle()
                        .apply { putParcelable(KEY_CROP_REQUEST, cropRequest) }
                        .also { this.putExtras(it) }
                }
        }
    }
}