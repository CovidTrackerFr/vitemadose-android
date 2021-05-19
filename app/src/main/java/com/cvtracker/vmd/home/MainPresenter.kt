package com.cvtracker.vmd.home

import com.cvtracker.vmd.R
import com.cvtracker.vmd.base.AbstractCenterPresenter
import com.cvtracker.vmd.data.Bookmark
import com.cvtracker.vmd.data.Disclaimer
import com.cvtracker.vmd.data.DisplayItem
import com.cvtracker.vmd.data.SearchEntry
import com.cvtracker.vmd.master.*
import com.cvtracker.vmd.master.FilterType.Companion.FILTER_AVAILABLE_ID
import com.cvtracker.vmd.master.FilterType.Companion.FILTER_CHRONODOSE_ID
import com.cvtracker.vmd.master.FilterType.Companion.FILTER_VACCINE_TYPE_SECTION
import kotlinx.coroutines.*
import timber.log.Timber
import java.util.*

class MainPresenter(override val view: MainContract.View) : AbstractCenterPresenter(view), MainContract.Presenter {

    private var jobSearch: Job? = null
    private var jobCenters: Job? = null

    private var filterSections = FilterType.getDefault(PrefHelper.filters)

    companion object{
        var disclaimer: Disclaimer? = null
        const val BASE_URL = "https://vitemadose.covidtracker.fr"
    }

    override fun loadInitialState() {
        PrefHelper.favEntry.let { entry ->
            if (entry == null) {
                view.showEmptyState()
            }
            view.displaySelectedSearchEntry(entry)
        }
    }

    override fun loadCenters() {
        jobCenters?.cancel()
        jobCenters = GlobalScope.launch(Dispatchers.Main) {
            PrefHelper.favEntry?.let { entry ->
                try {
                    val sortType = PrefHelper.primarySort
                    val isCitySearch = entry is SearchEntry.City

                    view.removeEmptyStateIfNeeded()
                    view.setLoading(true)

                    DataManager.getCenters(
                        departmentCode = entry.entryDepartmentCode,
                        useNearDepartment = isCitySearch
                    ).let {
                        val list = mutableListOf<DisplayItem>()

                        fun prepareCenters(centers: MutableList<DisplayItem.Center>): MutableList<DisplayItem.Center> {
                            /** Set up distance when city search **/
                            if (isCitySearch) {
                                centers.onEach { it.calculateDistance(entry as SearchEntry.City) }
                            }

                            val centersBookmark = PrefHelper.centersBookmark

                            /** Sort results **/
                            centers.sortWith(sortType.comparator)
                            centers.onEach { center ->
                                center.bookmark = centersBookmark
                                    .firstOrNull { center.id == it.centerId }?.bookmark
                                    ?: Bookmark.NONE
                            }
                            applyFilters(centers, filterSections.filter {
                                it.primaryFilter.not()
                            })
                            return centers
                        }

                        /** Add header to show last updated view **/
                        list.add(DisplayItem.LastUpdated(it.lastUpdated, disclaimer))

                        /** Update available vaccine filter type **/
                        updateVaccineFilters(it.availableCenters)

                        /** First pass preparing centers status, and secondary filters **/
                        val preparedAvailableCenters = prepareCenters(it.availableCenters)
                        val preparedUnavailableCenters = prepareCenters(it.unavailableCenters)

                        /** Add statistics header **/
                        val isChronodoseFilterSelected = filterSections
                            .flatMap { it.filters }
                            .find { it.id == FILTER_CHRONODOSE_ID }
                            ?.enabled ?: false

                        val isAvailableCentersFilterSelected = filterSections
                            .flatMap { it.filters }
                            .find { it.id == FILTER_AVAILABLE_ID }
                            ?.enabled ?: false

                        list.add(
                            DisplayItem.AvailableCenterHeader(
                                preparedAvailableCenters.sumBy { it.appointmentCount },
                                isAvailableCentersFilterSelected,
                                preparedAvailableCenters.filter { it.isChronodose }.sumBy { it.chronodoseCount },
                                isChronodoseFilterSelected
                            )
                        )

                        /** Second pass, primary filters.
                         * Primary filters modify the list but we have to keep the statistics info (above) for the header
                         * That's why we do it here **/
                        applyFilters(preparedAvailableCenters, filterSections.filter { it.primaryFilter })
                        applyFilters(preparedUnavailableCenters, filterSections.filter { it.primaryFilter })

                        when (sortType) {
                            SortType.ByDate -> {
                                /** Add available centers **/
                                list.addAll(preparedAvailableCenters)

                                if (preparedUnavailableCenters.isNotEmpty()) {
                                    /** Add unavailable centers **/
                                    list.addAll(preparedUnavailableCenters)
                                }
                            }
                            SortType.ByProximity -> {
                                val centers = mutableListOf<DisplayItem.Center>().apply {
                                    addAll(preparedAvailableCenters)
                                    addAll(preparedUnavailableCenters)
                                    sortWith(SortType.ByProximity.comparator)
                                }
                                list.addAll(centers)
                            }
                        }

                        view.updateFilterState(isDefaultFilters())
                        view.showCenters(list, if (isCitySearch) sortType else null)
                        AnalyticsHelper.logEventSearch(entry, it, sortType)
                    }
                } catch (e: CancellationException) {
                    /** Coroutine has been canceled => Ignore **/
                } catch (e: Exception){
                    Timber.e(e)
                    view.showCentersError()
                } finally {
                    view.setLoading(false)
                }
            }
        }
    }

