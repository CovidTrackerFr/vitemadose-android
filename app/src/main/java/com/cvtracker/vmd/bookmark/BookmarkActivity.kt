package com.cvtracker.vmd.bookmark

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.MenuItem
import com.cvtracker.vmd.R
import com.cvtracker.vmd.base.AbstractCenterActivity
import com.cvtracker.vmd.data.DisplayItem
import com.cvtracker.vmd.extensions.*
import kotlinx.android.synthetic.main.activity_about.toolbar
import kotlinx.android.synthetic.main.activity_bookmark.*
import kotlinx.android.synthetic.main.activity_bookmark.centersRecyclerView
import kotlinx.android.synthetic.main.activity_main.refreshLayout

class BookmarkActivity : AbstractCenterActivity<BookmarkContract.Presenter>(), BookmarkContract.View {

    companion object {
        const val EXTRA_DEPARTMENT = "EXTRA_DEPARTMENT"
        const val EXTRA_CENTER_ID = "EXTRA_CENTER_ID"
    }

    override val presenter: BookmarkContract.Presenter = BookmarkPresenter(this)

    private var notificationDepartment: String? = null
    private var notificationCenterId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookmark)
        setResult(RESULT_CANCELED)
        window.setBackgroundDrawable(ColorDrawable(colorAttr(R.attr.backgroundColor)))

        toolbar.setTitle(R.string.bookmark)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        notificationDepartment = intent.getStringExtra(EXTRA_DEPARTMENT)
        notificationCenterId = intent.getStringExtra(EXTRA_CENTER_ID)

        refreshLayout.setOnRefreshListener {
            presenter.loadBookmarks(notificationDepartment, notificationCenterId)
        }

        presenter.loadBookmarks(notificationDepartment, notificationCenterId)

        if(notificationCenterId != null || notificationDepartment != null){
            supportActionBar?.setTitle(R.string.notification)
        }

        centersRecyclerView.topPadding = resources.dpToPx(8f)
    }

    override fun showBookmarkBottomSheet(center: DisplayItem.Center, position: Int) {
        super.showBookmarkBottomSheet(center, position)
        /** Retain we could have changed a bookmark state **/
        setResult(RESULT_OK)
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

    override fun showNoBookmark(visible: Boolean) {
        if (visible) {
            bookmarkEmptyState.show()
        } else {
            bookmarkEmptyState.hide()
        }
    }

    override val onChronodoseFilterClick: (() -> Unit)? = null
    override val onSlotsFilterClick: (() -> Unit)? = null
}