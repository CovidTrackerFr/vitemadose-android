package com.cvtracker.vmd.base

import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.cvtracker.vmd.R
import com.cvtracker.vmd.custom.BookmarkBottomSheetFragment
import com.cvtracker.vmd.custom.CenterAdapter
import com.cvtracker.vmd.custom.view_holder.CenterViewHolder
import com.cvtracker.vmd.custom.view_holder.LastUpdatedViewHolder
import com.cvtracker.vmd.custom.view_holder.StatisticsHeaderViewHolder
import com.cvtracker.vmd.data.DisplayItem
import com.cvtracker.vmd.extensions.hide
import com.cvtracker.vmd.extensions.launchWebUrl
import com.cvtracker.vmd.extensions.show
import com.cvtracker.vmd.master.AbstractVMDActivity
import com.cvtracker.vmd.master.IntentHelper
import com.cvtracker.vmd.master.SortType
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*

abstract class AbstractCenterActivity<out T : CenterContract.Presenter> : AbstractVMDActivity(),
        CenterContract.View, CenterViewHolder.Listener {

    abstract val presenter: T

    open var statisticsHeaderListener: StatisticsHeaderViewHolder.Listener? = null

    open var lastUpdatedListener: LastUpdatedViewHolder.Listener? = null

    override fun showCenters(list: List<DisplayItem>, sortType: SortType?) {
        appBarLayout.setExpanded(true, true)
        centersRecyclerView.layoutManager = StaggeredGridLayoutManager(resources.getInteger(R.integer.column), StaggeredGridLayoutManager.VERTICAL)
        centersRecyclerView.adapter = CenterAdapter(
                context = this,
                items = list,
                centerListener = this,
                statisticsHeaderListener = statisticsHeaderListener,
                lastUpdatedListener = lastUpdatedListener
        )

        /** set up filter state **/
        if (sortType != null) {
            sortSwitchView?.show()
            sortSwitchView?.updateSelectedSort(sortType)
        } else {
            sortSwitchView?.hide()
        }
        filterView?.show()
    }

    open fun showBookmarkBottomSheet(center: DisplayItem.Center, position: Int) {
        supportFragmentManager.let {
            BookmarkBottomSheetFragment.newInstance(center.bookmark).apply {
                listener = {
                    presenter.onBookmarkClicked(center, it)
                    this@AbstractCenterActivity.centersRecyclerView.adapter?.notifyItemChanged(
                            position
                    )
                }
                show(it, tag)
            }
        }
    }

    override fun setLoading(loading: Boolean) {
        refreshLayout.isRefreshing = loading
    }

    override fun openLink(url: String) {
        launchWebUrl(url)
    }

    override fun showCentersError() {
        Snackbar.make(
                findViewById(R.id.container),
                getString(R.string.centers_error),
                Snackbar.LENGTH_SHORT
        ).show()
    }


    /** CenterViewHolder.Listener **/

    override fun onClicked(center: DisplayItem.Center) {
        presenter.onCenterClicked(center)
    }

    override fun onBookmarkClicked(center: DisplayItem.Center, position: Int) {
        showBookmarkBottomSheet(center, position)
    }

    override fun onAddressClicked(address: String) {
        IntentHelper.startMapsActivity(this, address)
    }

    override fun onPhoneClicked(phoneNumber: String) {
        IntentHelper.startPhoneActivity(this, phoneNumber)
    }
}