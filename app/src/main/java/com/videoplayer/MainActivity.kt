package com.videoplayer

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import com.videoplayer.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
 private var binding : ActivityMainBinding? = null
    private val PICK_VIDEO_REQUEST = 1
    private var selectedVideoUri: Uri? = null
    var videoList = arrayListOf<VideoModel>()
    var videoListAdapter : VideoListAdapter?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

      binding!!.uploadConstraint.setOnClickListener {
          openGallery()
      }

        videoListAdapter = VideoListAdapter(this,videoList)
        binding!!.recyclerView.adapter = videoListAdapter

        getDataFromFireStore()

    }

    private fun openGallery() {
        val intent = Intent()
        intent.type = "video/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Video"), PICK_VIDEO_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_VIDEO_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            selectedVideoUri = data.data
            uploadVideoToFirebaseStorage()
        }
    }

    private fun uploadVideoToFirebaseStorage() {
        binding!!.progressCircular.visibility = View.VISIBLE
        if (selectedVideoUri != null) {
            val videoFileName = "video_${System.currentTimeMillis()}.mp4"
            val storageRef = FirebaseStorage.getInstance().reference.child("videos").child(videoFileName)
            storageRef.putFile(selectedVideoUri!!)
                .addOnSuccessListener { taskSnapshot ->
                    binding!!.progressCircular.visibility = View.GONE
                    Toast.makeText(this, "Video uploaded successfully", Toast.LENGTH_SHORT).show()
                    // Get the download URL of the uploaded video
                    taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                        saveVideoDataToFirestore(videoFileName, uri.toString())
                    }
                }
                .addOnFailureListener { exception ->
                    binding!!.progressCircular.visibility = View.GONE
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
    @SuppressLint("SuspiciousIndentation")
    fun  getDataFromFireStore(){
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
                            videoListAdapter?.notifyDataSetChanged()
                        }
                    }
                }
        )
    }
}
