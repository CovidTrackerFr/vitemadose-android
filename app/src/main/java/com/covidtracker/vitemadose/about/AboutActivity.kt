package com.covidtracker.vitemadose.about

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.covidtracker.vitemadose.R
import com.covidtracker.vitemadose.data.DisplayStat
import com.covidtracker.vitemadose.extensions.color
import com.covidtracker.vitemadose.extensions.launchWebUrl
import com.covidtracker.vitemadose.extensions.show
import kotlinx.android.synthetic.main.activity_about.*

class AboutActivity : AppCompatActivity(), AboutContract.View {

    private val presenter: AboutContract.Presenter = AboutPresenter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        window.setBackgroundDrawable(ColorDrawable(color(R.color.grey_2)))

        toolbar.setTitle(R.string.about)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        faqView.setOnClickListener {
            launchWebUrl(URL_FAQ)
        }

        centersMapView.setOnClickListener {
            launchWebUrl(URL_CENTERS_PLACES)
        }

        vaccinTrackerView.setOnClickListener {
            launchWebUrl(URL_COVIDTRACKER)
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
        const val URL_COVIDTRACKER = "https://covidtracker.fr/vaccintracker/"
        const val URL_CENTERS_PLACES = "https://vitemadose.covidtracker.fr/centres"
        const val URL_FAQ = "https://vitemadose.covidtracker.fr/apropos"
    }
}