package com.cvtracker.vmd.master

import com.cvtracker.vmd.data.DisplayItem

enum class FilterType(val displayTitle: String, val comparator: Comparator<DisplayItem.Center>) {
    ByDate("Au plus tôt", compareBy(nullsLast()) { it.nextSlot }),
    ByProximity("Au plus proche", compareBy(nullsLast()) { it.distance });
}