package com.videoplayer

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.videoplayer.databinding.AdapterLayoutBinding

class VideoListAdapter(private  var context: Context, private val videos: List<VideoModel>) :
    RecyclerView.Adapter<VideoListAdapter.ViewHolder>() {
        var binding : AdapterLayoutBinding?= null

    class ViewHolder(binding: AdapterLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = AdapterLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(binding!!)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val video = videos[position]
        binding?.videoTitle?.text   = video.name
        binding?.playVideoConstraint?.setOnClickListener {

            val intent = Intent(context, ActivityVideoPlayer::class.java)
            intent.putExtra("Uri",video.url)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return videos.size
    }
}
