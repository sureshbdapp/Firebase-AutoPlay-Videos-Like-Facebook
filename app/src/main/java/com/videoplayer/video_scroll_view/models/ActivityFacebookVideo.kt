package com.videoplayer.video_scroll_view.models

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import com.videoplayer.video_scroll_view.models.viewModels.MediaViewModel
import com.videoplayer.R
import com.videoplayer.video_scroll_view.models.models.VideoModel
import com.videoplayer.databinding.FragmentFacebookPlayerBinding
import com.videoplayer.video_scroll_view.models.models.MediaObject
import com.videoplayer.video_scroll_view.models.adapter.PlayerViewAdapter.Companion.playIndexThenPausePreviousPlayer
import com.videoplayer.video_scroll_view.models.adapter.PlayerViewAdapter.Companion.releaseAllPlayers
import com.videoplayer.video_scroll_view.models.adapter.FacebookRecyclerAdapter


class ActivityFacebookVideo : AppCompatActivity() {
    private var recyclerView: RecyclerView? = null
    private var mAdapter: FacebookRecyclerAdapter? = null
    private val modelList = ArrayList<MediaObject>()
   lateinit var  binding : FragmentFacebookPlayerBinding
    private lateinit var scrollListener: RecyclerViewScrollListener
    val videoList = ArrayList<VideoModel>()
    private val PICK_VIDEO_REQUEST = 1
    private var selectedVideoUri: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentFacebookPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding!!.uploadConstraint.setOnClickListener {
            openGallery()
        }

        findViews(binding.root)
        setAdapter()
        getDataFromFireStore()



    }
    fun setViewModelData(videoList: ArrayList<VideoModel>) {
        val model = ViewModelProvider(this)[MediaViewModel::class.java]
        model.getMedia(videoList).observe(this, Observer {
            mAdapter?.updateList(arrayListOf(*it.toTypedArray()))
            binding.progressCircular.visibility = View.GONE
        })
    }
    private fun openGallery() {
        val intent = Intent()
        intent.type = "video/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Video"), PICK_VIDEO_REQUEST)
    }
    @SuppressLint("SuspiciousIndentation")
    fun  getDataFromFireStore(){
        binding.progressCircular.visibility = View.VISIBLE
        val query : Query = FirebaseFirestore.getInstance().collection("videos")
        query . addSnapshotListener( object : EventListener<QuerySnapshot> {

            @SuppressLint("NotifyDataSetChanged")
            override fun onEvent(
                value: QuerySnapshot?,
                error: FirebaseFirestoreException?
            ) {
                if (error != null) {
                    error.message?.let { Log.e("Firebase error", it) }
                    return
                }
                for (dc in value!!.documentChanges) {
                    if (dc.type === DocumentChange.Type.ADDED) {
                        videoList.add(dc.document.toObject(VideoModel::class.java))
                    }
                }
                setViewModelData(videoList)
            }
        }
        )
    }

    private fun findViews(view: View) {
        recyclerView = view.findViewById<View>(R.id.recycler_view) as RecyclerView
    }

    private fun setAdapter() {
        mAdapter = FacebookRecyclerAdapter(this, modelList)
        recyclerView!!.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(this)
        recyclerView!!.layoutManager = layoutManager
        recyclerView!!.adapter = mAdapter
        scrollListener = object : RecyclerViewScrollListener() {
            override fun onItemIsFirstVisibleItem(index: Int) {
                Log.d("visible item index", index.toString())
                if (index != -1)
                    playIndexThenPausePreviousPlayer(index)
            }

        }
        recyclerView!!.addOnScrollListener(scrollListener)
        mAdapter!!.SetOnItemClickListener(object : FacebookRecyclerAdapter.OnItemClickListener {
            override fun onItemClick(view: View?, position: Int, model: MediaObject?) {
            }
        })
    }

    override fun onPause() {
        super.onPause()
        releaseAllPlayers()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_VIDEO_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            selectedVideoUri = data.data
            uploadVideoToFirebaseStorage()
        }
    }

    private fun uploadVideoToFirebaseStorage() {
        binding.progressUpload.visibility = View.VISIBLE
        if (selectedVideoUri != null) {
            val videoFileName = "video_${System.currentTimeMillis()}.mp4"
            val storageRef = FirebaseStorage.getInstance().reference.child("videos").child(videoFileName)
            storageRef.putFile(selectedVideoUri!!)
                .addOnSuccessListener { taskSnapshot ->
                    binding.progressUpload.visibility = View.GONE
                    Toast.makeText(this, "Video uploaded successfully", Toast.LENGTH_SHORT).show()
                    // Get the download URL of the uploaded video
                    taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                        saveVideoDataToFirestore(videoFileName, uri.toString())
                    }
                }
                .addOnFailureListener { exception ->
                    binding!!.progressUpload.visibility = View.GONE
                    Toast.makeText(this, "Upload failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveVideoDataToFirestore(videoFileName: String, downloadUrl: String) {
        val db = FirebaseFirestore.getInstance()
        val videoData = hashMapOf(
            "name" to videoFileName,
            "url" to downloadUrl
        )
        db.collection("videos")
            .add(videoData)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(this, "Video data saved to Firestore", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Firestore error: $e", Toast.LENGTH_SHORT).show()
            }
    }
}