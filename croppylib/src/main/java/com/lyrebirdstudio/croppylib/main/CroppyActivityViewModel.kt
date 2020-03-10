package com.lyrebirdstudio.croppylib.main

import android.app.Application
import android.net.Uri
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.lyrebirdstudio.croppylib.ui.CroppedBitmapData
import com.lyrebirdstudio.croppylib.util.bitmap.BitmapUtils
import com.lyrebirdstudio.croppylib.util.file.FileCreator
import com.lyrebirdstudio.croppylib.util.file.FileExtension
import com.lyrebirdstudio.croppylib.util.file.FileOperationRequest
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class CroppyActivityViewModel(val app: Application) : AndroidViewModel(app) {
    private val showProgressLiveData = MutableLiveData<Boolean>()
    private val errorCropLiveData = MutableLiveData<Boolean>()
    private val saveBitmapLiveData = MutableLiveData<Uri>()

    private var cropDisposable: Disposable? = null

    fun getSaveBitmapLiveData(): LiveData<Uri> = saveBitmapLiveData
    fun showProgressLiveData(): LiveData<Boolean> = showProgressLiveData
    fun errorCropLiveData(): LiveData<Boolean> = errorCropLiveData

    fun saveBitmap(cropRequest: CropRequest, croppedBitmapData: CroppedBitmapData) {
        cropDisposable?.dispose()
        when (cropRequest) {
            is CropRequest.Manual -> {
                BitmapUtils
                    .saveBitmap(croppedBitmapData, cropRequest.destinationUri.toFile())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .composeProgress()
                    .subscribe(
                        { saveBitmapLiveData.value = cropRequest.destinationUri },
                        { doOnCropError() })
                    .let { cropDisposable = it }
            }
            is CropRequest.Auto -> {
                val destinationUri = FileCreator.createFile(
                    FileOperationRequest(
                        cropRequest.storageType,
                        System.currentTimeMillis().toString(),
                        FileExtension.PNG
                    ),
                    app.applicationContext
                ).toUri()

                BitmapUtils
                    .saveBitmap(croppedBitmapData, destinationUri.toFile())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .composeProgress()
                    .subscribe(
                        { saveBitmapLiveData.value = destinationUri },
                        { doOnCropError() })
                    .let { cropDisposable = it }

            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        cropDisposable?.dispose()
    }

    private fun Completable.composeProgress(): Completable {
        return this.doOnSubscribe { showProgressLiveData.postValue(true) }
            .doOnTerminate { showProgressLiveData.postValue(false) }
    }

    private fun doOnCropError() {
        errorCropLiveData.postValue(true)
    }
}