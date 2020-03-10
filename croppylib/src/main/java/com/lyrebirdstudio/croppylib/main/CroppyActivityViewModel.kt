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
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class CroppyActivityViewModel(val app: Application) : AndroidViewModel(app) {
    val showProgressLiveData = MutableLiveData<Boolean>()
    val errorCropLiveData = MutableLiveData<Boolean>()

    private val disposable = CompositeDisposable()

    private val saveBitmapLiveData = MutableLiveData<Uri>()

    fun getSaveBitmapLiveData(): LiveData<Uri> = saveBitmapLiveData

    fun saveBitmap(cropRequest: CropRequest, croppedBitmapData: CroppedBitmapData) {

        when (cropRequest) {
            is CropRequest.Manual -> {
                disposable.add(
                        BitmapUtils
                                .saveBitmap(croppedBitmapData, cropRequest.destinationUri.toFile())
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .composeProgress()
                                .subscribe(
                                        { saveBitmapLiveData.value = cropRequest.destinationUri },
                                        { handleErrorCrop() }))
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

                disposable.add(
                        BitmapUtils
                                .saveBitmap(croppedBitmapData, destinationUri.toFile())
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .composeProgress()
                                .subscribe(
                                        { saveBitmapLiveData.value = destinationUri },
                                        { handleErrorCrop() }))

            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (disposable.isDisposed.not()) {
            disposable.dispose()
        }
    }

    private fun Completable.composeProgress(): Completable {
        return this.doOnSubscribe { showProgressLiveData.postValue(true) }
                .doOnComplete { showProgressLiveData.postValue(false) }
    }

    private fun handleErrorCrop(){
        errorCropLiveData.postValue(true)
    }
}