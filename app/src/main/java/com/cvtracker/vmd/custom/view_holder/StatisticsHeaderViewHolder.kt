package com.cvtracker.vmd.custom.view_holder

import android.content.Context
import android.view.ViewGroup
import com.cvtracker.vmd.R
import com.cvtracker.vmd.custom.CenterAdapter
import com.cvtracker.vmd.data.DisplayItem
import com.cvtracker.vmd.data.ItemStat
import kotlinx.android.synthetic.main.item_statistics_header.view.*

class StatisticsHeaderViewHolder(
        context: Context,
        parent: ViewGroup,
        adapter: CenterAdapter
) : AbstractViewHolder<DisplayItem.AvailableCenterHeader>(context, parent, adapter, R.layout.item_statistics_header) {

    override fun bind(data: DisplayItem.AvailableCenterHeader, position: Int) {
        itemView.firstStatView.apply {
            bind(
                    ItemStat(
                            icon = R.drawable.ic_appointement,
                            plurals = R.plurals.slot_disponibilities,
                            countString = data.slotsCount.toString(),
                            count = data.slotsCount
                    )
            )
        }
        itemView.secondStatView.apply {
            bind(
                    ItemStat(
                            icon = R.drawable.ic_check,
                            plurals = R.plurals.center_disponibilities,
                            countString = data.placesCount.toString(),
                            count = data.placesCount
                    )
            )
        }
    }

}