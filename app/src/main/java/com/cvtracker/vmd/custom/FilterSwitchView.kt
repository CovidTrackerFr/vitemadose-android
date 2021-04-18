package com.cvtracker.vmd.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.forEachIndexed
import com.cvtracker.vmd.R
import com.cvtracker.vmd.master.AnalyticsHelper
import kotlinx.android.synthetic.main.view_filter_switch.view.*
import kotlinx.android.synthetic.main.view_filter_switch_item.view.*

class FilterSwitchView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    ConstraintLayout(context, attrs) {

    var onFilterChangedListener: ((AnalyticsHelper.FilterType) -> Unit)? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.view_filter_switch, this, true)
        AnalyticsHelper.FilterType.values().forEachIndexed { index, filter ->
            val view = LayoutInflater.from(context)
                .inflate(R.layout.view_filter_switch_item, container, false).apply {
                    setOnClickListener {
                        updateSelectedFilterIndex(index)
                        onFilterChangedListener?.invoke(filter)
                    }
                    filterNameView.text = filter.displayTitle
                }
            container.addView(
                view,
                LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            )
        }
        updateSelectedFilterIndex(0)
    }

    private fun updateSelectedFilterIndex(indexSelected: Int) {
        container.forEachIndexed { index, view -> view.isSelected = (index == indexSelected) }
    }

    fun updateSelectedFilter(filter: AnalyticsHelper.FilterType) {
        updateSelectedFilterIndex(AnalyticsHelper.FilterType.values().indexOf(filter))
    }
}