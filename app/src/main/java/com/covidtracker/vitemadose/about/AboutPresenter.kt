package com.covidtracker.vitemadose.about

import com.covidtracker.vitemadose.data.DisplayStat
import com.covidtracker.vitemadose.master.DataManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber

class AboutPresenter(private val view: AboutContract.View) : AboutContract.Presenter {

    override fun loadStats() {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                view.showStats(DisplayStat.from(DataManager.getStats()))
            } catch (e: Exception) {
                e.printStackTrace()
                Timber.e(e)
            }
        }
    }
}