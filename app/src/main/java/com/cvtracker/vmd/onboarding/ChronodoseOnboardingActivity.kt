package com.cvtracker.vmd.onboarding

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.cvtracker.vmd.R
import com.cvtracker.vmd.data.DisplayItem
import com.cvtracker.vmd.extensions.colorAttr
import com.cvtracker.vmd.extensions.hide
import com.cvtracker.vmd.master.PrefHelper
import kotlinx.android.synthetic.main.activity_about.toolbar
import kotlinx.android.synthetic.main.activity_chronodose_onboarding.*
import java.util.*

class ChronodoseOnboardingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chronodose_onboarding)
        window.setBackgroundDrawable(ColorDrawable(colorAttr(R.attr.backgroundColor)))

        toolbar.setTitle(R.string.chronodose_onboarding_title)
        setSupportActionBar(toolbar)
        val isActivityOpenedFromAboutScreen = intent?.extras?.get("FROM_ABOUT")
        if(isActivityOpenedFromAboutScreen == true){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            continueAfterOnBoarding.hide()
        } else {
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
        }

        PrefHelper.chronodoseOnboardingDisplayed = true
        continueAfterOnBoarding.setOnClickListener {
            onBackPressed()
        }
    }


    //Only used when activity is displayed from About activity
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        private val fakeCenter = DisplayItem.Center(
                department = "35",
                name = "Centre de vaccination COVID",
                url = "http",
                platform = "Doctolib",
                metadata = DisplayItem.Center.Metadata(
                        address = "1 Rue Georges Clemenceau, 35400 Saint-Malo",
                        businessHours = null,
                        phoneNumber = null
                ),
                location = null,
                nextSlot = Date(Date().time + 12 * 60 * 60 * 1000),
                appointmentCount = 30,
                type = "",
                id = "",
                vaccineType = listOf("Pfizer-BioNTech"),
                schedules = listOf(DisplayItem.Center.Schedule("chronodose", 4)),
                available = true
        )
    }
}