package com.videoplayer.video_scroll_view.models.models

class VideoModel {
    var name: String? = null
    var url: String? = null

    constructor() {
    }

    constructor(title: String, url: String) {
        this.name = title
        this.url = url
    }
}
