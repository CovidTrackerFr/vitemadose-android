package com.cvtracker.vmd.base

import com.cvtracker.vmd.data.Bookmark
import com.cvtracker.vmd.data.DisplayItem
import com.cvtracker.vmd.master.AnalyticsHelper
import com.cvtracker.vmd.master.FcmHelper
import com.cvtracker.vmd.master.PrefHelper
import com.cvtracker.vmd.master.SortType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

abstract class AbstractCenterPresenter(open val view: CenterContract.View) : CenterContract.Presenter, CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Main + SupervisorJob()

    override fun onCenterClicked(center: DisplayItem.Center) {
        view.openLink(center.url)
        AnalyticsHelper.logEventRdvClick(center, SortType.ByDate)
    }

    override fun onBookmarkClicked(center: DisplayItem.Center, target: Bookmark) {
        val fromBookmark = center.bookmark

        // unsubscribe from center
        when (fromBookmark) {
            Bookmark.NOTIFICATION_CHRONODOSE -> FcmHelper.unsubscribeFromCenter(center, true)
            Bookmark.NOTIFICATION -> FcmHelper.unsubscribeFromCenter(center, false)
            else -> { /* nothing to do */ }
        }

        // subscribe to center
        when (target) {
            Bookmark.NOTIFICATION_CHRONODOSE -> FcmHelper.subscribeToCenter(center, true)
            Bookmark.NOTIFICATION -> FcmHelper.subscribeToCenter(center, false)
            else -> { /* nothing to do */ }
        }

        center.bookmark = target
        PrefHelper.updateBookmark(center)
        AnalyticsHelper.logEventBookmarkClick(center, SortType.ByDate, fromBookmark)
    }

}