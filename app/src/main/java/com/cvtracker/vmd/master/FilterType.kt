package com.cvtracker.vmd.master

import com.cvtracker.vmd.R
import com.cvtracker.vmd.data.DisplayItem

class FilterType {

    companion object {

        fun getDefault(filterPref: Set<FilterPref>): MutableList<FilterSection> {
            return fromFilterPref(filterPref, getDefault())
        }

        fun toFilterPref(filterSections: MutableList<FilterSection>): Set<FilterPref> {
            val result: HashSet<FilterPref> = hashSetOf()
            filterSections.forEach { section ->
                section.filters.forEach { filter ->
                    result.add(FilterPref(section.id, filter.displayTitle, filter.enabled, filter.param))
                }
            }
            return result
        }

        fun fromFilterPref(
            filterPref: Set<FilterPref>,
            filterSections: MutableList<FilterSection>
        ): MutableList<FilterSection> {
            if (filterPref.isEmpty()) {
                return filterSections
            }
            filterSections.forEach { section ->
                section.filters.forEach { filter ->
                    filter.enabled =
                        filterPref.find { it.sectionId == section.id && it.name == filter.displayTitle }?.enabled
                            ?: section.defaultState
                    filter.param = filterPref.find { it.sectionId == section.id && it.name == filter.displayTitle }?.param
                        ?: section.defaultParam
                }
            }
            return filterSections
        }

        const val FILTER_APPOINTMENT_SECTION = "FILTER_APPOINTMENT_SECTION"
        const val FILTER_VACCINE_TYPE_SECTION = "FILTER_VACCINE_TYPE_SECTION"
        const val FILTER_DISTANCE_SECTION = "FILTER_DISTANCE_SECTION"

        const val FILTER_CHRONODOSE_ID = "FILTER_CHRONODOSE_ID"
        const val FILTER_AVAILABLE_ID = "FILTER_AVAILABLE_ID"

        const val DEFAULT_DISTANCE = 50

        private fun getDefault() = mutableListOf<FilterSection>().apply {
            add(FilterSection(
                    id = FILTER_APPOINTMENT_SECTION,
                    displayTitle = null,
                    defaultState = false,
                    primaryFilter = true,
                    filters = listOf(
                            Filter(ViteMaDoseApp.get().getString(R.string.filter_chronodose_only), false, FILTER_CHRONODOSE_ID) { center, filter ->
                                center.isChronodose
                            },
                            Filter(ViteMaDoseApp.get().getString(R.string.filter_available_centers_only), false, FILTER_AVAILABLE_ID) { center, filter ->
                                center.available
                            }
                    )))

            add(FilterSection(
                    id = FILTER_DISTANCE_SECTION,
                    displayTitle = ViteMaDoseApp.get().getString(R.string.filter_distance),
                    defaultState = true,
                    defaultParam = DEFAULT_DISTANCE,
                    primaryFilter = false,
                    filters = listOf(
                            FilterSeekBar(ViteMaDoseApp.get().getString(R.string.filter_distance), true, null, 5, 100, ({ filter -> "${filter.param} km" })) { center, filter ->
                                center.distance?.let { it < filter.param } ?: true
                            }
                    )))
        }
    }

    class FilterSection(
        val id: String,
        val displayTitle: String?,
        val defaultState: Boolean,
        val defaultParam: Int = 0,
        val primaryFilter: Boolean,
        val filters: List<Filter>
    ){
        override fun toString(): String {
            return displayTitle + " ==> " + filters.joinToString(separator = ",") { it.displayTitle + "/" + it.enabled }
        }
    }

    open class Filter(
            val displayTitle: String,
            var enabled: Boolean,
            val id: String? = null,
            var param: Int = 0,
            val predicate: (DisplayItem.Center, Filter) -> Boolean,
    )

    class FilterSeekBar(
            displayTitle: String,
            enabled: Boolean,
            id: String? = null,
            var minValue: Int,
            var maxValue: Int,
            private val valueFormatter: (Filter) -> String,
            predicate: (DisplayItem.Center, Filter) -> Boolean
    ) : Filter(displayTitle, enabled, id, DEFAULT_DISTANCE, predicate) {

        val formattedValue: String
            get() = valueFormatter.invoke(this)
    }
}