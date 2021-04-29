package com.cvtracker.vmd.bookmark

import com.cvtracker.vmd.data.Bookmark
import com.cvtracker.vmd.data.DisplayItem

interface BookmarkContract {

    interface View {

        /**
         * Display bookmark list of centers
         */
        fun showCenters(list: List<DisplayItem>)

        /**
         * Notify centers are loading
         */
        fun setLoading(loading: Boolean)

        /**
         * Notify an error occurs while retrieving centers
         */
        fun showCentersError()

        /**
         * Notify no bookmark saved
         */
        fun showNoBookmark()
    }

    interface Presenter {

        /**
         * Load centers for the saved bookmarks
         */
        fun loadBookmarks()

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