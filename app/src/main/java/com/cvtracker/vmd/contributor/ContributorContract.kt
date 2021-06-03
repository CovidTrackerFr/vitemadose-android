package com.cvtracker.vmd.contributor

import com.cvtracker.vmd.data.Contributor

interface ContributorContract {
    interface View {
        fun showLoading()

        fun hideLoading()

        fun showContributor(contributors: List<Contributor>)
    }

    interface Presenter {
        fun loadContributors()
    }
}