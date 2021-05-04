package com.cvtracker.vmd.base

import com.cvtracker.vmd.data.Bookmark
import com.cvtracker.vmd.data.DisplayItem
import com.cvtracker.vmd.master.FilterType

interface CenterContract {

    interface View {

        /**
         * Open link
         */
        fun openLink(url: String)

        /**
         * Display bookmark list of centers
         */
        fun showCenters(list: List<DisplayItem>, filter: FilterType?)

        /**
         * Notify centers are loading
         */
        fun setLoading(loading: Boolean)

        /**
         * Notify an error occurs while retrieving centers
         */
        fun showCentersError()

    }

    interface Presenter {

        /**
         * Called when a center is clicked
         */
        fun onCenterClicked(center: DisplayItem.Center)

        /**
         * Called when bookmark is clicked
         */
        fun onBookmarkClicked(center: DisplayItem.Center, target: Bookmark)
    }
}