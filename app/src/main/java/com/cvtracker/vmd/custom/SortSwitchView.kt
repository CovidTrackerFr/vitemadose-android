package com.cvtracker.vmd.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.forEachIndexed
import com.cvtracker.vmd.R
import com.cvtracker.vmd.master.SortType
import kotlinx.android.synthetic.main.view_sort_switch.view.*
import kotlinx.android.synthetic.main.view_sort_switch_item.view.*

class SortSwitchView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    ConstraintLayout(context, attrs) {

    var onSortChangedListener: ((SortType) -> Unit)? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.view_sort_switch, this, true)
        SortType.values().forEachIndexed { index, filter ->
            val view = LayoutInflater.from(context)
                .inflate(R.layout.view_sort_switch_item, container, false).apply {
                    setOnClickListener {
                        updateSelectedFilterIndex(index)
                        onSortChangedListener?.invoke(filter)
                    }
                    sortNameView.text = filter.displayTitle
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

    fun updateSelectedSort(filter: SortType) {
        updateSelectedFilterIndex(SortType.values().indexOf(filter))
    }
}