package com.cvtracker.vmd.master

import com.cvtracker.vmd.data.DisplayItem

class FilterType {

    companion object {

        private fun getDefault() = mutableListOf(appointmentFilterType, distanceFilterType)
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

        private val appointmentFilterType = FilterSection(
            id = FILTER_APPOINTMENT_SECTION,
            displayTitle = null,
            defaultState = false,
            primaryFilter = true,
            filters = listOf(
                Filter("Chronodoses uniquement", false, FILTER_CHRONODOSE_ID) { center, filter ->
                    center.isChronodose
                },
                Filter("Centres disponibles uniquement", false, FILTER_AVAILABLE_ID) { center, filter ->
                    center.available
                }
            ))

        private val distanceFilterType = FilterSection(
                id = FILTER_DISTANCE_SECTION,
                displayTitle = "Distance",
                defaultState = true,
                defaultParam = DEFAULT_DISTANCE,
                primaryFilter = false,
                filters = listOf(
                        FilterSeekBar("Distance", true, null, 5, 100) { center, filter ->
                            center.distance?.let { it < filter.param } ?: true
                        }
                ))
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
            predicate: (DisplayItem.Center, Filter) -> Boolean
    ) : Filter(displayTitle, enabled, id, DEFAULT_DISTANCE, predicate)
}