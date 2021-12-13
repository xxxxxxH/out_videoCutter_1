package net.basicmodel

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.messaging.FirebaseMessaging
import net.adapter.GridVideoAdapter
import net.adapter.ListVideoAdapter
import net.entity.VideoClass
import net.interfaces.AdapterItemTypeCallback
import net.interfaces.CallbackListener
import net.interfaces.PermissionCallback
import net.utils.CommonConstants
import net.utils.CommonUtilities
import java.io.File
import java.util.*

class MainActivity : BaseActivity(), View.OnClickListener,
    AdapterItemTypeCallback, CallbackListener, PermissionCallback {
    var rvVideoList: RecyclerView? = null
    var llPermission: LinearLayout? = null
    var tvAllow: AppCompatTextView? = null
    var llNoData: LinearLayout? = null
    var imgDrawer: AppCompatImageView? = null
    var imgGridList: AppCompatImageView? = null
    private var gridVideoAdapter: GridVideoAdapter? = null
    private var listVideoAdapter: ListVideoAdapter? = null
    private var videoClassArrayList: ArrayList<VideoClass>? = null
    private var isGridView = true
    var llAdView: RelativeLayout? = null
    var llAdViewFacebook: LinearLayout? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()
        checkPermissionAndLoadVideos()
        successCall()
        try {
            //subScribeToFirebaseTopic();
        } catch (e: Exception) {
            e.printStackTrace()
        }
        llAdView = findViewById(R.id.llAdView)
        llAdViewFacebook = findViewById(R.id.llAdViewFacebook)
        llAdView!!.visibility = View.GONE
        llAdViewFacebook!!.visibility = View.GONE
    }

    private fun subScribeToFirebaseTopic() {
        try {
            FirebaseMessaging.getInstance().subscribeToTopic("video_cutter_topic")
                .addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Log.e("subScribeFirebaseTopic", ": Fail")
                    } else {
                        Log.e("subScribeFirebaseTopic", ": Success")
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun successCall() {
        if (CommonUtilities.isNetworkConnected(this)) {
            if (CommonConstants.ENABLE_DISABLE == CommonConstants.ENABLE) {
                CommonUtilities.setPref(
                    this@MainActivity,
                    CommonConstants.AD_TYPE_FB_GOOGLE,
                    CommonConstants.AD_TYPE_FACEBOOK_GOOGLE
                )
                CommonUtilities.setPref(
                    this@MainActivity,
                    CommonConstants.FB_BANNER,
                    CommonConstants.FB_BANNER_ID
                )
                CommonUtilities.setPref(
                    this@MainActivity,
                    CommonConstants.FB_INTERSTITIAL,
                    CommonConstants.FB_INTERSTITIAL_ID
                )
                CommonUtilities.setPref(
                    this@MainActivity,
                    CommonConstants.GOOGLE_BANNER,
                    CommonConstants.GOOGLE_BANNER_ID
                )
                CommonUtilities.setPref(
                    this@MainActivity,
                    CommonConstants.GOOGLE_INTERSTITIAL,
                    CommonConstants.GOOGLE_INTERSTITIAL_ID
                )
                CommonUtilities.setPref(
                    this@MainActivity,
                    CommonConstants.STATUS_ENABLE_DISABLE,
                    CommonConstants.ENABLE_DISABLE
                )
                setAppAdId(CommonConstants.GOOGLE_ADMOB_APP_ID)
            } else {
                CommonUtilities.setPref(
                    this@MainActivity,
                    CommonConstants.STATUS_ENABLE_DISABLE,
                    CommonConstants.ENABLE_DISABLE
                )
            }
        } else {
            CommonUtilities.openInternetDialog(this, this, true)
        }
    }

    fun setAppAdId(id: String?) {
        try {
            val applicationInfo =
                packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            val bundle = applicationInfo.metaData
            val beforeChangeId = bundle.getString("com.google.android.gms.ads.APPLICATION_ID")
            Log.e("TAG", "setAppAdId:BeforeChange:::::  $beforeChangeId")
            applicationInfo.metaData.putString("com.google.android.gms.ads.APPLICATION_ID", id)
            val AfterChangeId = bundle.getString("com.google.android.gms.ads.APPLICATION_ID")
            Log.e("TAG", "setAppAdId:AfterChange::::  $AfterChangeId")
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
    }

    fun initViews() {
        videoClassArrayList = ArrayList<VideoClass>()
        rvVideoList = findViewById(R.id.rvVideoListActMain)
        rvVideoList!!.layoutManager = GridLayoutManager(this, 3, GridLayoutManager.VERTICAL, false)
        llPermission = findViewById(R.id.llPermissionActMain)
        tvAllow = findViewById(R.id.tvAllowActMain)
        imgDrawer = findViewById(R.id.imgDrawerActMain)
        imgGridList = findViewById(R.id.imgGridListActMain)
        llNoData = findViewById(R.id.llNoDataActMain)
        gridVideoAdapter = GridVideoAdapter(this, videoClassArrayList!!, this@MainActivity)
        rvVideoList!!.adapter = gridVideoAdapter
        listVideoAdapter = ListVideoAdapter(this, videoClassArrayList!!, this@MainActivity)
        rvVideoList!!.adapter = listVideoAdapter
        tvAllow!!.setOnClickListener(this)
        imgDrawer!!.setOnClickListener(this)
        imgGridList!!.setOnClickListener(this)
    }

    private fun checkPermissionAndLoadVideos() {
        if (CommonUtilities.checkRequiredPermission(this)) {
            rvVideoList!!.visibility = View.VISIBLE
            llPermission!!.visibility = View.GONE
            LoadVideosFromStorage(this).execute()
        } else {
            rvVideoList!!.visibility = View.GONE
            llPermission!!.visibility = View.VISIBLE
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.imgDrawerActMain -> openDrawer()
            R.id.tvAllowActMain -> CommonUtilities.requestRequiredPermission(this@MainActivity)
            R.id.imgGridListActMain -> {
                isGridView = !isGridView
                bindDataInGridListFormat(isGridView)
            }
        }
    }

    @SuppressLint("WrongConstant")
    fun openDrawer() {
        drawerLayout!!.openDrawer(Gravity.START)
    }

    override fun onSuccess() {}
    override fun onCancel() {}
    override fun onRetry() {}

    @SuppressLint("StaticFieldLeak")
    inner class LoadVideosFromStorage(private val context: Context) :
        AsyncTask<Void?, Void?, Void?>() {
        private var mProgressDialog: ProgressDialog? = null
        override fun onPreExecute() {
            super.onPreExecute()
            mProgressDialog = ProgressDialog(context)
            mProgressDialog!!.setCancelable(false)
            mProgressDialog!!.setMessage(getString(R.string.trimming_progress))
            mProgressDialog!!.show()
            videoClassArrayList = ArrayList<VideoClass>()
        }

        override fun doInBackground(vararg voids: Void?): Void? {
            var stringArrayList: ArrayList<String?>? = ArrayList()
            val videoItemHashSet = HashSet<String?>()
            val projection =
                arrayOf(MediaStore.Video.VideoColumns.DATA, MediaStore.Video.Media.DISPLAY_NAME)
            val cursor = contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                MediaStore.Video.Media.DISPLAY_NAME + " ASC"
            )
            try {
                if (cursor != null && cursor.count > 0) {
                    while (cursor.moveToNext()) {
                        val filePath =
                            cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA))
                        if (!filePath.contains(resources.getString(R.string.app_name))) {
                            try {
                                val file = File(filePath)
                                if (file.exists() && file.isFile) {
                                    videoItemHashSet.add(filePath)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (cursor != null && !cursor.isClosed) {
                    cursor.close()
                }
            }
            try {
                stringArrayList = ArrayList(videoItemHashSet)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            videoClassArrayList =
                CommonUtilities.getVideoListWithDetails(context, stringArrayList!!)
            return null
        }

        override fun onPostExecute(aVoid: Void?) {
            super.onPostExecute(aVoid)
            mProgressDialog!!.cancel()
            if (videoClassArrayList!!.size > 0) {
                llNoData!!.visibility = View.GONE
                bindDataInGridListFormat(isGridView)
            } else {
                llNoData!!.visibility = View.VISIBLE
                Toast.makeText(context, "No records found.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun bindDataInGridListFormat(isGridView: Boolean) {
        if (isGridView) {
            rvVideoList!!.layoutManager =
                GridLayoutManager(this, 3, GridLayoutManager.VERTICAL, false)
            gridVideoAdapter = GridVideoAdapter(this, videoClassArrayList!!, this@MainActivity)
            rvVideoList!!.adapter = gridVideoAdapter
            imgGridList!!.setImageResource(R.mipmap.ic_menu_more_app)
        } else {
            rvVideoList!!.layoutManager =
                LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            listVideoAdapter = ListVideoAdapter(this, videoClassArrayList!!, this@MainActivity)
            rvVideoList!!.adapter = listVideoAdapter
            imgGridList!!.setImageResource(R.mipmap.baseline_grid_on_white_24)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CommonConstants.RequestCodePermission -> if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                rvVideoList!!.visibility = View.VISIBLE
                llPermission!!.visibility = View.GONE
                LoadVideosFromStorage(this).execute()
            } else {
                CommonUtilities.showPermissionConfirmDialog(this)
            }
        }
    }

    override fun onItemTypeClickCallback(mType: Int, mPos: Int) {
        val aClass: VideoClass = videoClassArrayList!![mPos]
        if (mType == 0) {
            if (aClass.TrimDuration > 10 * 1000) {
                Log.e("TAG", "onItemTypeClickCallback:::: " + aClass.TrimDuration)
                startTrimActivity(aClass)
            } else {
                Toast.makeText(context, "Video duration is less then 10 second", Toast.LENGTH_SHORT)
                    .show()
            }
        } else if (mType == 1) {
            openEditCatNameDialog(this@MainActivity, aClass)
        } else if (mType == 2) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    openConfirmDeleteDialog(this@MainActivity, aClass.VideoPath, mPos)
                } else {
                    CommonUtilities.showSettingsDialog(
                        true,
                        getString(R.string.allow_title),
                        getString(R.string.you_need_to),
                        this@MainActivity
                    )
                }
            } else {
                CommonUtilities.checkPermission(
                    this@MainActivity,
                    this@MainActivity,
                    aClass.VideoPath,
                    mPos
                )
            }
            //            openConfirmDeleteDialog(MainActivity.this, aClass.getVideoPath(), mPos);
        } else if (mType == 3) {
            openShareIntent(aClass)
        }
    }

    override fun PermissionGrant(view: String?, pos: Int) {
        openConfirmDeleteDialog(this@MainActivity, view, pos)
    }

    private fun startTrimActivity(aClass: VideoClass) {
        val intent = Intent(this, TrimmerActivity::class.java)
        //intent.putExtra(CommonConstants.EXTRA_VIDEO_PATH, CommonUtilities.getFilePath(this, uri));
        intent.putExtra(CommonConstants.EXTRA_VIDEO_PATH, aClass.VideoPath)
        intent.putExtra(CommonConstants.VIDEO_TOTAL_DURATION, aClass.TrimDuration)
        Log.e("TAG", "startTrimActivity::::Class:::  " + aClass.VideoPath)
        startActivity(intent)
    }

    fun openEditCatNameDialog(context: Context?, aClass: VideoClass) {
        val margin = resources.getDimension(R.dimen.twenty_dp).toInt()
        val alertDialog = AlertDialog.Builder(
            context!!
        )
        alertDialog.setTitle(CommonConstants.CapMessage)
        alertDialog.setMessage(CommonConstants.MsgRenameFile)
        val linearLayout = LinearLayout(context)
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(margin, 0, margin, 0)
        val etCatName = AppCompatEditText(context)
        etCatName.setText(aClass.FileName)
        etCatName.layoutParams = layoutParams
        linearLayout.addView(etCatName)
        alertDialog.setView(linearLayout)
        alertDialog.setPositiveButton(CommonConstants.CapRename,
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
                        //                        File oldFile1 = new File(aClass.getVideoPath());
                        //                        Uri oldUri1 = Uri.fromFile(new File(aClass.getVideoPath())).toString();
                        //                        File newFile1 = new File(aClass.getVideoPath().replace(aClass.getFileName(), newCategoryName));
                        //                        Uri newUri1 = Uri.fromFile(new File(aClass.getVideoPath().replace(aClass.getFileName(), newCategoryName)));
                        val path1: String = CommonUtilities.getFilePath(
                            context,
                            Uri.fromFile(File(aClass.VideoPath))
                        )!!
                        val path2: String = CommonUtilities.getFilePath(
                            context,
                            Uri.fromFile(
                                File(
                                    aClass.VideoPath
                                        .replace(aClass.FileName, newCategoryName)
                                )
                            )
                        )!!
                        if (path1 != null && path2 != null) {
                            val oldFile1 = File(path1)
                            val newFile1 = File(path2)
                            val oldFile2 = File(oldFile1.parent, aClass.FileName)
                            val newFile2 = File(newFile1.parent, newCategoryName)
                            try {
                                if (oldFile2.renameTo(newFile2)) {
                                    Log.e("<><>", "Rename successful")
                                    Toast.makeText(
                                        context,
                                        CommonConstants.MsgFileRenameSuccessfully,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Log.e("<><>", "Rename failed")
                                    Toast.makeText(
                                        context,
                                        CommonConstants.MsgSomethingWrong,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            dialog.dismiss()
                            LoadVideosFromStorage(context).execute()
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

    fun openConfirmDeleteDialog(context: Context?, videoPath: String?, mPos: Int) {
        val alertDialog = AlertDialog.Builder(
            context!!
        )
        alertDialog.setTitle(CommonConstants.CapConfirm)
        alertDialog.setMessage(CommonConstants.MsgDoYouWantToDelete)
        alertDialog.setPositiveButton(CommonConstants.CapDelete,
            DialogInterface.OnClickListener { dialog, which ->
                if (CommonUtilities.deleteFileFromStorage(context, videoPath)) {
                    try {
                        videoClassArrayList!!.removeAt(mPos)
                        if (isGridView) {
                            gridVideoAdapter!!.notifyDataSetChanged()
                        } else {
                            listVideoAdapter!!.notifyDataSetChanged()
                        }
                        Toast.makeText(
                            context,
                            CommonConstants.MsgFileDeletedSuccessfully,
                            Toast.LENGTH_SHORT
                        ).show()
                        val scrollPos =
                            if (videoClassArrayList!!.size != 0) if (videoClassArrayList!!.size > mPos) mPos else mPos - 1 else -1
                        if (scrollPos != -1) {
                            rvVideoList!!.layoutManager!!.scrollToPosition(scrollPos)
                        }
                        LoadVideosFromStorage(this@MainActivity).execute()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    Toast.makeText(context, CommonConstants.MsgSomethingWrong, Toast.LENGTH_SHORT)
                        .show()
                }
                dialog.dismiss()
            })
        alertDialog.setNegativeButton(CommonConstants.CapCancel,
            DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() })
        alertDialog.show()
    }

    fun openShareIntent(aClass: VideoClass) {
        try {
            val link = "https://play.google.com/store/apps/details?id=$packageName"
            val intent = Intent()
            intent.action = Intent.ACTION_SEND
            intent.type = "video/*"
            intent.putExtra(Intent.EXTRA_TEXT, link)
            intent.putExtra(Intent.EXTRA_TITLE, link)
            intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(aClass.VideoPath))
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val key1: String = CommonConstants.KeyIsDataUpdated
        if (resultCode == RESULT_OK) {
            if (requestCode == CommonConstants.RequestDataUpdate) {
                if (data != null && data.hasExtra(key1)) {
                    if (data.hasExtra(key1)) {
                        val isDataUpdated = data.getBooleanExtra(key1, false)
                        if (isDataUpdated) {
                            LoadVideosFromStorage(this@MainActivity).execute()
                        }
                    }
                }
            }
        }
    }
}
