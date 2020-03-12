package com.lyrebirdstudio.croppylib.main

import android.app.Application
import android.net.Uri
import androidx.core.net.toFile
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.lyrebirdstudio.croppylib.ui.CroppedBitmapData
import com.lyrebirdstudio.croppylib.util.bitmap.BitmapUtils
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