package com.cvtracker.vmd.bookmark

import com.cvtracker.vmd.base.CenterContract

interface BookmarkContract {

    interface View: CenterContract.View{

        /**
         * Notify no bookmark saved
         */
        fun showNoBookmark()
    }

    interface Presenter: CenterContract.Presenter {

        /**
         * Load centers for the saved bookmarks
         */
        fun loadBookmarks()
    }
}