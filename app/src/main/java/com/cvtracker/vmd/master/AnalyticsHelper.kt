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

    // CENTER EVENTS
    private const val EVENT_RDV_CLICK = "rdv_click"
    private const val EVENT_RDV_VERIFY_CLICK = "rdv_verify"
    private const val EVENT_RDV_DATA_DEPARTMENT = "rdv_departement"
    private const val EVENT_RDV_DATA_NAME = "rdv_name"
    private const val EVENT_RDV_DATA_PLATFORM = "rdv_platform"
    private const val EVENT_RDV_DATA_LOCATION_TYPE = "rdv_location_type"
    private const val EVENT_RDV_DATA_VACCINE = "rdv_vaccine"
    private const val EVENT_RDV_DATA_FILTER_TYPE = "rdv_filter_type"

    // BOOKMARK EVENTS
    private const val EVENT_BOOKMARK_FAVORITE_CLICK = "bookmark_favorites"
    private const val EVENT_BOOKMARK_NOTIFICATION_CHRONODOSE_CLICK = "bookmark_notification_chronodose"
    private const val EVENT_BOOKMARK_NOTIFICATION_CLICK = "bookmark_notification"
    private const val EVENT_BOOKMARK_NONE_CLICK = "bookmark_remove"
    private const val EVENT_BOOKMARK_DATA_FROM = "bookmark_from"
    private const val EVENT_BOOKMARK_DATA_FROM_VALUE_NONE = "none"
    private const val EVENT_BOOKMARK_DATA_FROM_VALUE_FAVORITE = "favorite"
    private const val EVENT_BOOKMARK_DATA_FROM_VALUE_NOTIFICATION = "notification"
    private const val EVENT_BOOKMARK_DATA_FROM_VALUE_NOTIFICATION_CHRONODOSE = "notification_chronodose"

    // NOTIFICATION EVENTS
    private const val EVENT_NOTIFICATION_RECEIVE = "notif_receive"
    private const val EVENT_NOTIFICATION_OPEN = "notif_open"
    private const val EVENT_NOTIFICATION_UNSUBSCRIBE = "notif_unsubscribe"
    private const val EVENT_NOTIFICATION_DATA_DEPARTMENT = "notif_department"
    private const val EVENT_NOTIFICATION_DATA_CENTER = "notif_center"
    private const val EVENT_NOTIFICATION_DATA_TOPIC = "notif_topic"
    private const val EVENT_NOTIFICATION_DATA_TYPE = "notif_type"

    private val firebaseAnalytics: FirebaseAnalytics by lazy {
        FirebaseAnalytics.getInstance(ViteMaDoseApp.get())
    }

    fun logEventSearch(searchEntry: SearchEntry, response: CenterResponse, filterType: SortType) {
        val event = when (searchEntry) {
            is SearchEntry.Department -> EVENT_SEARCH_BY_DEPARTMENT
            is SearchEntry.City -> EVENT_SEARCH_BY_MUNICIPALITY
        }

        firebaseAnalytics.logEvent(event, Bundle().apply {
            if (searchEntry is SearchEntry.City) {
                putString(EVENT_SEARCH_DATA_MUNICIPALITY, "${searchEntry.postalCode} - ${searchEntry.name} (${searchEntry.code})")
            }
            putString(EVENT_SEARCH_DATA_DEPARTMENT, searchEntry.entryDepartmentCode)
            putInt(EVENT_SEARCH_DATA_APPOINTMENTS, response.availableCenters.sumBy { it.appointmentCount })
            putInt(EVENT_SEARCH_DATA_AVAILABLE_CENTERS, response.availableCenters.size)
            putInt(EVENT_SEARCH_DATA_UNAVAILABLE_CENTERS, response.unavailableCenters.size)
            putString(EVENT_SEARCH_DATA_FILTER_TYPE, filterTypeValue(filterType))
        })
    }

    fun logEventRdvClick(center: DisplayItem.Center, filterType: SortType) {
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

    fun logEventBookmarkClick(center: DisplayItem.Center, filterType: SortType, fromBookmark: Bookmark) {
        val event = when (center.bookmark) {
            Bookmark.NOTIFICATION_CHRONODOSE -> EVENT_BOOKMARK_NOTIFICATION_CHRONODOSE_CLICK
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
            putString(EVENT_BOOKMARK_DATA_FROM, when (fromBookmark) {
                Bookmark.NOTIFICATION_CHRONODOSE -> EVENT_BOOKMARK_DATA_FROM_VALUE_NOTIFICATION_CHRONODOSE
                Bookmark.NOTIFICATION -> EVENT_BOOKMARK_DATA_FROM_VALUE_NOTIFICATION
                Bookmark.FAVORITE -> EVENT_BOOKMARK_DATA_FROM_VALUE_FAVORITE
                else -> EVENT_BOOKMARK_DATA_FROM_VALUE_NONE
            })
        })
    }

    fun logEventNotificationReceive(department: String, centerId: String, topic: String, type: String) {
        logEventNotification(EVENT_NOTIFICATION_RECEIVE, department, centerId, topic, type)
    }

    fun logEventNotificationOpen(department: String, centerId: String, topic: String, type: String) {
        logEventNotification(EVENT_NOTIFICATION_OPEN, department, centerId, topic, type)
    }

    fun logEventNotificationUnsubscribe(department: String, centerId: String, topic: String, type: String) {
        logEventNotification(EVENT_NOTIFICATION_UNSUBSCRIBE, department, centerId, topic, type)
    }

    private fun logEventNotification(event: String, department: String, centerId: String, topic: String, type: String) {
        firebaseAnalytics.logEvent(event, Bundle().apply {
            putString(EVENT_NOTIFICATION_DATA_DEPARTMENT, department)
            putString(EVENT_NOTIFICATION_DATA_CENTER, centerId)
            putString(EVENT_NOTIFICATION_DATA_TOPIC, topic)
            putString(EVENT_NOTIFICATION_DATA_TYPE, type)
        })
    }

    private fun filterTypeValue(filterType: SortType) = when (filterType) {
        SortType.ByDate -> "au plus tot"
        SortType.ByProximity -> "au plus proche"
    }
}