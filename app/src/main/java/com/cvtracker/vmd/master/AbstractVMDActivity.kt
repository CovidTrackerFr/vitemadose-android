package com.cvtracker.vmd.master

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cvtracker.vmd.R

abstract class AbstractVMDActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        if (resources.getBoolean(R.bool.isLarge).not()) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
        }
        super.onCreate(savedInstanceState)
    }
}