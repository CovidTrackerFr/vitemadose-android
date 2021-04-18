package com.cvtracker.vmd.home

import com.cvtracker.vmd.data.DisplayItem
import com.cvtracker.vmd.data.SearchEntry
import com.cvtracker.vmd.master.AnalyticsHelper

interface MainContract {

    interface View {

        /**
         * Show empty state
         */
        fun showEmptyState()

        /**
         * Display main list of centers (available/unavailable)
         */
        fun showCenters(list: List<DisplayItem>, filter: AnalyticsHelper.FilterType?)

        /**
         * Setup department selectors with retrieved departments
         */
        fun setupSelector(items: List<SearchEntry>)

        /**
         * Open link
         */
        fun openLink(url: String)

        /**
         * Notify an error occurs while retrieving centers
         */
        fun showCentersError()

        /**
         * Notify an error occurs while retrieving suggestions
         */
        fun showSearchError()

        /**
         * Notify centers are loading
         */
        fun setLoading(loading: Boolean)

        /**
         * Display the entry in the selector
         */
        fun displaySelectedSearchEntry(entry: SearchEntry?)
    }

    interface Presenter {

        /**
         * Load centers for the saved department
         */
        fun loadCenters()

        /**
         * Called when a center is clicked
         */
        fun onCenterClicked(center: DisplayItem.Center)

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
        fun onFilterChanged(filter: AnalyticsHelper.FilterType)
    }
}