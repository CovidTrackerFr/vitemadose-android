package com.cvtracker.vmd.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.SeekBar
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.cvtracker.vmd.R
import com.cvtracker.vmd.master.FilterType
import kotlinx.android.synthetic.main.alert_dialog_filters.view.container
import kotlinx.android.synthetic.main.item_chip_holder.view.*
import kotlinx.android.synthetic.main.item_filter_seekbar.view.*

class FiltersDialogView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null): ConstraintLayout(context, attrs) {

    init {
        View.inflate(context, R.layout.alert_dialog_filters, this)
    }

    lateinit var newFilters: List<FilterType.FilterSection>

    fun populateFilters(filters: List<FilterType.FilterSection>){
        newFilters = filters.toMutableList()

        filters.forEach {
            /** Section header **/
            it.displayTitle?.let {
                val sectionView =  LayoutInflater.from(context).inflate(R.layout.item_filter_section, null)
                (sectionView as AppCompatTextView).text = it
                container.addView(sectionView)
            }

            if (it.id == FilterType.FILTER_VACCINE_TYPE_SECTION) {
                val chipHolder = LayoutInflater.from(context).inflate(R.layout.item_chip_holder, null) as ConstraintLayout
                it.filters
                        .sortedBy { it.displayTitle }
                        .forEach { chipHolder.flow.addView(getFilterItemView(it, R.layout.item_filter_chip)) }
                container.addView(chipHolder)
            } else if (it.id == FilterType.FILTER_DISTANCE_SECTION) {
                it.filters.forEach { container.addView(getFilterItemSeekbarView(it as FilterType.FilterSeekBar)) }
            } else {
                it.filters.forEach { container.addView(getFilterItemView(it, R.layout.item_filter)) }
            }
        }
    }

    private fun getFilterItemView(filter: FilterType.Filter, @LayoutRes layoutId: Int): AppCompatCheckBox{
        return (LayoutInflater.from(context).inflate(layoutId, null) as AppCompatCheckBox).apply {
            text = filter.displayTitle
            isChecked = filter.enabled
            id = View.generateViewId()
            setOnCheckedChangeListener { buttonView, isChecked ->
                buttonView.isChecked = isChecked
                newFilters.onEach {
                    it.filters.find { it.displayTitle == buttonView.text }?.enabled = isChecked
                }
            }
        }
    }

    private fun getFilterItemSeekbarView(filter: FilterType.FilterSeekBar): View {
        return (LayoutInflater.from(context).inflate(R.layout.item_filter_seekbar, null)).apply {
            id = View.generateViewId()

            seekbarView.max = filter.maxValue - filter.minValue
            seekbarView.progress = filter.param - filter.minValue
            valueTextView.text = filter.formattedValue
            seekbarView.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    filter.param = filter.minValue + progress
                    valueTextView.text = filter.formattedValue
                    newFilters.onEach {
                        it.filters.find { it.displayTitle == filter.displayTitle }?.param = filter.param
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    // do noting
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    // do noting
                }
            })
        }
    }
}