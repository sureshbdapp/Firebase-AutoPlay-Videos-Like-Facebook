package com.videoplayer.video_scroll_view.models.callback_interface

import com.google.android.exoplayer2.Player

interface PlayerStateCallback {

    fun onVideoDurationRetrieved(duration: Long, player: Player)

    fun onVideoBuffering(player: Player)

    fun onStartedPlaying(player: Player)

    fun onFinishedPlaying(player: Player)
}