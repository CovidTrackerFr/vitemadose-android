package com.cvtracker.vmd.about

import com.cvtracker.vmd.data.DisplayStat
import com.cvtracker.vmd.master.DataManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

class AboutPresenter(private val view: AboutContract.View) : AboutContract.Presenter, CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Main + Job()

    override fun loadStats() {
        launch(Dispatchers.Main) {
            try {
                view.showStats(DisplayStat.from(DataManager.getStats()))
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }
}