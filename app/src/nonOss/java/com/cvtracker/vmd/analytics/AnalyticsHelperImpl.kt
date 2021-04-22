package com.cvtracker.vmd.analytics

import android.app.Application
import android.os.Bundle
import com.cvtracker.vmd.data.CenterResponse
import com.cvtracker.vmd.data.DisplayItem
import com.cvtracker.vmd.master.DataManager
import com.cvtracker.vmd.master.ViteMaDoseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.remoteconfig.BuildConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import timber.log.Timber

object AnalyticsHelperImpl : AnalyticsHelper {

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
    private const val EVENT_RDV_DATA_DEPARTMENT = "rdv_departement"
    private const val EVENT_RDV_DATA_NAME = "rdv_name"
    private const val EVENT_RDV_DATA_PLATFORM = "rdv_platform"
    private const val EVENT_RDV_DATA_LOCATION_TYPE = "rdv_location_type"
    private const val EVENT_RDV_DATA_VACCINE = "rdv_vaccine"
    private const val EVENT_RDV_DATA_FILTER_TYPE = "rdv_filter_type"

    // KEYS
    private const val URL_BASE_KEY = "url_base"
    private const val PATH_LIST_DEPARTMENTS_KEY = "path_list_departments"
    private const val PATH_DATA_DEPARTMENT_KEY = "path_data_department"
    private const val PATH_STATS_KEY = "path_stats"

    private val firebaseAnalytics: FirebaseAnalytics by lazy {
        FirebaseAnalytics.getInstance(ViteMaDoseApp.get())
    }

    override fun initApp(app: Application) {
        loadCacheFirebaseConfig()

        FirebaseRemoteConfig.getInstance().apply {
            setConfigSettingsAsync(remoteConfigSettings {
                minimumFetchIntervalInSeconds = if (BuildConfig.DEBUG) 0 else 3600
            })
            fetchAndActivate().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    loadCacheFirebaseConfig()
                }
            }
        }
    }

    private fun loadCacheFirebaseConfig() {
        FirebaseRemoteConfig.getInstance().apply {
            getString(URL_BASE_KEY).takeIf { it.isNotBlank() }?.let {
                DataManager.URL_BASE = it
                Timber.d("RemoteConfig set URL_BASE = $it")
            }
            getString(PATH_LIST_DEPARTMENTS_KEY).takeIf { it.isNotBlank() }?.let {
                DataManager.PATH_LIST_DEPARTMENTS = it
                Timber.d("RemoteConfig set PATH_LIST_DEPARTMENTS = $it")
            }
            getString(PATH_DATA_DEPARTMENT_KEY).takeIf { it.isNotBlank() }?.let {
                DataManager.PATH_DATA_DEPARTMENT = it
                Timber.d("RemoteConfig set PATH_DATA_DEPARTMENT = $it")
            }
            getString(PATH_STATS_KEY).takeIf { it.isNotBlank() }?.let {
                DataManager.PATH_STATS = it
                Timber.d("RemoteConfig set PATH_STATS = $it")
            }
        }
    }


    override fun logEventSearchByDepartment(
        department: String,
        response: CenterResponse,
        filterType: AnalyticsHelper.FilterType
    ) {
        firebaseAnalytics.logEvent(EVENT_SEARCH_BY_DEPARTMENT, Bundle().apply {
            putString(EVENT_SEARCH_DATA_DEPARTMENT, department)
            putInt(EVENT_SEARCH_DATA_APPOINTMENTS, response.availableCenters.sumBy { it.appointmentCount })
            putInt(EVENT_SEARCH_DATA_AVAILABLE_CENTERS, response.availableCenters.size)
            putInt(EVENT_SEARCH_DATA_UNAVAILABLE_CENTERS, response.unavailableCenters.size)
            putString(EVENT_SEARCH_DATA_FILTER_TYPE, filterType.value)
        })
    }

    override fun logEventRdvClick(
        center: DisplayItem.Center,
        filterType: AnalyticsHelper.FilterType
    ) {
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
            putString(EVENT_RDV_DATA_FILTER_TYPE, filterType.value)
        })
    }
}