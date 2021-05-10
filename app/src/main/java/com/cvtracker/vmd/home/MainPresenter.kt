package com.cvtracker.vmd.home

import com.cvtracker.vmd.base.AbstractCenterPresenter
import com.cvtracker.vmd.data.Bookmark
import com.cvtracker.vmd.data.DisplayItem
import com.cvtracker.vmd.data.SearchEntry
import com.cvtracker.vmd.master.*
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

                        fun prepareCenters(centers: MutableList<DisplayItem.Center>): List<DisplayItem.Center> {
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
                            filterSections.forEach { section ->
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
                            return centers
                        }

                        /** Add header to show last updated view **/
                        list.add(DisplayItem.LastUpdated(it.lastUpdated))

                        /** Update available vaccine filter type **/
                        updateVaccineFilters(it.availableCenters)

                        val preparedAvailableCenters = prepareCenters(it.availableCenters)
                        val preparedUnavailableCenters = prepareCenters(it.unavailableCenters)

                        if (preparedAvailableCenters.isNotEmpty()) {
                            /** Add header when available centers **/
                            list.add(
                                    DisplayItem.AvailableCenterHeader(
                                            preparedAvailableCenters.size,
                                            preparedAvailableCenters.sumBy { it.appointmentCount },
                                            preparedAvailableCenters.sumBy { it.chronodoseCount })
                            )
                        }

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
            filters = mapVaccine.map { entry ->
                FilterType.Filter(entry.key, entry.value){
                    it.vaccineType?.toList()?.contains(entry.key) ?: false
                }
            }
        )
        filterSections.removeAll { it.id == FILTER_VACCINE_TYPE }
        filterSections.add(section)
    }

    override fun updateFilters(filters: List<FilterType.FilterSection>){
        filterSections = filters.toMutableList()
        loadCenters()
    }

    override fun requestFiltersDialog(){
        view.showFiltersDialog(filterSections)
    }

}