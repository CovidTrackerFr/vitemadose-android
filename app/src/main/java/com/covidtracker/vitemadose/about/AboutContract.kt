package com.covidtracker.vitemadose.about

import com.covidtracker.vitemadose.data.DisplayStat

interface AboutContract {

    interface View {
        fun showStats(displayStat: DisplayStat)
    }

    interface Presenter {
        fun loadStats()
    }
}