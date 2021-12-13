package net.basicmodel

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import com.google.android.material.snackbar.Snackbar
import com.snatik.storage.Storage
import net.interfaces.PermissionCallback
import net.utils.CommonConstants
import net.utils.CommonUtilities
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ShareImageActivity : AppCompatActivity(), View.OnClickListener,
    PermissionCallback {
    var context: Context? = null
    var storage: Storage? = null
    var btnBack: ImageView? = null
    var imgShare: ImageView? = null
    var videoView: VideoView? = null
    var file: File? = null
    private var saveState = false
    var folder: File? = null
    var isSaveState = false
    private var filePath: Uri? = null
    var llAdView: RelativeLayout? = null
    var llAdViewFacebook: LinearLayout? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share_image)
        initViews()
        loadDataFromIntent()
        llAdView = findViewById(R.id.llAdView)
        llAdViewFacebook = findViewById(R.id.llAdViewFacebook)
        llAdView!!.visibility = View.GONE
        llAdViewFacebook!!.visibility = View.GONE
    }

    fun initViews() {
        context = this
        storage = Storage(context)
        videoView = findViewById(R.id.videoView)
        imgShare = findViewById(R.id.imgShare)
        btnBack = findViewById(R.id.btnBack)
    }

    private fun loadDataFromIntent() {
        val intent = intent
        if (intent != null) {
            val filepath = intent.getStringExtra(CommonConstants.KeyVideoPath)
            file = File(filepath)
            filePath = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val uri1 = Uri.fromFile(file)
                Uri.parse(CommonUtilities.getFilePath(this, uri1))
            } else {
                Uri.fromFile(file)
            }
            loadAppContent(filePath)
        }
    }

    fun loadAppContent(uri: Uri?) {
        try {
            saveState = false
            val mediaController = MediaController(this)
            mediaController.setAnchorView(videoView)
            videoView!!.setMediaController(mediaController)
            videoView!!.setVideoURI(uri)
            videoView!!.requestFocus()
            videoView!!.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btnBack -> {
                saveState = false
                finish()
            }
            R.id.btnSaveGallery -> if (CommonUtilities.checkRequiredPermission(this)) {
                if (saveState == false) {
                    onClickDownload()
                } else {
                    Snackbar.make(btnBack!!, "Already Save To My Album", 1000).show()
                }
            } else {
                CommonUtilities.requestRequiredPermission(this@ShareImageActivity)
            }
            R.id.btnInsta -> onClickInsta()
            R.id.btnWhatsapp -> onClickWhatsApp()
            R.id.btnFb -> onClickFb()
            R.id.btnShare -> onClickShare()
            R.id.btnCreateNewFrame -> {
                saveState = false
                startActivity(
                    Intent(
                        this,
                        MainActivity::class.java
                    ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                )
                finish()
            }
        }
    }

    fun onClickInsta() {
        val intent = packageManager.getLaunchIntentForPackage("com.instagram.android")
        if (intent != null) {
            val share = Intent()
            share.action = Intent.ACTION_SEND
            share.type = "video/*"
            share.setPackage("com.instagram.android")
            share.putExtra(Intent.EXTRA_STREAM, filePath)
            startActivity(share)
        } else {
            Toast.makeText(context, "Instagram Not Installed", Toast.LENGTH_LONG).show()
        }
    }

    fun onClickWhatsApp() {
        val intent = packageManager.getLaunchIntentForPackage("com.whatsapp")
        if (intent != null) {
            val share = Intent()
            share.action = Intent.ACTION_SEND
            share.type = "video/*"
            share.setPackage("com.whatsapp")
            share.putExtra(Intent.EXTRA_STREAM, filePath)
            startActivity(share)
        } else {
            Toast.makeText(context, "WhatsApp Not Installed", Toast.LENGTH_LONG).show()
        }
    }

    fun onClickFb() {
        val intent = packageManager.getLaunchIntentForPackage("com.facebook.katana")
        if (intent != null) {
            val share = Intent()
            share.action = Intent.ACTION_SEND
            share.type = "video/*"
            share.setPackage("com.facebook.katana")
            share.putExtra(Intent.EXTRA_STREAM, filePath)
            startActivity(share)
        } else {
            Toast.makeText(context, "Facebook Not Installed", Toast.LENGTH_LONG).show()
        }
    }

    fun onClickDownload() {
        if (isSaveState == false) {
            openEditCatNameDialog(this)
        } else {
            Snackbar.make(btnBack!!, "Video is already saved...", 1000).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CommonConstants.RequestCodePermission) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (saveState == false) {
                    openEditCatNameDialog(this)
                } else {
                    Snackbar.make(btnBack!!, "Video is already saved...", 1000).show()
                }
            } else {
                CommonUtilities.showPermissionConfirmDialog(this@ShareImageActivity)
            }
        }
    }

    private fun addImageGallery(file: File) {
        val cR = context!!.contentResolver
        val mime = MimeTypeMap.getSingleton()
        val type = mime.getExtensionFromMimeType(cR.getType(Uri.fromFile(file)))
        Log.e("TAG", "addImageGallery::::MIME TYPE:::  $type")
        val values = ContentValues()
        values.put(MediaStore.Video.Media.DATA, file.absolutePath)
        values.put(MediaStore.Video.Media.MIME_TYPE, "video/*")
        //        values.put(MediaStore.Images.Media.MIME_TYPE, type);
        contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
    }

    fun onClickShare() {
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.type = "video/*"
        val link = "https://play.google.com/store/apps/details?id=" + context!!.packageName
        intent.putExtra(Intent.EXTRA_TEXT, link)
        intent.putExtra(Intent.EXTRA_TITLE, link)
        intent.putExtra(Intent.EXTRA_STREAM, filePath)
        startActivity(intent)
    }

    fun openEditCatNameDialog(context: Context?) {
        val margin = resources.getDimension(R.dimen.twenty_dp).toInt()
        val alertDialog = AlertDialog.Builder(
            context!!
        )
        alertDialog.setTitle(CommonConstants.CapMessage)
        alertDialog.setMessage(CommonConstants.MsgEnterFileName)
        val linearLayout = LinearLayout(context)
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(margin, 0, margin, 0)
        val fileName =
            "Video_Cutter_" + SimpleDateFormat("dd_MMM_yyyy_hh_mm_ss", Locale.getDefault()).format(
                Date()
            ) + ".mp4"
        val etCatName = AppCompatEditText(context)
        etCatName.setText(fileName)
        etCatName.layoutParams = layoutParams
        linearLayout.addView(etCatName)
        alertDialog.setView(linearLayout)
        alertDialog.setPositiveButton(CommonConstants.CapSave,
            DialogInterface.OnClickListener { dialog, which ->
                try {
                    val newCategoryName = etCatName.text.toString()
                    if (newCategoryName.isEmpty()) {
                        Toast.makeText(
                            context,
                            CommonConstants.MsgEnterFileName,
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            if (Environment.isExternalStorageManager()) {
                                saveVideo(newCategoryName)
                            } else {
                                CommonUtilities.showSettingsDialog(
                                    true,
                                    getString(R.string.allow_title),
                                    getString(R.string.you_need_to),
                                    this@ShareImageActivity
                                )
                            }
                        } else {
                            CommonUtilities.checkPermission(
                                this@ShareImageActivity,
                                this@ShareImageActivity,
                                newCategoryName,
                                0
                            )
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            })
        alertDialog.setNegativeButton(CommonConstants.CapCancel,
            DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() })
        alertDialog.show()
    }

    fun saveVideo(newCategoryName: String) {
        var newCategoryName = newCategoryName
        if (!newCategoryName.contains(".mp4")) {
            newCategoryName = "$newCategoryName.mp4"
        }
        SaveVideoToGallery(newCategoryName).execute()
    }

    override fun PermissionGrant(name: String?, pos: Int) {
        saveVideo(name!!)
    }

    @SuppressLint("StaticFieldLeak")
    inner class SaveVideoToGallery(fileName: String) :
        AsyncTask<Void?, Void?, Void?>() {
        private var mProgressDialog: ProgressDialog? = null
        private var fileName = ""
        override fun onPreExecute() {
            super.onPreExecute()
            mProgressDialog = ProgressDialog(this@ShareImageActivity)
            mProgressDialog!!.setCancelable(false)
            mProgressDialog!!.setMessage(getString(R.string.trimming_progress))
            if (!mProgressDialog!!.isShowing) {
                mProgressDialog!!.show()
            }
        }

        override fun doInBackground(vararg voids: Void?): Void? {
            val extStorageDirectory = Environment.getExternalStorageDirectory().toString()
            folder = File(extStorageDirectory, context!!.resources.getString(R.string.app_name))
            if (!folder!!.exists()) {
                if (folder!!.mkdir()) {
                    Log.e("<><>", "Dir Created")
                }
            }
            val filePath = folder.toString() + File.separator + fileName + ".mp4"
            storage!!.copy(file!!.absolutePath, filePath)
            val file1 = File(filePath)
            addImageGallery(file1)
            isSaveState = true
            return null
        }

        override fun onPostExecute(aVoid: Void?) {
            super.onPostExecute(aVoid)
            mProgressDialog!!.dismiss()
            Snackbar.make(btnBack!!, "Saved Successfully", 1000).show()
        }

        init {
            this.fileName = fileName
        }
    }

    override fun onBackPressed() {
        /*super.onBackPressed();
        saveState = false;*/
        saveState = false
        startActivity(
            Intent(
                this,
                MainActivity::class.java
            ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        )
        finish()
    }

}