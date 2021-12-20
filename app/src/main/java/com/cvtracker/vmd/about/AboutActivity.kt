package com.cvtracker.vmd.about

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import com.cvtracker.vmd.R
import com.cvtracker.vmd.contributor.ContributorBottomSheet
import com.cvtracker.vmd.data.DisplayStat
import com.cvtracker.vmd.extensions.launchWebUrl
import com.cvtracker.vmd.extensions.show
import com.cvtracker.vmd.master.AbstractVMDActivity
import com.cvtracker.vmd.master.PrefHelper
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_about.*


class AboutActivity : AbstractVMDActivity(), AboutContract.View {

    private val presenter: AboutContract.Presenter = AboutPresenter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        toolbar.setTitle(R.string.about)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        newSystem.isChecked = PrefHelper.isNewSystem
        newSystem.setOnCheckedChangeListener { _, isChecked ->
            PrefHelper.isNewSystem = isChecked
            setResult(RESULT_OK)
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

        ossView.setOnClickListener {
            startActivity(Intent(this, OssLicensesMenuActivity::class.java))
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

        contributorsView.setOnClickListener {
            ContributorBottomSheet.newInstance().show(supportFragmentManager, ContributorBottomSheet.TAG)
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
