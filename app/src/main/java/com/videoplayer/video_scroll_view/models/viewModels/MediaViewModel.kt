package com.videoplayer.video_scroll_view.models.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.videoplayer.video_scroll_view.models.models.VideoModel
import com.videoplayer.video_scroll_view.models.models.MediaObject
import com.videoplayer.video_scroll_view.models.models.MediaRepo

class MediaViewModel: ViewModel() {
    fun getMedia(videoList: ArrayList<VideoModel>): MutableLiveData<MutableList<MediaObject>>{
        return MediaRepo().getMediaData(videoList)
    }
}