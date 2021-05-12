package com.cvtracker.vmd.base

import androidx.appcompat.app.AppCompatActivity
import com.cvtracker.vmd.R
import com.cvtracker.vmd.custom.BookmarkBottomSheetFragment
import com.cvtracker.vmd.custom.CenterAdapter
import com.cvtracker.vmd.data.DisplayItem
import com.cvtracker.vmd.extensions.*
import com.cvtracker.vmd.master.IntentHelper
import com.cvtracker.vmd.master.SortType
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*

abstract class AbstractCenterActivity<out T : CenterContract.Presenter> : AppCompatActivity(),
    CenterContract.View {

    abstract val presenter: T

    abstract val onChronodoseFilterClick: (() -> Unit)?

    abstract val onSlotsFilterClick: (() -> Unit)?

    abstract val onRemoveDisclaimerClick: (() -> Unit)?

    override fun showCenters(list: List<DisplayItem>, sortType: SortType?) {
        appBarLayout.setExpanded(true, true)
        centersRecyclerView.adapter = CenterAdapter(
            context = this,
            items = list,
            onClicked = { presenter.onCenterClicked(it) },
            onBookmarkClicked = { center, position -> showBookmarkBottomSheet(center, position) },
            onAddressClicked = { IntentHelper.startMapsActivity(this, it) },
            onPhoneClicked = { IntentHelper.startPhoneActivity(this, it) },
            onChronodoseFilterClick = onChronodoseFilterClick,
            onSlotsFilterClick = onSlotsFilterClick,
            onRemoveDisclaimerClick = onRemoveDisclaimerClick
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
}