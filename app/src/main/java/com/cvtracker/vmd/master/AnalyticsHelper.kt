package com.cvtracker.vmd.master

import android.os.Bundle
import com.cvtracker.vmd.data.Bookmark
import com.cvtracker.vmd.data.CenterResponse
import com.cvtracker.vmd.data.DisplayItem
import com.cvtracker.vmd.data.SearchEntry
import com.google.firebase.analytics.FirebaseAnalytics

object AnalyticsHelper {

    // SEARCH EVENT
    private const val EVENT_SEARCH_BY_DEPARTMENT = "search_by_departement"
    private const val EVENT_SEARCH_BY_MUNICIPALITY = "search_by_commune"
    private const val EVENT_SEARCH_DATA_DEPARTMENT = "search_departement"
    private const val EVENT_SEARCH_DATA_MUNICIPALITY = "search_commune"
    private const val EVENT_SEARCH_DATA_APPOINTMENTS = "search_nb_doses"
    private const val EVENT_SEARCH_DATA_AVAILABLE_CENTERS = "search_nb_lieu_vaccination"
    private const val EVENT_SEARCH_DATA_UNAVAILABLE_CENTERS = "search_nb_lieu_vaccination_inactive"
    private const val EVENT_SEARCH_DATA_FILTER_TYPE = "search_filter_type"

    // CENTER EVENT
    private const val EVENT_RDV_CLICK = "rdv_click"
    private const val EVENT_RDV_VERIFY_CLICK = "rdv_verify"
    private const val EVENT_BOOKMARK_FAVORITE_CLICK = "bookmark_favorite"
    private const val EVENT_BOOKMARK_NOTIFICATION_CLICK = "bookmark_notification"
    private const val EVENT_BOOKMARK_NONE_CLICK = "bookmark_none"
    private const val EVENT_RDV_DATA_DEPARTMENT = "rdv_departement"
    private const val EVENT_RDV_DATA_NAME = "rdv_name"
    private const val EVENT_RDV_DATA_PLATFORM = "rdv_platform"
    private const val EVENT_RDV_DATA_LOCATION_TYPE = "rdv_location_type"
    private const val EVENT_RDV_DATA_VACCINE = "rdv_vaccine"
    private const val EVENT_RDV_DATA_FILTER_TYPE = "rdv_filter_type"

    private val firebaseAnalytics: FirebaseAnalytics by lazy {
        FirebaseAnalytics.getInstance(ViteMaDoseApp.get())
    }

    fun logEventSearch(searchEntry: SearchEntry, response: CenterResponse, filterType: FilterType) {
        val event = when(searchEntry){
            is SearchEntry.Department -> EVENT_SEARCH_BY_DEPARTMENT
            is SearchEntry.City -> EVENT_SEARCH_BY_MUNICIPALITY
        }

        firebaseAnalytics.logEvent(event, Bundle().apply {
            if(searchEntry is SearchEntry.City) {
                putString(EVENT_SEARCH_DATA_MUNICIPALITY, "${searchEntry.postalCode} - ${searchEntry.name} (${searchEntry.code})")
            }
            putString(EVENT_SEARCH_DATA_DEPARTMENT, searchEntry.entryDepartmentCode)
            putInt(EVENT_SEARCH_DATA_APPOINTMENTS, response.availableCenters.sumBy { it.appointmentCount })
            putInt(EVENT_SEARCH_DATA_AVAILABLE_CENTERS, response.availableCenters.size)
            putInt(EVENT_SEARCH_DATA_UNAVAILABLE_CENTERS, response.unavailableCenters.size)
            putString(EVENT_SEARCH_DATA_FILTER_TYPE, filterTypeValue(filterType))
        })
    }

    fun logEventRdvClick(center: DisplayItem.Center, filterType: FilterType) {
        val event = when (center.available) {
            true -> EVENT_RDV_CLICK
            else -> EVENT_RDV_VERIFY_CLICK
        }
        firebaseAnalytics.logEvent(event, Bundle().apply {
            putString(EVENT_RDV_DATA_DEPARTMENT, center.department)
            putString(EVENT_RDV_DATA_NAME, center.name)
            putString(EVENT_RDV_DATA_PLATFORM, center.platform)
            putString(EVENT_RDV_DATA_LOCATION_TYPE, center.type)
            putString(EVENT_RDV_DATA_VACCINE, center.vaccineType?.joinToString(separator = ","))
            putString(EVENT_RDV_DATA_FILTER_TYPE, filterTypeValue(filterType))
        })
    }

    fun logEventBookmarkClick(center: DisplayItem.Center, filterType: FilterType) {
        val event = when (center.bookmark) {
            Bookmark.NOTIFICATION -> EVENT_BOOKMARK_NOTIFICATION_CLICK
            Bookmark.FAVORITE -> EVENT_BOOKMARK_FAVORITE_CLICK
            else -> EVENT_BOOKMARK_NONE_CLICK
        }
        firebaseAnalytics.logEvent(event, Bundle().apply {
            putString(EVENT_RDV_DATA_DEPARTMENT, center.department)
            putString(EVENT_RDV_DATA_NAME, center.name)
            putString(EVENT_RDV_DATA_PLATFORM, center.platform)
            putString(EVENT_RDV_DATA_LOCATION_TYPE, center.type)
            putString(EVENT_RDV_DATA_VACCINE, center.vaccineType?.joinToString(separator = ","))
            putString(EVENT_RDV_DATA_FILTER_TYPE, filterTypeValue(filterType))
        })
    }

    private fun filterTypeValue(filterType: FilterType) = when (filterType) {
        FilterType.ByDate -> "au plus tot"
        FilterType.ByProximity -> "au plus proche"
    }
}