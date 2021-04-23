package com.cvtracker.vmd.about

import com.cvtracker.vmd.data.DisplayStat
import com.cvtracker.vmd.master.DataManager
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
                Timber.e(e)
            }
        }
    }
}