    override fun onSearchEntrySelected(searchEntry: SearchEntry) {
        if (searchEntry.entryCode != PrefHelper.favEntry?.entryCode) {
            PrefHelper.favEntry = searchEntry
            view.showCenters(emptyList(), null)
        }
        loadCenters()
    }

    override fun getSavedSearchEntry(): SearchEntry? {
        return PrefHelper.favEntry
    }

    override fun onSortChanged(sortType: SortType) {
        view.showCenters(emptyList(), sortType)
        PrefHelper.primarySort = sortType
        loadCenters()
    }

    override fun onSearchUpdated(search: String) {
        jobSearch?.cancel()
        if (search.length < 2) {
            /** reset entry list **/
            view.setupSelector(emptyList())
            return
        }
        jobSearch = GlobalScope.launch(Dispatchers.Main) {
            /** Wait a bit then we are sure the user want to do this one **/
            delay(250)
            val list = mutableListOf<SearchEntry>()
            try {
                if (search.first().isDigit()) {
                    /** Search by code **/
                    list.addAll(DataManager.getDepartmentsByCode(search))
                    list.addAll(DataManager.getCitiesByPostalCode(search))
                } else {
                    /** Search by name **/
                    list.addAll(DataManager.getDepartmentsByName(search))
                    list.addAll(DataManager.getCitiesByName(search))
                }
                view.setupSelector(list)
            } catch (e: CancellationException) {
                /** Coroutine has been canceled => Ignore **/
            } catch (e: Exception) {
                Timber.e(e)
                view.showSearchError()
            }
        }
    }

    override fun handleDeepLink(data: String) {
        when {
            data.startsWith("$BASE_URL/bookmarks") -> {
                /** Bookmarks list **/
                view.showBookmarks()
            }
            data.startsWith("$BASE_URL/bookmark/") -> {
                val params = data.replace("$BASE_URL/bookmark/", "").split("/")
                if (params.size >= 4) {
                    val department = params[0]
                    val centerId = params[1]
                    val topic = params[2]
                    val type = params[3]

                    view.showBookmarks(department, centerId)

                    AnalyticsHelper.logEventNotificationOpen(department, centerId, topic, type)
                }
            }
        }
    }

    private fun applyFilters(centers: MutableList<DisplayItem.Center>, filtersList: List<FilterType.FilterSection>){
        filtersList.forEach { section ->
            section.filters.forEach { filter ->
                when{
                    (filter.enabled && section.id == FilterType.FILTER_DISTANCE_SECTION) ||
                    section.defaultState.not() && filter.enabled -> {
                        /** We want to remove items which do not match the predicate, ie a filter **/
                        centers.removeAll { filter.predicate(it, filter).not() }
                    }
                    section.defaultState && !filter.enabled -> {
                        /** We want to remove items which do match the predicate **/
                        centers.removeAll { filter.predicate(it, filter) }
                    }
                }
            }
        }
    }

    private fun updateVaccineFilters(centers: List<DisplayItem.Center>){
        /** Retrieve all vaccine type **/
        val mapVaccine = mutableMapOf<String, Boolean>()
        centers.flatMap { it.vaccineType?.toList() ?: emptyList() } .distinct().forEach {
            /** by default, a vaccine filter is enabled **/
            mapVaccine[it] = true
        }

        /** Create our filter vaccine with the map **/
        val section = FilterType.FilterSection(
            id = FILTER_VACCINE_TYPE_SECTION,
            displayTitle = ViteMaDoseApp.get().getString(R.string.filter_vaccine_type),
            defaultState = true,
            primaryFilter = false,
            filters = mapVaccine.map { entry ->
                FilterType.Filter(entry.key, entry.value){ center, filter ->
                    center.vaccineType?.toList()?.contains(entry.key) ?: false
                }
            }
        )
        filterSections.removeAll { it.id == FILTER_VACCINE_TYPE_SECTION }
        filterSections.add(section)
        filterSections = FilterType.fromFilterPref(PrefHelper.filters, filterSections)
    }

    override fun updateFilters(filters: List<FilterType.FilterSection>, needRefresh: Boolean){
        filterSections = filters.toMutableList()
        PrefHelper.filters = FilterType.toFilterPref(filterSections)
        view.updateFilterState(isDefaultFilters())
        if(needRefresh) {
            loadCenters()
        }
    }

    override fun resetFilters(needRefresh: Boolean) {
        filterSections.onEach { section ->
            section.filters.onEach {
                it.enabled = section.defaultState
                it.param = section.defaultParam
            }
        }
        updateFilters(filterSections, needRefresh)
    }

    private fun isDefaultFilters(): Boolean {
        return (filterSections.find { section ->
            section.filters.find { it.enabled != section.defaultState } != null ||
                    section.filters.find { it.param != section.defaultParam } != null
        }) == null
    }

    override fun getFilters() = filterSections

    override fun displayChronodoseOnboardingIfNeeded() {
        if (!PrefHelper.chronodoseOnboardingDisplayed) {
            view.showChronodoseOnboarding()
        }
    }

    override fun removeDisclaimer(){
        disclaimer?.let {
            PrefHelper.lastDisclaimerClosedTimestamp = Date().time
            PrefHelper.lastDisclaimerClosedMessage = it.message
            disclaimer = null
        }
    }
}
