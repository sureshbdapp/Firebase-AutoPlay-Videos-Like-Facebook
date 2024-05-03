package com.videoplayer.video_scroll_view.models.models

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo

 class MediaRepo {
    val data: MutableLiveData<MutableList<MediaObject>> = MutableLiveData()

    @SuppressLint("SuspiciousIndentation")

    fun getMediaData(videoList: ArrayList<VideoModel>): MutableLiveData<MutableList<MediaObject>> {
        //getDataFromFireStore()
        val dataObservable = Observable.create<MutableList<MediaObject>> { emitter ->
            val mediaList = mutableListOf<MediaObject>()

            for (videoModel in videoList) {
                val mediaObject = MediaObject(
                    videoModel.name,
                    videoModel.url,
                    "",
                    ""
                )
                mediaList.add(mediaObject)
            }
            emitter.onNext(mediaList)
            emitter.onComplete()
        }

        dataObservable.subscribe { mediaList ->
            data.value = mediaList
        }.addTo(CompositeDisposable())

        return data
    }
}