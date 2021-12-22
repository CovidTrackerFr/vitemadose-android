package com.cvtracker.vmd.home

import android.text.format.DateFormat
import com.cvtracker.vmd.R
import com.cvtracker.vmd.base.AbstractCenterPresenter
import com.cvtracker.vmd.data.*
import com.cvtracker.vmd.master.*
import com.cvtracker.vmd.master.FilterType.Companion.FILTER_DISTANCE_SECTION
import com.cvtracker.vmd.master.FilterType.Companion.FILTER_VACCINE_TYPE_SECTION
import kotlinx.coroutines.*
import timber.log.Timber
import java.util.*

class MainPresenter(override val view: MainContract.View) : AbstractCenterPresenter(view), MainContract.Presenter {

    private var jobSearch: Job? = null
    private var jobCenters: Job? = null

    private val sortType = SortType.ByProximity

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
        jobCenters = launch(Dispatchers.Main) {
            PrefHelper.favEntry?.let { entry ->
                try {
                    val isCitySearch = entry is SearchEntry.City

                    view.removeEmptyStateIfNeeded()
                    view.setLoading(true)

                    DataManager.getCenters(
                        departmentCode = entry.entryDepartmentCode,
                        useNearDepartment = isCitySearch
                    ).let { response ->

                        val calendar = Calendar.getInstance()
                        calendar.add(Calendar.DAY_OF_YEAR, -1)

                        val listTitleHeaders = mutableListOf<String>()
                        val listTabHeader = mutableListOf<TabHeaderItem>()
                        val flattenAvailableCenters = mutableListOf<DisplayItem.Center>()

                        val listCenterByDays = withContext(Dispatchers.IO) {
                            val filteredResponseList = if(PrefHelper.isNewSystem){
                                /** Construct filtered response list based on the day **/
                                 (0 until 60).map {
                                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                                    val dateKey = DateFormat.format("yyyy-MM-dd", calendar).toString()
                                    val filteredResponse = response.applyDailyFilters(dateKey, PrefHelper.tagType.key)
                                    listTitleHeaders.add(DateFormat.format("d MMM", calendar).toString())
                                    flattenAvailableCenters.addAll(filteredResponse.availableCenters)
                                    filteredResponse
                                }
                            }else {
                                /** The regular response not filtered **/
                                listTitleHeaders.add("Tout")
                                flattenAvailableCenters.addAll(response.availableCenters)
                                listOf(response)
                            }

                            /** Update available vaccine filter type **/
                            updateVaccineFilters(flattenAvailableCenters)

                            /** Build display list **/
                            filteredResponseList.mapIndexed { index, centerResponse ->
                                val list = buildCentersList(centerResponse, entry)
                                listTabHeader.add(
                                    TabHeaderItem(
                                        header = listTitleHeaders[index],
                                        count = list.filterIsInstance<DisplayItem.AvailableCenterHeader>().sumBy { it.slotsCount }
                                    ))
                                list
                            }
                        }

                        view.updateFilterState(isDefaultFilters())
                        view.showCenters(listCenterByDays, if(PrefHelper.isNewSystem) PrefHelper.tagType else null)
                        view.showTabs(if(PrefHelper.isNewSystem) listTabHeader else null)
                        AnalyticsHelper.logEventSearch(entry, response, sortType)
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

    /** Build display list for the given @param response **/
    private fun buildCentersList(
        response: CenterResponse,
        entry: SearchEntry
    ): List<DisplayItem>{

        /** Here is the magic ! **/
        val availableCenters = response.availableCenters
        val unavailableCenters = response.unavailableCenters

        val list = mutableListOf<DisplayItem>()

        /** Add header to show last updated view **/
        list.add(DisplayItem.LastUpdated(response.lastUpdated, disclaimer))

        /** First pass preparing centers status, and secondary filters **/
        val preparedAvailableCenters = prepareCenters(availableCenters, entry)
        val preparedUnavailableCenters = prepareCenters(unavailableCenters, entry)

        /** Add statistics header **/
        list.add(
            DisplayItem.AvailableCenterHeader(
                preparedAvailableCenters.sumBy { it.appointmentCount },
                preparedAvailableCenters.count(),
            )
        )

        /** Second pass, primary filters.
         * Primary filters modify the list but we have to keep the statistics info (above) for the header
         * That's why we do it here **/
        applyFilters(preparedAvailableCenters, filterSections.filter { it.primaryFilter })
        applyFilters(preparedUnavailableCenters, filterSections.filter { it.primaryFilter })

        list.addAll(preparedAvailableCenters)
        list.addAll(preparedUnavailableCenters)

        return list
    }

    /** Prepare centers, calculating distance, applying filters **/
    private fun prepareCenters(centers: MutableList<DisplayItem.Center>, entry: SearchEntry): MutableList<DisplayItem.Center> {
        /** Set up distance when city search **/
        if (entry is SearchEntry.City) {
            centers.onEach {
                it.calculateDistance(entry)
            }
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


    override fun onSearchEntrySelected(searchEntry: SearchEntry) {
        if (searchEntry.entryCode != PrefHelper.favEntry?.entryCode) {
            PrefHelper.favEntry = searchEntry
            view.showCenters(emptyList(), if(PrefHelper.isNewSystem) PrefHelper.tagType else null)
        }
        loadCenters()
    }

    override fun getSavedSearchEntry(): SearchEntry? {
        return PrefHelper.favEntry
    }

    override fun onTagTypeChanged(tagType: TagType) {
        PrefHelper.tagType = tagType
        view.showCenters(emptyList(), if(PrefHelper.isNewSystem) PrefHelper.tagType else null)
        loadCenters()
    }

    override fun onSearchUpdated(search: String) {
        jobSearch?.cancel()
        if (search.length < 2) {
            /** reset entry list **/
            view.setupSelector(emptyList())
            return
        }
        jobSearch = launch(Dispatchers.Main) {
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
                if(list.size > 0){
                    view.displaySelectorDropdown()
                }
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
            when (section.id) {
                FILTER_DISTANCE_SECTION -> {
                    section.filters.filter { it.enabled }.forEach { filter ->
                        centers.removeAll { filter.predicate(it, filter).not() }
                    }
                }
                FILTER_VACCINE_TYPE_SECTION -> {
                    centers.removeAll { center ->
                        val filters = section.filters.filter { center.vaccineType?.contains(it.displayTitle) ?: false }
                        /** We want to remove centers with vaccineList items ALL disabled **/
                        if(filters.isEmpty()){
                            false
                        }else {
                            filters.all {
                                if (it.enabled.not()) it.predicate(center,it) else it.predicate(center, it).not()
                            }
                        }
                    }
                }
                else -> {
                    section.filters.forEach { filter ->
                        if(section.defaultState.not() && filter.enabled){
                            centers.removeAll { filter.predicate(it, filter).not() }
                        }else if(section.defaultState && !filter.enabled){
                            centers.removeAll { filter.predicate(it, filter) }
                        }
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

    override fun removeDisclaimer(){
        disclaimer?.let {
            PrefHelper.lastDisclaimerClosedTimestamp = Date().time
            PrefHelper.lastDisclaimerClosedMessage = it.message
            disclaimer = null
        }
    }
}
