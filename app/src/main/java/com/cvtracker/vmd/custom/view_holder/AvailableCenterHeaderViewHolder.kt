package com.cvtracker.vmd.custom.view_holder

import android.content.Context
import android.view.ViewGroup
import com.cvtracker.vmd.R
import com.cvtracker.vmd.custom.CenterAdapter
import com.cvtracker.vmd.data.DisplayItem
import com.cvtracker.vmd.data.ItemStat
import com.cvtracker.vmd.extensions.color
import com.cvtracker.vmd.extensions.colorAttr
import kotlinx.android.synthetic.main.item_available_center_header.view.*

class AvailableCenterHeaderViewHolder(
        context: Context,
        parent: ViewGroup,
        adapter: CenterAdapter,
        private val listener: Listener?
) : AbstractViewHolder<DisplayItem.AvailableCenterHeader>(context, parent, adapter, R.layout.item_available_center_header) {

    interface Listener {
        fun onChronodoseFilterClick()
        fun onSlotsFilterClick()
    }

    override fun bind(data: DisplayItem.AvailableCenterHeader, position: Int) {
        itemView.firstStatView.apply {
            isSelected = data.isSlotFilterSelected
            bind(
                    ItemStat(
                            icon = R.drawable.ic_appointement,
                            plurals = R.plurals.slot_disponibilities,
                            countString = data.slotsCount.toString(),
                            count = data.slotsCount,
                            color = color(R.color.danube)
                    )
            )
            setOnClickListener {
                data.isSlotFilterSelected = !data.isSlotFilterSelected
                data.isChronodoseFilterSelected = false
                adapter.notifyItemChanged(position)
                listener?.onSlotsFilterClick()
            }
        }
        itemView.secondStatView.apply {
            isSelected = data.isChronodoseFilterSelected
            bind(
                    ItemStat(
                            icon = R.drawable.ic_eclair,
                            plurals = R.plurals.chronodose_disponibilities,
                            countString = data.chronodoseCount.toString(),
                            count = data.chronodoseCount,
                            color = colorAttr(R.attr.colorPrimary)
                    )
            )
            setOnClickListener {
                data.isChronodoseFilterSelected = !data.isChronodoseFilterSelected
                data.isSlotFilterSelected = false
                adapter.notifyItemChanged(position)
                listener?.onChronodoseFilterClick()
            }
        }
    }

}