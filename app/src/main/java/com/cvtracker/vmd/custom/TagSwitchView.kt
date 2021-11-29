package com.cvtracker.vmd.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.forEachIndexed
import com.cvtracker.vmd.R
import com.cvtracker.vmd.master.PrefHelper
import com.cvtracker.vmd.master.TagType

class TagSwitchView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    ConstraintLayout(context, attrs) {

    var onTagTypeChangedListener: ((TagType) -> Unit)? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.view_sort_switch, this, true)
        TagType.values().forEachIndexed { index, filter ->
            val view = LayoutInflater.from(context)
                .inflate(R.layout.view_sort_switch_item, findViewById(R.id.container), false).apply {
                    setOnClickListener {
                        updateSelectedTagType(filter)
                        onTagTypeChangedListener?.invoke(filter)
                    }
                    findViewById<TextView>(R.id.sortNameView).setText(filter.titleRes)
                }
            findViewById<ViewGroup>(R.id.container).addView(
                view,
                LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            )
        }
        updateSelectedFilterIndex(PrefHelper.tagType.ordinal)
    }

    private fun updateSelectedFilterIndex(indexSelected: Int) {
        findViewById<ViewGroup>(R.id.container).forEachIndexed { index, view -> view.isSelected = (index == indexSelected) }
    }

    fun updateSelectedTagType(filter: TagType) {
        updateSelectedFilterIndex(TagType.values().indexOf(filter))
    }
}
