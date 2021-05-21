package com.cvtracker.vmd.onboarding

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.MenuItem
import com.cvtracker.vmd.R
import com.cvtracker.vmd.extensions.colorAttr
import com.cvtracker.vmd.extensions.hide
import com.cvtracker.vmd.master.AbstractVMDActivity
import com.cvtracker.vmd.master.PrefHelper
import kotlinx.android.synthetic.main.activity_about.toolbar
import kotlinx.android.synthetic.main.activity_chronodose_onboarding.*

class ChronodoseOnboardingActivity : AbstractVMDActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chronodose_onboarding)
        window.setBackgroundDrawable(ColorDrawable(colorAttr(R.attr.backgroundColor)))

        setSupportActionBar(toolbar)
        val isActivityOpenedFromAboutScreen = intent?.extras?.get("FROM_ABOUT")
        if(isActivityOpenedFromAboutScreen == true){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            toolbar.setTitle(R.string.chronodose_onboarding_title)
            continueAfterOnBoarding.hide()
        } else {
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
            toolbar.setTitle(R.string.before_going_further)
        }
        continueAfterOnBoarding.setOnClickListener {
            onBackPressed()
        }

        PrefHelper.chronodoseOnboardingDisplayed = true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}