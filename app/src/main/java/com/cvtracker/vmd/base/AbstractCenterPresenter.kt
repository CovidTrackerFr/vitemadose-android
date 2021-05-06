package com.cvtracker.vmd.base

import com.cvtracker.vmd.data.Bookmark
import com.cvtracker.vmd.data.DisplayItem
import com.cvtracker.vmd.master.AnalyticsHelper
import com.cvtracker.vmd.master.FcmHelper
import com.cvtracker.vmd.master.FilterType
import com.cvtracker.vmd.master.PrefHelper

abstract class AbstractCenterPresenter(open val view: CenterContract.View): CenterContract.Presenter {

    override fun onCenterClicked(center: DisplayItem.Center) {
        view.openLink(center.url)
        AnalyticsHelper.logEventRdvClick(center, FilterType.ByDate)
    }

    override fun onBookmarkClicked(center: DisplayItem.Center, target: Bookmark) {
        val fromBookmark = center.bookmark

        if (fromBookmark == Bookmark.NOTIFICATION) {
            // unsubscribe from center
            FcmHelper.unsubscribeFromCenter(center)
        }

        if (target == Bookmark.NOTIFICATION) {
            // subscribe to center
            FcmHelper.subscribeToCenter(center)
        }

        center.bookmark = target
        PrefHelper.updateBookmark(center)
        AnalyticsHelper.logEventBookmarkClick(center, FilterType.ByDate, fromBookmark)
    }

}