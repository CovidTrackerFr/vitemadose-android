package com.cvtracker.vmd.bookmark

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.cvtracker.vmd.R
import com.cvtracker.vmd.custom.BookmarkBottomSheetFragment
import com.cvtracker.vmd.custom.CenterAdapter
import com.cvtracker.vmd.data.DisplayItem
import com.cvtracker.vmd.extensions.colorAttr
import com.cvtracker.vmd.master.IntentHelper
import kotlinx.android.synthetic.main.activity_about.*
import kotlinx.android.synthetic.main.activity_main.*

class BookmarkActivity : AppCompatActivity(), BookmarkContract.View {

    private val presenter: BookmarkContract.Presenter = BookmarkPresenter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookmark)
        window.setBackgroundDrawable(ColorDrawable(colorAttr(R.attr.backgroundColor)))

        toolbar.setTitle(R.string.bookmark)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        refreshLayout.setOnRefreshListener {
            presenter.loadBookmarks()
        }

        presenter.loadBookmarks()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun showCenters(list: List<DisplayItem>) {
        centersRecyclerView.adapter = CenterAdapter(
            context = this,
            items = list,
            onClicked = { presenter.onCenterClicked(it) },
            onBookmarkClicked = { center, position ->
                supportFragmentManager.let {
                    BookmarkBottomSheetFragment.newInstance(center.bookmark).apply {
                        listener = {
                            presenter.onBookmarkClicked(center, it)
                            this@BookmarkActivity.centersRecyclerView.adapter?.notifyItemChanged(
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
    }

    override fun setLoading(loading: Boolean) {
        refreshLayout.isRefreshing = loading
    }

    override fun showCentersError() {
        // TODO("Not yet implemented")
    }

    override fun showNoBookmark() {
        // TODO("Not yet implemented")
    }
}