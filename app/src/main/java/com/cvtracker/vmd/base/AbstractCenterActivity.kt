package com.cvtracker.vmd.base

import androidx.constraintlayout.widget.ConstraintLayout
import com.cvtracker.vmd.R
import com.cvtracker.vmd.custom.BookmarkBottomSheetFragment
import com.cvtracker.vmd.custom.CenterAdapter
import com.cvtracker.vmd.custom.DailyCenterListAdapter
import com.cvtracker.vmd.custom.view_holder.CenterViewHolder
import com.cvtracker.vmd.custom.view_holder.LastUpdatedViewHolder
import com.cvtracker.vmd.data.DisplayItem
import com.cvtracker.vmd.extensions.hide
import com.cvtracker.vmd.extensions.launchWebUrl
import com.cvtracker.vmd.extensions.show
import com.cvtracker.vmd.master.AbstractVMDActivity
import com.cvtracker.vmd.master.IntentHelper
import com.cvtracker.vmd.master.TagType
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*

abstract class AbstractCenterActivity<out T : CenterContract.Presenter> : AbstractVMDActivity(),
        CenterContract.View, CenterViewHolder.Listener {

    abstract val presenter: T

    open var lastUpdatedListener: LastUpdatedViewHolder.Listener? = null

    override fun showCenters(list: List<List<DisplayItem>>, tagType: TagType?) {
        appBarLayout.setExpanded(true, true)
        if(centersPagerView.adapter == null){
            centersPagerView.adapter = DailyCenterListAdapter(
                context = this,
                items = list,
                centerListener = this,
                lastUpdatedListener = lastUpdatedListener
            )
        }else{
            (centersPagerView.adapter as? DailyCenterListAdapter)?.updateList(list)
        }

        /** set up filter state **/
        if (tagType != null) {
            (filterView?.layoutParams as? ConstraintLayout.LayoutParams)?.verticalBias = 0.5f
            sortSwitchView?.show()
            sortSwitchView?.updateSelectedTagType(tagType)
        } else {
            (filterView?.layoutParams as? ConstraintLayout.LayoutParams)?.verticalBias = 0f
            sortSwitchView?.hide()
        }
        filterView?.show()
    }

    open fun showBookmarkBottomSheet(adapter: CenterAdapter, center: DisplayItem.Center, position: Int) {
        supportFragmentManager.let {
            BookmarkBottomSheetFragment.newInstance(center.bookmark).apply {
                listener = {
                    presenter.onBookmarkClicked(center, it)
                    adapter.notifyItemChanged(position)
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

    override fun onBookmarkClicked(adapter: CenterAdapter, center: DisplayItem.Center, position: Int) {
        showBookmarkBottomSheet(adapter, center, position)
    }

    override fun onAddressClicked(address: String) {
        IntentHelper.startMapsActivity(this, address)
    }

    override fun onPhoneClicked(phoneNumber: String) {
        IntentHelper.startPhoneActivity(this, phoneNumber)
    }
}