package com.caravan.caravan.ui.activity

import android.os.Build
import android.os.Bundle
import android.view.WindowInsetsController
import androidx.annotation.RequiresApi
import com.caravan.caravan.R

class MainActivity : BaseActivity() {
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.windowInsetsController?.setSystemBarsAppearance(WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS)
        setContentView(R.layout.activity_main)
    }
}