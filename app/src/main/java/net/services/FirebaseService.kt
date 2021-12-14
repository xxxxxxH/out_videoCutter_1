package net.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.res.Resources.NotFoundException
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.text.format.DateUtils
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import net.basicmodel.R
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL

class FirebaseService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        generateNotification(remoteMessage)
    }

    private fun generateNotification(remoteMessage: RemoteMessage) {
        try {
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val mIntent: Intent? = null
            val channelId = "11111"
            try {
                val channelName = resources.getString(R.string.app_name)
                val channelDescription = "Application_name Alert"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val importance = NotificationManager.IMPORTANCE_HIGH
                    val notificationChannel =
                        NotificationChannel(channelId, channelName, importance)
                    notificationChannel.description = channelDescription
                    notificationChannel.setSound(
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                        notificationChannel.audioAttributes
                    )
                    notificationManager.createNotificationChannel(notificationChannel)
                }
            } catch (e: NotFoundException) {
                e.printStackTrace()
            }
            val builder = NotificationCompat.Builder(this, channelId)
            val expandedView = RemoteViews(packageName, R.layout.item_notification_expand)
            val collapsedView = RemoteViews(packageName, R.layout.item_notification_coll)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder.setSmallIcon(R.mipmap.notification_icon)
                builder.color = ContextCompat.getColor(this, R.color.colorBlack)
                expandedView.setImageViewResource(R.id.big_icon, R.mipmap.ic_launcher)
                expandedView.setTextViewText(
                    R.id.timestamp,
                    DateUtils.formatDateTime(
                        this,
                        System.currentTimeMillis(),
                        DateUtils.FORMAT_SHOW_TIME
                    )
                )
                collapsedView.setImageViewResource(R.id.big_icon, R.mipmap.ic_launcher)
                collapsedView.setTextViewText(
                    R.id.timestamp,
                    DateUtils.formatDateTime(
                        this,
                        System.currentTimeMillis(),
                        DateUtils.FORMAT_SHOW_TIME
                    )
                )
            } else {
                builder.setSmallIcon(R.mipmap.notification_icon)
                expandedView.setImageViewResource(R.id.big_icon, R.mipmap.ic_launcher)
                expandedView.setTextViewText(
                    R.id.timestamp,
                    DateUtils.formatDateTime(
                        this,
                        System.currentTimeMillis(),
                        DateUtils.FORMAT_SHOW_TIME
                    )
                )
                collapsedView.setImageViewResource(R.id.big_icon, R.mipmap.ic_launcher)
                collapsedView.setTextViewText(
                    R.id.timestamp,
                    DateUtils.formatDateTime(
                        this,
                        System.currentTimeMillis(),
                        DateUtils.FORMAT_SHOW_TIME
                    )
                )
            }
            val title = resources.getString(R.string.app_name)
            val data = remoteMessage.notification
            if (data != null) {
                mIntent!!.putExtra("notificationData", Gson().toJson(remoteMessage.data))
                Log.e("TAG", "generateNotification:::INTENT:::::  \${Gson().toJson(p0.data)}  ")
                mIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                try {
                    expandedView.setTextViewText(R.id.title_text, data.title)
                    expandedView.setTextViewText(R.id.notification_message, data.body)
                    collapsedView.setTextViewText(R.id.content_text, data.body)
                    collapsedView.setTextViewText(R.id.title_text, data.title)
                    val policy = ThreadPolicy.Builder().permitAll().build()
                    StrictMode.setThreadPolicy(policy)
                    val image1Url = URL(data.imageUrl.toString())
                    val bmp1 =
                        BitmapFactory.decodeStream(image1Url.openConnection().getInputStream())
                    if (bmp1 != null) {
                        builder.setStyle(
                            NotificationCompat.BigPictureStyle().bigPicture(bmp1)
                                .setSummaryText(data.body)
                        )
                        expandedView.setBitmap(R.id.notification_img, "setImageBitmap", bmp1)
                    }
                } catch (e: MalformedURLException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } else {
                mIntent!!.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                builder.setContentText(title)
            }
            builder.setCustomContentView(collapsedView)
            builder.setCustomBigContentView(expandedView)
            builder.setShowWhen(false)
            builder.setStyle(NotificationCompat.DecoratedCustomViewStyle())
            builder.priority = NotificationCompat.PRIORITY_HIGH
            builder.setAutoCancel(true)
            builder.setVisibility(NotificationCompat.VISIBILITY_SECRET)
            val l = System.currentTimeMillis()
            val pendingIntent = PendingIntent.getActivity(
                this,
                l.toInt(),
                mIntent,
                PendingIntent.FLAG_CANCEL_CURRENT
            )
            builder.setContentIntent(pendingIntent)
            builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            notificationManager.notify(l.toInt(), builder.build())
        } catch (e: NotFoundException) {
            e.printStackTrace()
        }
    }
}
