package com.cvtracker.vmd.home

import com.cvtracker.vmd.base.AbstractCenterPresenter
import com.cvtracker.vmd.data.Bookmark
import com.cvtracker.vmd.data.Disclaimer
import com.cvtracker.vmd.data.DisplayItem
import com.cvtracker.vmd.data.SearchEntry
import com.cvtracker.vmd.master.*
import com.cvtracker.vmd.master.FilterType.Companion.FILTER_AVAILABLE_ID
import com.cvtracker.vmd.master.FilterType.Companion.FILTER_CHRONODOSE_ID
import com.cvtracker.vmd.master.FilterType.Companion.FILTER_VACCINE_TYPE
import kotlinx.coroutines.*
import timber.log.Timber

class MainPresenter(override val view: MainContract.View) : AbstractCenterPresenter(view), MainContract.Presenter {

    private var jobSearch: Job? = null
    private var jobCenters: Job? = null

    private var selectedSortType: SortType? = null
    private var filterSections = FilterType.getDefault()

    companion object{
        var DISPLAY_CENTER_MAX_DISTANCE_IN_KM = 50f
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
                    val sortType = selectedSortType ?: entry.defaultSortType
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
                                centers.removeAll {
                                    (it.distance ?: 0f) > DISPLAY_CENTER_MAX_DISTANCE_IN_KM
                                }
                            }

                            val centersBookmark = PrefHelper.centersBookmark

                            /** Sort results **/
                            centers.sortWith(sortType.comparator)
                            centers.onEach { center ->
                                center.available = center.appointmentCount > 0
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
                                preparedAvailableCenters.sumBy { it.chronodoseCount },
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
                                    /** Add the header with unavailable centers **/
                                    list.add(DisplayItem.UnavailableCenterHeader(preparedAvailableCenters.isNotEmpty()))

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
        selectedSortType = null
        resetFilters(needRefresh = false)
        loadCenters()
    }

    override fun getSavedSearchEntry(): SearchEntry? {
        return PrefHelper.favEntry
    }

    override fun onSortChanged(sortType: SortType) {
        selectedSortType = sortType
        view.showCenters(emptyList(), sortType)
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
                if(section.defaultState.not() && filter.enabled) {
                    /** We want to remove items which do not match the predicate, ie a filter **/
                    centers.removeAll { filter.predicate(it).not() }
                }else if(section.defaultState && !filter.enabled) {
                    /** We want to remove items which do match the predicate **/
                    centers.removeAll(filter.predicate)
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

        /** Update our map with the current filters **/
        filterSections.find { it.id == FILTER_VACCINE_TYPE }?.filters?.map {
            if(mapVaccine[it.displayTitle] != null){
                mapVaccine[it.displayTitle] = it.enabled
            }
        }

        /** Finally create our filter vaccine with the map **/
        val section = FilterType.FilterSection(
            id = FILTER_VACCINE_TYPE,
            displayTitle = "Types de vaccins",
            defaultState = true,
            primaryFilter = false,
            filters = mapVaccine.map { entry ->
                FilterType.Filter(entry.key, entry.value){
                    it.vaccineType?.toList()?.contains(entry.key) ?: false
                }
            }
        )
        filterSections.removeAll { it.id == FILTER_VACCINE_TYPE }
        filterSections.add(section)
    }

    override fun updateFilters(filters: List<FilterType.FilterSection>, needRefresh: Boolean){
        filterSections = filters.toMutableList()
        view.updateFilterState(isDefaultFilters())
        if(needRefresh) {
            loadCenters()
        }
    }

    override fun resetFilters(needRefresh: Boolean) {
        filterSections.onEach { section ->
            section.filters.onEach { it.enabled = section.defaultState }
        }
        updateFilters(filterSections, needRefresh)
    }

    private fun isDefaultFilters(): Boolean {
        return (filterSections.find { section ->
            section.filters.find { it.enabled != section.defaultState } != null
        }) == null
    }

    override fun getFilters() = filterSections

    override fun displayChronodoseOnboardingIfNeeded() {
        if (!PrefHelper.chronodoseOnboardingDisplayed) {
            view.showChronodoseOnboarding()
        }
    }

    override fun removeDisclaimer(){
        disclaimer = null
    }
}