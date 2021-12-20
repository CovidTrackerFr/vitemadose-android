package com.cvtracker.vmd.home

import com.cvtracker.vmd.base.CenterContract
import com.cvtracker.vmd.data.SearchEntry
import com.cvtracker.vmd.data.TabHeaderItem
import com.cvtracker.vmd.master.FilterType
import com.cvtracker.vmd.master.TagType

interface MainContract {

    interface View : CenterContract.View {

        /**
         * Show empty state
         */
        fun showEmptyState()

        /**
         * Setup department selectors with retrieved departments
         */
        fun setupSelector(items: List<SearchEntry>)

        /**
         * Setup department selectors with retrieved departments
         */
        fun displaySelectorDropdown()


        /**
         * Notify an error occurs while retrieving suggestions
         */
        fun showSearchError()

        /**
         * Display the entry in the selector
         */
        fun displaySelectedSearchEntry(entry: SearchEntry?)

        fun removeEmptyStateIfNeeded()

        fun showBookmarks(department: String? = null, centerId: String? = null)

        fun showFiltersDialog(filterSections: MutableList<FilterType.FilterSection>)

        fun updateFilterState(defaultFilters: Boolean)

        fun showTabs(listTabHeader: List<TabHeaderItem>?)
    }

    interface Presenter : CenterContract.Presenter {

        /**
         * Load centers for the saved department
         */
        fun loadCenters()

        /**
         * Called when a department is selected via the selector
         */
        fun onSearchEntrySelected(searchEntry: SearchEntry)

        /**
         * Get saved search entry
         */
        fun getSavedSearchEntry(): SearchEntry?

        /**
         * Called when the search has been modified
         */
        fun onSearchUpdated(search: String)

        /**
         * load initial state (empty state, fav search entry,..;)
         */
        fun loadInitialState()

        /**
         * Called when the filter has been modified
         */
        fun onTagTypeChanged(tagType: TagType)

        fun handleDeepLink(data: String)

        fun updateFilters(filters: List<FilterType.FilterSection>, needRefresh: Boolean = true)

        fun resetFilters(needRefresh: Boolean = true)

        fun getFilters(): List<FilterType.FilterSection>

        fun removeDisclaimer()
    }
}