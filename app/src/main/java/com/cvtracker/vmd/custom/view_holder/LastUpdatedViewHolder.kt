package com.cvtracker.vmd.custom.view_holder

import android.content.Context
import android.content.res.ColorStateList
import android.text.format.DateUtils
import android.view.ViewGroup
import com.cvtracker.vmd.R
import com.cvtracker.vmd.custom.CenterAdapter
import com.cvtracker.vmd.data.DisplayItem
import com.cvtracker.vmd.extensions.hide
import com.cvtracker.vmd.extensions.show
import kotlinx.android.synthetic.main.item_last_updated.view.*

class LastUpdatedViewHolder(
        context: Context,
        parent: ViewGroup,
        adapter: CenterAdapter,
        private val listener: Listener?
) : AbstractViewHolder<DisplayItem.LastUpdated>(context, parent, adapter, R.layout.item_last_updated) {

    interface Listener {
        fun onRemoveDisclaimerClick()
    }

    override fun bind(data: DisplayItem.LastUpdated, position: Int) {
        with(itemView) {
            lastUpdated.text = context.getString(
                    R.string.last_updated,
                    DateUtils.getRelativeTimeSpanString(
                            data.date.time,
                            System.currentTimeMillis(),
                            0L
                    )
            )
            data.disclaimer?.let { disclaimer ->
                disclaimerMessageView.text = disclaimer.message
                disclaimerMessageView.setTextColor(disclaimer.severity.textColor(context))
                disclaimerCardView.setCardBackgroundColor(disclaimer.severity.backgroundColor(context))
                removeDisclaimerView.imageTintList = ColorStateList.valueOf(disclaimer.severity.textColor(context))
                removeDisclaimerView.setOnClickListener {
                    data.disclaimer = null
                    adapter.notifyItemChanged(position)
                    listener?.onRemoveDisclaimerClick()
                }
                disclaimerCardView.show()
            } ?: apply {
                disclaimerCardView.hide()
            }
        }
    }
}