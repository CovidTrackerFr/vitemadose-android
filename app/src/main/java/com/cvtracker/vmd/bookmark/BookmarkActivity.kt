package com.cvtracker.vmd.bookmark

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.MenuItem
import com.cvtracker.vmd.R
import com.cvtracker.vmd.base.AbstractCenterActivity
import com.cvtracker.vmd.data.DisplayItem
import com.cvtracker.vmd.extensions.colorAttr
import kotlinx.android.synthetic.main.activity_about.*
import kotlinx.android.synthetic.main.activity_main.*

class BookmarkActivity : AbstractCenterActivity<BookmarkContract.Presenter>(), BookmarkContract.View {

    override val presenter: BookmarkContract.Presenter = BookmarkPresenter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookmark)
        setResult(RESULT_CANCELED)
        window.setBackgroundDrawable(ColorDrawable(colorAttr(R.attr.backgroundColor)))

        toolbar.setTitle(R.string.bookmark)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        refreshLayout.setOnRefreshListener {
            presenter.loadBookmarks()
        }

        presenter.loadBookmarks()
    }

    override fun showBookmarkBottomSheet(center: DisplayItem.Center, position: Int){
        super.showBookmarkBottomSheet(center, position)
        /** Retain we should have changed a bookmark state **/
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

    override fun showNoBookmark() {
        // TODO("Not yet implemented")
    }
}