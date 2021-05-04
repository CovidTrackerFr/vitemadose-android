package com.cvtracker.vmd.custom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.cvtracker.vmd.R
import com.cvtracker.vmd.data.Bookmark
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.bottomsheet_center_bookmark.*

class BookmarkBottomSheetFragment : BottomSheetDialogFragment() {

    var listener: ((Bookmark) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottomsheet_center_bookmark, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViews()
    }

    private fun setUpViews() {
        when (arguments?.getSerializable(EXTRA_CURRENT_BOOKMARK) as? Bookmark) {
            Bookmark.NOTIFICATION -> notificationView.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_notifications_24dp, 0, R.drawable.ic_done_black_24dp, 0)
            Bookmark.FAVORITE -> favoriteView.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_bookmark_24dp, 0, R.drawable.ic_done_black_24dp, 0)
            Bookmark.NONE -> noneView.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_bookmark_border_24_dp, 0, R.drawable.ic_done_black_24dp, 0)
        }

        notificationView.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.notification_disclaimer_title)
                .setMessage(R.string.notification_disclaimer_message)
                .setPositiveButton(R.string.notification_disclaimer_compris) { _, _ ->
                    dismissAllowingStateLoss()
                    listener?.invoke(Bookmark.NOTIFICATION)
                }
                .setNegativeButton(R.string.notification_disclaimer_cancel) { _, _ ->
                }
                .show()
        }
        favoriteView.setOnClickListener {
            dismissAllowingStateLoss()
            listener?.invoke(Bookmark.FAVORITE)
        }
        noneView.setOnClickListener {
            dismissAllowingStateLoss()
            listener?.invoke(Bookmark.NONE)
        }
    }

    companion object {

        private const val EXTRA_CURRENT_BOOKMARK = "EXTRA_CURRENT_BOOKMARK"

        fun newInstance(currentBookmark: Bookmark): BookmarkBottomSheetFragment =
            BookmarkBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(EXTRA_CURRENT_BOOKMARK, currentBookmark)
                }
            }
    }
}