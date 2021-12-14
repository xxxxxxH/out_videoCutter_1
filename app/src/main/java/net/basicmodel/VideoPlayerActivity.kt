package net.basicmodel

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.MediaController
import android.widget.VideoView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import net.utils.CommonConstants

class VideoPlayerActivity : AppCompatActivity() {
    var videoView: VideoView? = null

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)
        initViews()
        loadDataFromIntent()
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    private fun initViews() {
        videoView = findViewById(R.id.videoViewActVidPlayer)
    }

    private fun loadDataFromIntent() {
        val intent = intent
        if (intent != null) {
            try {
                val filePath = intent.getStringExtra(CommonConstants.KeyVideoPath)
                val uri = Uri.parse(filePath)
                try {
                    val mediaController = MediaController(this)
                    mediaController.setAnchorView(videoView)
                    videoView!!.setMediaController(mediaController)
                    videoView!!.setVideoURI(uri)
                    videoView!!.requestFocus()
                    videoView!!.start()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}