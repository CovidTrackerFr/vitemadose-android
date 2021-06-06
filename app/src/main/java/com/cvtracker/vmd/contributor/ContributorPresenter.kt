package com.cvtracker.vmd.contributor

import com.cvtracker.vmd.master.DataManager
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class ContributorPresenter(
    override val view: ContributorContract.View
) : ContributorContract.Presenter, CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Main + Job()
    private var job: Job? = null

    override fun loadContributors() {
        view.showLoading()
        job = launch(Dispatchers.Main) {
            val contributors = withContext(Dispatchers.IO) { DataManager.getContributors() }
            view.hideLoading()
            view.showContributor(contributors)
        }
    }

    override fun cancel() {
        job?.cancel()
    }
}