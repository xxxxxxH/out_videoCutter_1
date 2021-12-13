package net.basicmodel

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import net.adapter.GridVideoAdapter
import net.adapter.ListVideoAdapter
import net.entity.VideoClass
import net.interfaces.AdapterItemTypeCallback
import net.interfaces.PermissionCallback
import net.utils.CommonConstants
import net.utils.CommonUtilities
import java.io.File
import java.util.*

class MyCuttingsActivity : AppCompatActivity(), AdapterItemTypeCallback, View.OnClickListener,
    PermissionCallback {
    var rvVideoList: RecyclerView? = null
    var llNoData: LinearLayout? = null
    var imgBack: AppCompatImageView? = null
    var imgGridList: AppCompatImageView? = null
    private var gridVideoAdapter: GridVideoAdapter? = null
    private var listVideoAdapter: ListVideoAdapter? = null
    private var videoClassArrayList: ArrayList<VideoClass>? = null
    private var isGridView = true
    private var isDataUpdated = false
    var llAdView: RelativeLayout? = null
    var llAdViewFacebook: LinearLayout? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_cuttings)
        initViews()
        LoadVideosFromStorage(this).execute()
        llAdView = findViewById(R.id.llAdView)
        llAdViewFacebook = findViewById(R.id.llAdViewFacebook)
        llAdView!!.visibility = View.GONE
        llAdViewFacebook!!.visibility = View.GONE
    }

    fun initViews() {
        videoClassArrayList = ArrayList<VideoClass>()
        rvVideoList = findViewById(R.id.rvVideoListActMyVideo)
        rvVideoList!!.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        llNoData = findViewById(R.id.llNoDataActMyVideo)
        imgBack = findViewById(R.id.imgBackActMyVideo)
        imgGridList = findViewById(R.id.imgGridListActMyVideo)
        gridVideoAdapter = GridVideoAdapter(this, videoClassArrayList!!, this@MyCuttingsActivity)
        rvVideoList!!.adapter = gridVideoAdapter
        listVideoAdapter = ListVideoAdapter(this, videoClassArrayList!!, this@MyCuttingsActivity)
        rvVideoList!!.adapter = listVideoAdapter
        imgBack!!.setOnClickListener(this)
        imgGridList!!.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.imgBackActMyVideo -> onBackPressed()
            R.id.imgGridListActMyVideo -> {
                isGridView = !isGridView
                bindDataInGridListFormat(isGridView)
            }
        }
    }

    private fun bindDataInGridListFormat(isGridView: Boolean) {
        if (isGridView) {
            rvVideoList!!.layoutManager =
                GridLayoutManager(this, 3, GridLayoutManager.VERTICAL, false)
            gridVideoAdapter =
                GridVideoAdapter(this, videoClassArrayList!!, this@MyCuttingsActivity)
            rvVideoList!!.adapter = gridVideoAdapter
            imgGridList!!.setImageResource(R.mipmap.ic_menu_more_app)
        } else {
            rvVideoList!!.layoutManager =
                LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            listVideoAdapter =
                ListVideoAdapter(this, videoClassArrayList!!, this@MyCuttingsActivity)
            rvVideoList!!.adapter = listVideoAdapter
            imgGridList!!.setImageResource(R.mipmap.baseline_grid_on_white_24)
        }
    }

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
            var stringArrayList = ArrayList<String?>()
            val folderName = resources.getString(R.string.app_name)
            val pattern = ".mp4"
            try {
                val dir = File(Environment.getExternalStorageDirectory().toString(), folderName)
                val listFile = dir.listFiles()
                if (listFile != null) {
                    for (i in listFile.indices) {
                        if (listFile[i].name.endsWith(pattern)) {
                            val filePath = listFile[i].absolutePath
                            try {
                                val file = File(filePath)
                                if (file.exists() && file.isFile) {
                                    stringArrayList.add(listFile[i].absolutePath)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            Collections.sort(stringArrayList, object : Comparator<String?> {
                override fun compare(filePath1: String?, filePath2: String?): Int {
                    val file1 = File(filePath1)
                    val file2 = File(filePath2)
                    return file1.name.compareTo(file2.name)
                }
            })
            try {
                stringArrayList = ArrayList(stringArrayList)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            videoClassArrayList = CommonUtilities.getVideoListWithDetails(context, stringArrayList)
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

    override fun onItemTypeClickCallback(mType: Int, mPos: Int) {
        try {
            val aClass: VideoClass = videoClassArrayList!![mPos]
            if (mType == 0) {
                startNextActivity(aClass)
            } else if (mType == 1) {
                openEditCatNameDialog(this, aClass)
            } else if (mType == 2) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (Environment.isExternalStorageManager()) {
                        openConfirmDeleteDialog(
                            this@MyCuttingsActivity,
                            aClass.VideoPath,
                            mPos
                        )
                    } else {
                        CommonUtilities.showSettingsDialog(
                            true,
                            getString(R.string.allow_title),
                            getString(R.string.you_need_to),
                            this@MyCuttingsActivity
                        )
                    }
                } else {
                    CommonUtilities.checkPermission(
                        this@MyCuttingsActivity,
                        this@MyCuttingsActivity,
                        aClass.VideoPath,
                        mPos
                    )
                }
            } else if (mType == 3) {
                openShareIntent(aClass)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun PermissionGrant(view: String?, pos: Int) {
        openConfirmDeleteDialog(this@MyCuttingsActivity, view, pos)
    }

    private fun startNextActivity(aClass: VideoClass) {
        val intent = Intent(this, VideoPlayerActivity::class.java)
        intent.putExtra(CommonConstants.KeyVideoPath, aClass.VideoPath)
        startActivity(intent)
    }

    fun openEditCatNameDialog(context: Context, aClass: VideoClass) {
        val margin = resources.getDimension(R.dimen.twenty_dp).toInt()
        val alertDialog = AlertDialog.Builder(context)
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
                        val oldFile: File = File(aClass.FileName)
                        val newFile = File(newCategoryName)
                        try {
                            if (oldFile.renameTo(newFile)) {
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
                } else {
                    Toast.makeText(context, CommonConstants.MsgSomethingWrong, Toast.LENGTH_SHORT)
                        .show()
                }
                dialog.dismiss()
                isDataUpdated = true
                LoadVideosFromStorage(this@MyCuttingsActivity).execute()
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

    override fun onBackPressed() {
        finish()
    }
}