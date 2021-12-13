package net.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.media.ThumbnailUtils
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.preference.PreferenceManager
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import net.basicmodel.R
import net.entity.VideoClass
import net.interfaces.CallbackListener
import net.interfaces.PermissionCallback
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

object CommonUtilities {
    fun checkRequiredPermission(context: Context?): Boolean {
        val result1 =
            ContextCompat.checkSelfPermission(context!!, Manifest.permission.READ_EXTERNAL_STORAGE)
        val result2 =
            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        return result1 == PackageManager.PERMISSION_GRANTED &&
                result2 == PackageManager.PERMISSION_GRANTED
    }

    fun requestRequiredPermission(appCompatActivity: AppCompatActivity?) {
        ActivityCompat.requestPermissions(
            appCompatActivity!!, arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ), CommonConstants.RequestCodePermission
        )
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun showPermissionConfirmDialog(appCompatActivity: AppCompatActivity) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(appCompatActivity)
            .setTitle(CommonConstants.CapPermission)
            .setMessage(CommonConstants.MsgAllowPermission)
            .setPositiveButton(CommonConstants.CapContinue,
                DialogInterface.OnClickListener { dialog, which ->
                    dialog.dismiss()
                    appCompatActivity.requestPermissions(
                        arrayOf(
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ), CommonConstants.RequestCodePermission
                    )
                })
            .setNegativeButton(CommonConstants.CapCancel,
                DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() })
        builder.show()
    }

    @SuppressLint("NewApi")
    fun getFilePath(context: Context, uri: Uri): String? {
        var uri = uri
        var selection: String? = null
        var selectionArgs: Array<String>? = null
        if (Build.VERSION.SDK_INT >= 19 && DocumentsContract.isDocumentUri(
                context.applicationContext,
                uri
            )
        ) {
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
            } else if (isDownloadsDocument(uri)) {
                val id = DocumentsContract.getDocumentId(uri)
                uri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id)
                )
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                if ("image" == type) {
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                selection = "_id=?"
                selectionArgs = arrayOf(split[1])
            }
        }
        if ("content".equals(uri.scheme, ignoreCase = true)) {
            val projection = arrayOf(
                MediaStore.Images.Media.DATA
            )
            var cursor: Cursor? = null
            try {
                if (context.contentResolver != null) {
                    cursor = context.contentResolver.query(
                        uri,
                        projection,
                        selection,
                        selectionArgs,
                        null
                    )
                    if (cursor != null) {
                        val column_index =
                            cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                        if (cursor.moveToFirst()) {
                            return cursor.getString(column_index)
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
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        return null
    }

    fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    fun getVideoListWithDetails(
        context: Context,
        stringArrayList: ArrayList<String?>
    ): ArrayList<VideoClass> {
        val videoClassArrayList: ArrayList<VideoClass> = ArrayList<VideoClass>()
        for (i in stringArrayList.indices) {
            val videoPath = stringArrayList[i]
            val aClass = VideoClass()
            aClass.VideoPath = videoPath!!
            try {
                aClass.DisplayDuration = getDisplayVideoDuration(context, videoPath)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                aClass.TrimDuration = getTrimVideoDuration(context, videoPath)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                aClass.FileName = getFileName(videoPath)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                aClass.FileSize = getFileSize(videoPath)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                aClass.ThumbBitmap = getVideoMiniThumb(videoPath)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            videoClassArrayList.add(aClass)
        }
        return videoClassArrayList
    }

    private fun getDisplayVideoDuration(context: Context, videoPath: String?): String {
        var finalData = "00:00:00"
        var duration = 0
        var uri1: Uri? = null
        try {
            val file = File(videoPath)
            uri1 = Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            if (uri1 != null) {
                val mp = MediaPlayer.create(context, uri1)
                duration = mp.duration
                mp.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            if (duration != 0) {
                finalData = String.format(
                    Locale.ENGLISH,
                    "%02d:%02d:%02d",
                    TimeUnit.MILLISECONDS.toHours(duration.toLong()),
                    TimeUnit.MILLISECONDS.toMinutes(duration.toLong()) - TimeUnit.HOURS.toMinutes(
                        TimeUnit.MILLISECONDS.toHours(duration.toLong())
                    ),
                    TimeUnit.MILLISECONDS.toSeconds(duration.toLong()) - TimeUnit.MINUTES.toSeconds(
                        TimeUnit.MILLISECONDS.toMinutes(duration.toLong())
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return finalData
    }

    private fun getTrimVideoDuration(context: Context, videoPath: String?): Int {
        var duration = 0
        var uri1: Uri? = null
        try {
            val file = File(videoPath)
            uri1 = Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            if (uri1 != null) {
                val mp = MediaPlayer.create(context, uri1)
                duration = mp.duration
                mp.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return duration
    }

    private fun getFileName(filePath: String?): String {
        val file = File(filePath)
        return file.name
    }

    private fun getFileSize(filePath: String?): String {
        val file = File(filePath)
        val fileSizeInBytes = file.length()
        val fileSizeInKB = fileSizeInBytes / 1024
        val fileSizeInMB = fileSizeInKB / 1024
        return fileSizeInMB.toString()
    }

    private fun getVideoMiniThumb(videoPath: String?): Bitmap? {
        var videoThumbnail: Bitmap? = null
        try {
            videoThumbnail = ThumbnailUtils.createVideoThumbnail(
                videoPath!!,
                MediaStore.Video.Thumbnails.MINI_KIND
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return videoThumbnail
    }

    fun deleteFileFromStorage(context: Context, filePath: String?): Boolean {
        var isDeleted = false
        try {
            val file = File(filePath)
            if (file.delete()) {
                isDeleted = true
                Log.e("<><>", " file deleted")
            }
            if (file.exists()) {
                try {
                    if (file.canonicalFile.delete()) {
                        isDeleted = true
                        Log.e("<><>", " file deleted")
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                if (file.exists()) {
                    try {
                        if (context.deleteFile(file.name)) {
                            isDeleted = true
                            Log.e("<><>", " file deleted")
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return isDeleted
    }

    fun checkPermission(
        context: Context,
        permissionCallback: PermissionCallback,
        name: String?,
        pos: Int
    ) {
        Dexter.withActivity(context as Activity)
            .withPermissions(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (report.areAllPermissionsGranted()) {
                        permissionCallback.PermissionGrant(name, pos)
                    } else if (report.isAnyPermissionPermanentlyDenied) {
                        showSettingsDialog(
                            false,
                            context.getString(R.string.need_permission),
                            context.getString(R.string.permision_message),
                            context
                        )
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest>,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            }).check()
    }

    fun showSettingsDialog(isAllFile: Boolean, title: String?, message: String?, context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton(
            "Go to setting"
        ) { dialog, which ->
            if (isAllFile) {
                val permissionIntent =
                    Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                context.startActivity(permissionIntent)
            } else {
                openSettings(context)
            }
        }
        builder.setNegativeButton(
            "Cancel"
        ) { dialog, which -> dialog.dismiss() }
        builder.show()
    }

    private fun openSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", context.packageName, null)
        intent.data = uri
        (context as Activity).startActivityForResult(intent, 101)
    }

    fun setPref(context: Context?, key: String?, value: String?) {
        val editor =
            PreferenceManager.getDefaultSharedPreferences(context).edit().putString(key, value)
        editor.apply()
    }

    fun getPref(context: Context?, key: String?, value: String?): String? {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(key, value)
    }

    fun setPref(context: Context?, key: String?, value: Int?) {
        val editor = PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(
            key,
            value!!
        )
        editor.apply()
    }

    fun getPref(context: Context?, key: String?, value: Int?): Int {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(key, value!!)
    }

    fun isNetworkConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo != null && cm.activeNetworkInfo!!.isConnected
    }

    fun openInternetDialog(
        context: Context,
        callbackListener: CallbackListener,
        isSplash: Boolean?
    ) {
        if (!isNetworkConnected(context)) {
            val builder = android.app.AlertDialog.Builder(context)
            builder.setTitle("No internet Connection")
            builder.setCancelable(false)
            builder.setMessage("Please turn on internet connection to continue")
            builder.setNegativeButton(
                "Retry"
            ) { dialog, which ->
                if (!isSplash!!) {
                    openInternetDialog(context, callbackListener, false)
                }
                dialog.dismiss()
                callbackListener.onRetry()
            }
            builder.setPositiveButton(
                "Close"
            ) { dialog, which ->
                dialog.dismiss()
                val homeIntent = Intent(Intent.ACTION_MAIN)
                homeIntent.addCategory(Intent.CATEGORY_HOME)
                homeIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                context.startActivity(homeIntent)
                (context as Activity).finishAffinity()
            }
            val alertDialog = builder.create()
            alertDialog.show()
        }
    }
}