package net.basicmodel

import android.app.ProgressDialog
import android.content.Context
import android.content.CursorLoader
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import net.interfaces.OnHgLVideoListener
import net.interfaces.OnTrimVideoListener
import net.utils.CommonConstants
import net.widget.HgLVideoTrimmer

class TrimmerActivity : AppCompatActivity(), OnTrimVideoListener,
    OnHgLVideoListener {
    private var mVideoTrimmer: HgLVideoTrimmer? = null
    private var mProgressDialog: ProgressDialog? = null
    var context: Context? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trimmer)
        context = this
        initViews()
    }

    private fun initViews() {
        val extraIntent = intent
        var path: String? = ""
        var maxDuration = 10
        if (extraIntent != null) {
            path = extraIntent.getStringExtra(CommonConstants.EXTRA_VIDEO_PATH)
            maxDuration = extraIntent.getIntExtra(CommonConstants.VIDEO_TOTAL_DURATION, 10)
        }

        //setting progressbar
        mProgressDialog = ProgressDialog(this)
        mProgressDialog!!.setCancelable(false)
        mProgressDialog!!.setMessage(getString(R.string.trimming_progress))
        mVideoTrimmer = findViewById(R.id.timeLine)
        if (mVideoTrimmer != null) {
            Log.e("<>", "maxDuration = $maxDuration")
            //mVideoTrimmer.setMaxDuration(maxDuration);
            mVideoTrimmer!!.setMaxDuration(maxDuration)
            mVideoTrimmer!!.setOnTrimVideoListener(this)
            mVideoTrimmer!!.setOnHgLVideoListener(this)
            //            mVideoTrimmer.setVideoURI(Uri.parse(path));
            mVideoTrimmer!!.setVideoURI(Uri.parse(path))
            mVideoTrimmer!!.setVideoInformationVisibility(true)
        }
    }

    override fun onTrimStarted() {
        mProgressDialog!!.show()
    }

    override fun getResult(uri: Uri?) {
        mProgressDialog!!.cancel()
        runOnUiThread {
            contentUri = uri
            setDataAndStartShareActivity()
            //                Utils.onClickSave(TrimmerActivity.this, TrimmerActivity.this);
        }

    }

    private var contentUri: Uri? = null
    private fun setDataAndStartShareActivity() {
        val intent = Intent(this@TrimmerActivity, ShareImageActivity::class.java)
        intent.putExtra(CommonConstants.KeyVideoPath, contentUri.toString())
        startActivity(intent)
    }

    private fun getRealPathFromURI(contentUri: Uri): String {
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val loader = CursorLoader(this@TrimmerActivity, contentUri, proj, null, null, null)
        val cursor = loader.loadInBackground()
        val column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        val result = cursor.getString(column_index)
        cursor.close()
        return result
    }

    private fun playUriOnVLC(uri: Uri) {
        val vlcRequestCode = 42
        val vlcIntent = Intent(Intent.ACTION_VIEW)
        vlcIntent.setPackage("org.videolan.vlc")
        vlcIntent.setDataAndTypeAndNormalize(uri, "video/*")
        vlcIntent.putExtra("title", "Kung Fury")
        vlcIntent.putExtra("from_start", false)
        vlcIntent.putExtra("position", 90000L)
        startActivityForResult(vlcIntent, vlcRequestCode)
    }

    override fun cancelAction() {
        mProgressDialog!!.cancel()
        mVideoTrimmer!!.destroy()
        finish()
    }

    override fun onError(message: String?) {
        mProgressDialog!!.cancel()
        runOnUiThread {
        }
    }

    override fun onVideoPrepared() {
        runOnUiThread {
        }
    }
}