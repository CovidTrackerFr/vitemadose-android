package com.covidtracker.vitemadose.home

import com.covidtracker.vitemadose.R
import com.covidtracker.vitemadose.data.Department
import com.covidtracker.vitemadose.data.DisplayItem
import com.covidtracker.vitemadose.master.DataManager
import com.covidtracker.vitemadose.master.PrefHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber

class MainPresenter(private val view: MainContract.View) : MainContract.Presenter {

    override fun loadCenters() {
        GlobalScope.launch(Dispatchers.Main) {
            PrefHelper.favDepartmentCode?.let { department ->
                try {
                    view.setLoading(true)
                    DataManager.getCenters(department).let {
                        val list = mutableListOf<DisplayItem>()
                        list.add(DisplayItem.LastUpdated(it.lastUpdated))
                        if (it.availableCenters.isNotEmpty()) {
                            list.add(DisplayItem.AvailableCenterHeader(it.availableCenters.size, it.availableCenters.sumBy { it.appointmentCount }))
                            list.addAll(it.availableCenters.onEach { it.available = true })
                        }
                        if (it.unavailableCenters.isNotEmpty()) {
                            if (list.isEmpty()) {
                                list.add(DisplayItem.UnavailableCenterHeader(R.string.no_slots_available_center_header))
                            } else {
                                list.add(DisplayItem.UnavailableCenterHeader(R.string.no_slots_available_center_header_others))
                            }
                            list.addAll(it.unavailableCenters.onEach { it.available = false })
                        }
                        view.showCenters(list)
                    }
                } catch (e: Exception) {
                    Timber.e(e)
                    view.showCentersError()
                } finally {
                    view.setLoading(false)
                }
            }
        }
    }

    override fun loadDepartments() {
        if (PrefHelper.favDepartmentCode == null) {
            view.showEmptyState()
        }

        GlobalScope.launch(Dispatchers.Main) {
            try {
                val items = DataManager.getDepartments()
                view.setupSelector(
                    items,
                    items.indexOfFirst { PrefHelper.favDepartmentCode == it.departmentCode })
            } catch (e: Exception) {
                Timber.e(e)
                /** Do we want to display an error if we have departments cache ? **/
            }
        }
    }

    override fun onDepartmentSelected(department: Department) {
        if (department.departmentCode != PrefHelper.favDepartmentCode) {
            PrefHelper.favDepartmentCode = department.departmentCode
            view.showCenters(emptyList())
        }
        loadCenters()
    }

    override fun onCenterClicked(center: DisplayItem.Center) {
        view.openLink(center.url)
    }

}