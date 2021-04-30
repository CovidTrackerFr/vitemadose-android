package com.cvtracker.vmd.bookmark

import com.cvtracker.vmd.base.AbstractCenterPresenter
import com.cvtracker.vmd.data.Bookmark
import com.cvtracker.vmd.data.DisplayItem
import com.cvtracker.vmd.master.DataManager
import com.cvtracker.vmd.master.PrefHelper
import kotlinx.coroutines.*
import timber.log.Timber

class BookmarkPresenter(override val view: BookmarkContract.View) : AbstractCenterPresenter(view), BookmarkContract.Presenter {

    private var jobBookmarks: Job? = null

    override fun loadBookmarks() {
        jobBookmarks?.cancel()
        jobBookmarks = GlobalScope.launch(Dispatchers.Main) {
            val centersBookmark = PrefHelper.centersBookmark
            if (centersBookmark.isEmpty()) {
                view.showNoBookmark()
            } else {
                try {
                    view.setLoading(true)

                    DataManager.getCentersBookmark(centersBookmark).let {
                        val centersBookmarkId = centersBookmark.map { it.centerId }

                        fun prepareCenters(
                            centers: List<DisplayItem.Center>,
                            available: Boolean
                        ): List<DisplayItem.Center> {
                            return centers
                                .filter { it.id in centersBookmarkId } // todo move this filter on DataManager ?
                                .onEach { center ->
                                    center.available = available
                                    center.bookmark = centersBookmark
                                        .firstOrNull { center.id == it.centerId }?.bookmark
                                        ?: Bookmark.NONE
                                }
                        }

                        val list = mutableListOf<DisplayItem>()

                        /** Add available centers **/
                        list.addAll(prepareCenters(it.availableCenters, true))

                        /** Add unavailable centers **/
                        list.addAll(prepareCenters(it.unavailableCenters, true))
                        view.showCenters(list, null)
                    }
                } catch (e: CancellationException) {
                    /** Coroutine has been canceled => Ignore **/
                } catch (e: Exception) {
                    Timber.e(e)
                    view.showCentersError()
                } finally {
                    view.setLoading(false)
                }
            }
        }
    }


}