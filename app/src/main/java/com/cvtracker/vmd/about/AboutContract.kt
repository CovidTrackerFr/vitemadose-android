package com.cvtracker.vmd.about

import com.cvtracker.vmd.data.DisplayStat

interface AboutContract {

    interface View {
        fun showStats(displayStat: DisplayStat)
    }

    interface Presenter {
        fun loadStats()
    }
}