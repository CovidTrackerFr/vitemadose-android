package com.cvtracker.vmd.analytics

import android.app.Application
import com.cvtracker.vmd.data.CenterResponse
import com.cvtracker.vmd.data.DisplayItem

object AnalyticsHelperImpl : AnalyticsHelper {
    override fun initApp(app: Application) {}

    override fun logEventSearchByDepartment(
        department: String,
        response: CenterResponse,
        filterType: AnalyticsHelper.FilterType
    ) {}

    override fun logEventRdvClick(
        center: DisplayItem.Center,
        filterType: AnalyticsHelper.FilterType
    ) {}
}