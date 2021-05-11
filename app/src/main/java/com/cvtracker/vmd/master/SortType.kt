package com.cvtracker.vmd.master

import androidx.annotation.StringRes
import com.cvtracker.vmd.R
import com.cvtracker.vmd.data.DisplayItem
import java.util.*

enum class SortType(@StringRes val displayTitle: Int, val comparator: Comparator<DisplayItem.Center>) {
    ByDate(R.string.sort_by_date, compareBy<DisplayItem.Center, Date?>(nullsLast()) { it.nextSlot }.thenBy(nullsLast()) { it.distance }),
    ByProximity(R.string.sort_by_proximity, compareBy<DisplayItem.Center, Float?>(nullsLast()) { it.distance }.thenBy(nullsLast()) { it.nextSlot });
}