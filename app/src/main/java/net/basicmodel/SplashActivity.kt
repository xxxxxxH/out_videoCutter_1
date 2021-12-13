package net.basicmodel

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import com.bumptech.glide.Glide
import net.interfaces.CallbackListener
import net.utils.CommonConstants
import net.utils.CommonUtilities

class SplashActivity : AppCompatActivity(), CallbackListener {
    private var isLoaded = false
    private val handler = Handler()
    private val myRunnable = Runnable {
        if (CommonUtilities.isNetworkConnected(this@SplashActivity)) {
            if (!isLoaded) {
                startNextActivity(0)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_splash)
        initViews()
    }

    private fun initViews() {
        val imgSplash = findViewById<AppCompatImageView>(R.id.imgSplashActSplash)
        Glide.with(this)
            .load(R.drawable.splash)
            .into(imgSplash)
        callApi()
    }

    private fun startNextActivity(time: Int) {
        Handler().postDelayed({
            val mainIntent = Intent(this@SplashActivity, MainActivity::class.java)
            startActivity(mainIntent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            finish()
        }, time.toLong())
    }

    fun callApi() {
        if (CommonUtilities.isNetworkConnected(this)) {
            successCall()
        } else {
            CommonUtilities.openInternetDialog(this, this, true)
        }
        handler.postDelayed(myRunnable, 10000)
    }

    private fun successCall() {
        if (CommonUtilities.getPref(this, CommonConstants.SPLASH_SCREEN_COUNT, 1) === 1) {
            Log.e(
                "TAG",
                "successCall::::IFFFFF " + CommonUtilities.getPref(
                    this,
                    CommonConstants.SPLASH_SCREEN_COUNT,
                    1
                )
            )
            CommonUtilities.setPref(this, CommonConstants.SPLASH_SCREEN_COUNT, 2)
            startNextActivity(1000)
        } else {
            Log.e(
                "TAG",
                "successCall::::ELSEEE " + CommonUtilities.getPref(
                    this,
                    CommonConstants.SPLASH_SCREEN_COUNT,
                    1
                )
            )
            checkAd()
        }
    }

    private fun checkAd() {
        startNextActivity(1000)
    }

    override fun onSuccess() {}
    override fun onCancel() {}
    override fun onRetry() {
        callApi()
    }

    fun adLoadingFailed() {
        startNextActivity(0)
    }

    fun adClose() {
        startNextActivity(0)
    }

    fun startNextScreen() {
        startNextActivity(0)
    }

    fun onLoaded() {
        isLoaded = true
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacks(myRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(myRunnable)
    }
}
