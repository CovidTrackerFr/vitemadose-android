package com.cvtracker.vmd.base

import androidx.appcompat.app.AppCompatActivity
import com.cvtracker.vmd.R
import com.cvtracker.vmd.custom.BookmarkBottomSheetFragment
import com.cvtracker.vmd.custom.CenterAdapter
import com.cvtracker.vmd.data.DisplayItem
import com.cvtracker.vmd.extensions.*
import com.cvtracker.vmd.master.FilterType
import com.cvtracker.vmd.master.IntentHelper
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*

abstract class AbstractCenterActivity<out T : CenterContract.Presenter> : AppCompatActivity(), CenterContract.View {

    abstract val presenter: T

    override fun showCenters(list: List<DisplayItem>, filter: FilterType?) {
        appBarLayout.setExpanded(true, true)
        centersRecyclerView.adapter = CenterAdapter(
            context = this,
            items = list,
            onClicked = { presenter.onCenterClicked(it) },
            onBookmarkClicked = { center, position ->
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
            },
            onAddressClicked = { IntentHelper.startMapsActivity(this, it) },
            onPhoneClicked = { IntentHelper.startPhoneActivity(this, it) }
        )

        /** set up filter state **/
        if (filter != null) {
            centersRecyclerView.topPadding = resources.dpToPx(50f)
            filterSwitchView?.show()
            filterSwitchView?.updateSelectedFilter(filter)
        } else {
            centersRecyclerView.topPadding = resources.dpToPx(12f)
            filterSwitchView?.hide()
        }
    }

    override fun setLoading(loading: Boolean) {
        refreshLayout.isRefreshing = loading
    }

    override fun openLink(url: String) {
        launchWebUrl(url)
    }

    override fun showCentersError() {
        Snackbar.make(findViewById(R.id.container), getString(R.string.centers_error), Snackbar.LENGTH_SHORT).show()
    }
}