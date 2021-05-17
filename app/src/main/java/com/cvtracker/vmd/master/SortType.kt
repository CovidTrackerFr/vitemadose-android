package com.cvtracker.vmd.master

import androidx.annotation.StringRes
import com.cvtracker.vmd.R
import com.cvtracker.vmd.data.DisplayItem
import java.util.*

enum class SortType(val value:Int, @StringRes val displayTitle: Int, val comparator: Comparator<DisplayItem.Center>) {
    ByDate(0,R.string.sort_by_date, compareBy<DisplayItem.Center, Date?>(nullsLast()) { it.nextSlot }.thenByDescending{ it.isValidAppointmentByPhoneOnly }.thenBy(nullsLast()) { it.distance }),
    ByProximity(1,R.string.sort_by_proximity, compareBy<DisplayItem.Center, Float?>(nullsLast()) { it.distance }.thenBy(nullsLast()) { it.nextSlot });

    companion object {
        fun fromInt(value: Int) = values().first { it.value == value }
    }
}
