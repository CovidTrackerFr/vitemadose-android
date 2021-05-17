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
                    result.add(FilterPref(section.id, filter.displayTitle, filter.enabled))
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
                            ?: filter.enabled
                }
            }
            return filterSections
        }

        const val FILTER_APPOINTMENT = "FILTER_APPOINTMENT"
        const val FILTER_VACCINE_TYPE = "FILTER_VACCINE_TYPE"
        const val FILTER_DISTANCE = "FILTER_DISTANCE"

        const val FILTER_CHRONODOSE_ID = "FILTER_CHRONODOSE_ID"
        const val FILTER_AVAILABLE_ID = "FILTER_AVAILABLE_ID"

        val appointmentFilterType = FilterSection(
            id = FILTER_APPOINTMENT,
            displayTitle = null,
            defaultState = false,
            primaryFilter = true,
            filters = listOf(
                Filter("Chronodoses uniquement", false, FILTER_CHRONODOSE_ID) {
                    it.isChronodose
                },
                Filter("Centres disponibles uniquement", false, FILTER_AVAILABLE_ID) {
                    it.available
                }
            ))

        val distanceFilterType = FilterSection(
                id = FILTER_DISTANCE,
                displayTitle = "Distance",
                defaultState = false,
                primaryFilter = true,
                filters = listOf(
                        FilterSeekBar("", false, null, 5, 100, 50) {
                            it.distance != null // todo
                        }
                ))
    }

    class FilterSection(
        val id: String,
        val displayTitle: String?,
        val defaultState: Boolean,
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
            val predicate: (DisplayItem.Center) -> Boolean
    )

    class FilterSeekBar(
            displayTitle: String,
            enabled: Boolean,
            id: String? = null,
            var minValue: Int,
            var maxValue: Int,
            var value: Int,
            predicate: (DisplayItem.Center) -> Boolean
    ) : Filter(displayTitle, enabled, id, predicate)
}