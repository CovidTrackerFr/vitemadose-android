package com.cvtracker.vmd.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.cvtracker.vmd.R
import com.cvtracker.vmd.data.ItemStat
import kotlinx.android.synthetic.main.view_stats.view.*

class StatView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    ConstraintLayout(context, attrs) {

    init {
        LayoutInflater.from(context).inflate(R.layout.view_stats, this, true)
    }

    fun bind(itemStat: ItemStat) {
        iconView.setImageResource(itemStat.icon)
        statNumberView.text = itemStat.countString
        statLibelleView.text = context.resources.getQuantityText(
            itemStat.plurals,
            itemStat.count
        )
    }
}