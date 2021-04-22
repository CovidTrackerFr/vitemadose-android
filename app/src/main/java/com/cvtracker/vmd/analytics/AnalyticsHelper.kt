package com.cvtracker.vmd.analytics

import android.app.Application
import com.cvtracker.vmd.data.CenterResponse
import com.cvtracker.vmd.data.DisplayItem

interface AnalyticsHelper {
    enum class FilterType(val value: String) {
        ByDate("au plus tot"),
        ByProximity("au plus proche");
    }

    fun initApp(app: Application)
    fun logEventSearchByDepartment(department: String, response: CenterResponse, filterType: FilterType)
    fun logEventRdvClick(center: DisplayItem.Center, filterType: FilterType)
}