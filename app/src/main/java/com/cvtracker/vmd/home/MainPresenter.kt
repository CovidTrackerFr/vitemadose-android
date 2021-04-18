package com.cvtracker.vmd.home

import com.cvtracker.vmd.R
import com.cvtracker.vmd.data.DisplayItem
import com.cvtracker.vmd.data.SearchEntry
import com.cvtracker.vmd.master.AnalyticsHelper
import com.cvtracker.vmd.master.DataManager
import com.cvtracker.vmd.master.PrefHelper
import kotlinx.coroutines.*
import timber.log.Timber

class MainPresenter(private val view: MainContract.View) : MainContract.Presenter {

    private var jobSearch: Job? = null
    private var jobCenters: Job? = null

    private var selectedFilter: AnalyticsHelper.FilterType? = null

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
                    val filter = selectedFilter ?: entry.defaultFilterType
                    val isCitySearch = entry is SearchEntry.City

                    view.setLoading(true)

                    DataManager.getCenters(
                        departmentCode = entry.entryDepartmentCode,
                        useNearDepartment = isCitySearch
                    ).let {
                        val list = mutableListOf<DisplayItem>()

                        val availableCenters = it.availableCenters.toMutableList()
                        /** Add header to show last updated view **/
                        list.add(DisplayItem.LastUpdated(it.lastUpdated))
                        if (availableCenters.isNotEmpty()) {
                            /** Add header when available centers **/
                            list.add(
                                DisplayItem.AvailableCenterHeader(
                                    availableCenters.size,
                                    availableCenters.sumBy { it.appointmentCount })
                            )
                            /** Set up distance when city search **/
                            if (isCitySearch) {
                                availableCenters.onEach { it.calculateDistance(entry as SearchEntry.City) }
                            }
                            /** Sort results **/
                            when (filter) {
                                AnalyticsHelper.FilterType.ByProximity -> {
                                    availableCenters.sortBy { it.distance }
                                }
                                AnalyticsHelper.FilterType.ByDate -> {
                                    availableCenters.sortBy { it.nextSlot }
                                }
                            }
                            list.addAll(availableCenters.onEach { it.available = true })
                        }

                        val unavailableCenters = it.unavailableCenters.toMutableList()
                        if (unavailableCenters.isNotEmpty()) {
                            /** Add the right header with  unavailable centers **/
                            if (availableCenters.isEmpty()) {
                                list.add(DisplayItem.UnavailableCenterHeader(R.string.no_slots_available_center_header))
                            } else {
                                list.add(DisplayItem.UnavailableCenterHeader(R.string.no_slots_available_center_header_others))
                            }
                            /** Set up distance when city search **/
                            if (isCitySearch) {
                                unavailableCenters.onEach { it.calculateDistance(entry as SearchEntry.City) }
                            }
                            /** Sort results **/
                            when (filter) {
                                AnalyticsHelper.FilterType.ByProximity -> {
                                    unavailableCenters.sortBy { it.distance }
                                }
                                AnalyticsHelper.FilterType.ByDate -> {
                                    unavailableCenters.sortBy { it.nextSlot }
                                }
                            }
                            list.addAll(unavailableCenters.onEach { it.available = false })
                        }
                        view.showCenters(list, if (isCitySearch) filter else null)
                        AnalyticsHelper.logEventSearch(entry, it, filter)
                    }
                } catch (e: CancellationException) {
                    /** Couroutine has been canceled => Ignore**/
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
        selectedFilter = null
        loadCenters()
    }

    override fun getSavedSearchEntry(): SearchEntry? {
        return PrefHelper.favEntry
    }

    override fun onCenterClicked(center: DisplayItem.Center) {
        view.openLink(center.url)
        AnalyticsHelper.logEventRdvClick(center, AnalyticsHelper.FilterType.ByDate)
    }

    override fun onFilterChanged(filter: AnalyticsHelper.FilterType) {
        selectedFilter = filter
        view.showCenters(emptyList(), filter)
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
                if (search.substring(0, 1).toIntOrNull() != null) {
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
                /** Couroutine has been canceled => Ignore**/
            } catch (e: Exception) {
                Timber.e(e)
                view.showSearchError()
            }
        }
    }

}