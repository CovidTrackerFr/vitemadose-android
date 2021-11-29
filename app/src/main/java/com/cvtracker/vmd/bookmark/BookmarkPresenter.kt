package com.cvtracker.vmd.bookmark

import com.cvtracker.vmd.base.AbstractCenterPresenter
import com.cvtracker.vmd.data.Bookmark
import com.cvtracker.vmd.data.DisplayItem
import com.cvtracker.vmd.master.DataManager
import com.cvtracker.vmd.master.PrefHelper
import com.cvtracker.vmd.master.SortType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber

class BookmarkPresenter(override val view: BookmarkContract.View) : AbstractCenterPresenter(view), BookmarkContract.Presenter {

    private var jobBookmarks: Job? = null

    override fun loadBookmarks(department: String?, centerId: String?) {
        jobBookmarks?.cancel()
        jobBookmarks = launch(Dispatchers.Main) {
            val centersBookmark = PrefHelper.centersBookmark
                    .filter { department == null || department == it.department }
                    .filter { centerId == null || centerId == it.centerId }

            if (centersBookmark.isEmpty()) {
                view.setLoading(false)
                view.showNoBookmark(true)
            } else {
                view.showNoBookmark(false)
                try {
                    view.setLoading(true)

                    DataManager.getCentersBookmark(centersBookmark).let {
                        fun prepareCenters(
                            centers: MutableList<DisplayItem.Center>
                        ): List<DisplayItem.Center> {
                            centers.sortWith(SortType.ByDate.comparator)
                            return centers
                                .onEach { center ->
                                    center.bookmark = centersBookmark
                                        .firstOrNull { center.id == it.centerId }?.bookmark
                                        ?: Bookmark.NONE
                                }
                        }

                        val list = mutableListOf<DisplayItem>()

                        /** Add available centers **/
                        list.addAll(prepareCenters(it.availableCenters))

                        /** Add unavailable centers **/
                        list.addAll(prepareCenters(it.unavailableCenters))

                        if(list.isEmpty()){
                            view.showNoBookmark(true)
                        }else {
                            view.showCenters(listOf(list), null)
                        }
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