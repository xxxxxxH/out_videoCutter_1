package net.basicmodel

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.BaseAdapter
import android.widget.FrameLayout
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.drawerlayout.widget.DrawerLayout
import net.utils.CommonConstants
import net.utils.CommonUtilities
import java.util.*

open class BaseActivity : AppCompatActivity(), OnItemClickListener {
    var context: Context? = null
    var drawerLayout: DrawerLayout? = null
    override fun setContentView(layoutResID: Int) {
        drawerLayout = layoutInflater.inflate(R.layout.activity_base, null) as DrawerLayout
        val activityContainer = drawerLayout!!.findViewById<FrameLayout>(R.id.activity_content)
        layoutInflater.inflate(layoutResID, activityContainer, true)
        super.setContentView(drawerLayout)
        context = this
        initDefine_base()
    }

    var arrDrawerItem: ArrayList<String>? = null
    var arrDrawerImg: ArrayList<Int>? = null
    fun initDefine_base() {
        left_drawer_list_view = findViewById(R.id.left_drawer_list_view)
        arrDrawerItem = ArrayList()
        arrDrawerItem!!.add("Home")
        arrDrawerItem!!.add("My Cuttings")
        arrDrawerItem!!.add("Contact Us")
        arrDrawerItem!!.add("Rate Us")
        arrDrawerItem!!.add("Share App")
        //        arrDrawerItem.add("More App");
        arrDrawerImg = ArrayList()
        arrDrawerImg!!.add(R.mipmap.ic_menu_home)
        arrDrawerImg!!.add(R.mipmap.baseline_folder_white_24)
        arrDrawerImg!!.add(R.mipmap.ic_menu_contact_us)
        arrDrawerImg!!.add(R.mipmap.ic_menu_app_rate)
        arrDrawerImg!!.add(R.mipmap.ic_menu_share_app)
        //        arrDrawerImg.add(R.drawable.ic_menu_more_app);
        setListViewAdapterOfMenu()
        //left_drawer_list_view.setOnItemClickListener(this);
        left_drawer_list_view!!.setOnItemClickListener(OnItemClickListener { parent, view, position, id ->
            if (arrDrawerItem!![position] == "Home") {
                drawerLayout!!.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            } else if (arrDrawerItem!![position] == "My Cuttings") {
                drawerLayout!!.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                if (CommonUtilities.checkRequiredPermission(this@BaseActivity)) {
                    startActivityForResult(
                        Intent(
                            this@BaseActivity,
                            MyCuttingsActivity::class.java
                        ), CommonConstants.RequestDataUpdate
                    )
                } else {
                    CommonUtilities.requestRequiredPermission(this@BaseActivity)
                }
            } else if (arrDrawerItem!![position] == "Contact Us") {
                drawerLayout!!.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                ContactUs()
            } else if (arrDrawerItem!![position] == "Rate Us") {
                drawerLayout!!.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                RateUs()
            } else if (arrDrawerItem!![position] == "Share App") {
                drawerLayout!!.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                ShareAppLink()
            } else if (arrDrawerItem!![position] == "More App") {
                drawerLayout!!.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                MoreApp()
            }
        })
    }

    var left_drawer_list_view: ListView? = null
    var menuAdapter: MenuAdapter? = null
    fun setListViewAdapterOfMenu() {
        menuAdapter = MenuAdapter()
        left_drawer_list_view!!.adapter = menuAdapter
    }

     inner class MenuAdapter : BaseAdapter() {
        override fun getCount(): Int {
            return arrDrawerItem!!.size
        }

        override fun getItem(position: Int): Any {
            return position
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View, parent: ViewGroup): View {
            var convertView = convertView
            convertView = LayoutInflater.from(context).inflate(R.layout.cell_drawer_item, null)
            val imgItem: AppCompatImageView = convertView.findViewById(R.id.imgItem)
            val txtItem: AppCompatTextView = convertView.findViewById(R.id.txtItem)
            imgItem.setImageResource(arrDrawerImg!![position])
            txtItem.text = arrDrawerItem!![position]
            return convertView
        }
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View, position: Int, id: Long) {}

    /* public void ContactUs() {
        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        emailIntent.setType("image/jpeg");
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"kaushalpatell60331@gmail.com"});
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getResources().getString(R.string.app_name));
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "");
        emailIntent.putExtra(Intent.EXTRA_STREAM, "");
        emailIntent.setPackage("com.google.android.gm");
        try {
            startActivity(emailIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
*/
    fun ContactUs() {
        try {
            val sendIntentGmail = Intent(Intent.ACTION_SEND)
            sendIntentGmail.type = "plain/text"
            sendIntentGmail.setPackage("com.google.android.gm")
            sendIntentGmail.putExtra(Intent.EXTRA_EMAIL, arrayOf("Enter your mail"))
            sendIntentGmail.putExtra(
                Intent.EXTRA_SUBJECT,
                resources.getString(R.string.app_name) + " - Android"
            )
            startActivity(sendIntentGmail)
        } catch (e: Exception) {
            val sendIntentIfGmailFail = Intent(Intent.ACTION_SEND)
            sendIntentIfGmailFail.type = "*/*"
            sendIntentIfGmailFail.putExtra(Intent.EXTRA_EMAIL, arrayOf("Enter your mail"))
            sendIntentIfGmailFail.putExtra(
                Intent.EXTRA_SUBJECT,
                resources.getString(R.string.app_name) + " - Android"
            )
            if (sendIntentIfGmailFail.resolveActivity(packageManager) != null) {
                startActivity(sendIntentIfGmailFail)
            }
        }
    }

    fun ShareAppLink() {
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        val link = "https://play.google.com/store/apps/details?id=$packageName"
        shareIntent.putExtra(Intent.EXTRA_TEXT, link)
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, resources.getString(R.string.app_name))
        shareIntent.type = "text/plain"
        startActivity(Intent.createChooser(shareIntent, resources.getString(R.string.app_name)))
    }

    fun RateUs() {
        /* final String appPackageName = getPackageName();
        Uri uri = Uri.parse("https://play.google.com/store/apps/developer?id=" + appPackageName);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);*/
        val appPackageName = packageName
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store")))
        } catch (anfe: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store")))
        }
    }

    fun MoreApp() {
        val appPackageName = packageName
        try {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=$appPackageName")
                )
            )
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
                )
            )
        }
    }
}