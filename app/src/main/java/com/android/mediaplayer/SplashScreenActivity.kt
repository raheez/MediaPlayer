package com.android.mediaplayer

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import com.android.mediaplayer.databinding.SplashScreenActivityBinding
import android.content.Intent
import android.R
import android.os.Handler
import android.view.WindowManager


class SplashScreenActivity :AppCompatActivity(){

    lateinit var  mBinding : SplashScreenActivityBinding
    private val SPLASH_SCREEN_TIME_OUT = 2000


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = SplashScreenActivityBinding.inflate(layoutInflater)
        setContentView(mBinding?.root)

        handleSplashEffect()
    }

    private fun handleSplashEffect() {

        val mVersionNo =  BuildConfig.VERSION_NAME;
        mBinding?.versionTitle?.setText("version ${mVersionNo}")

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        Handler().postDelayed(Runnable {
            val i = Intent(this,MainActivity::class.java)
            startActivity(i)
            finish()
        }, SPLASH_SCREEN_TIME_OUT.toLong())
    }
}