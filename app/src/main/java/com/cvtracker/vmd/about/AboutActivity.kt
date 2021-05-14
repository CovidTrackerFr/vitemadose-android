package com.cvtracker.vmd.about

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.cvtracker.vmd.R
import com.cvtracker.vmd.data.DisplayStat
import com.cvtracker.vmd.extensions.colorAttr
import com.cvtracker.vmd.extensions.launchWebUrl
import com.cvtracker.vmd.extensions.show
import com.cvtracker.vmd.onboarding.ChronodoseOnboardingActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_about.*


class AboutActivity : AppCompatActivity(), AboutContract.View {

    private val presenter: AboutContract.Presenter = AboutPresenter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        window.setBackgroundDrawable(ColorDrawable(colorAttr(R.attr.backgroundColor)))

        toolbar.setTitle(R.string.about)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        chronodoseView.setOnClickListener {
            val chronodoseOnBoardingIntent = Intent(this, ChronodoseOnboardingActivity::class.java)
            chronodoseOnBoardingIntent.putExtra("FROM_ABOUT",true)
            startActivity(chronodoseOnBoardingIntent)
        }

        faqView.setOnClickListener {
            launchWebUrl(URL_FAQ)
        }

        centersMapView.setOnClickListener {
            launchWebUrl(URL_CENTERS_PLACES)
        }

        vaccinTrackerView.setOnClickListener {
            launchWebUrl(URL_VACCINTRACKER)
        }

        shareView.setOnClickListener {
            try {
                startActivity(Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, getString(R.string.share_description))
                })
            }catch (e: ActivityNotFoundException){
                Snackbar.make(
                    container,
                    getString(R.string.no_app_activity_found),
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }

        presenter.loadStats()
    }

    override fun showStats(displayStat: DisplayStat) {
        statFirstLineView.show()
        statSecondLineView.show()
        statHeaderView.show()

        availableCentersStat.bind(displayStat.availableCentersCount)
        centersStat.bind(displayStat.centersCount)
        availableSlotsStat.bind(displayStat.slotsCount)
        fillStat.bind(displayStat.fillRatio)
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

    companion object {
        const val URL_VACCINTRACKER = "https://covidtracker.fr/vaccintracker/"
        const val URL_CENTERS_PLACES = "https://vitemadose.covidtracker.fr/centres"
        const val URL_FAQ = "https://vitemadose.covidtracker.fr/apropos"
    }
}