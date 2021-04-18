package com.cvtracker.vmd.home

import com.cvtracker.vmd.R
import com.cvtracker.vmd.data.Department
import com.cvtracker.vmd.data.DisplayItem
import com.cvtracker.vmd.master.DataManager
import com.cvtracker.vmd.master.PrefHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber

class MainPresenter(private val view: MainContract.View) : MainContract.Presenter {

    override fun loadInitialState() {
        PrefHelper.favDepartment.let { department ->
            view.displaySelectedDepartment(department)
            if (department == null) {
                view.showEmptyState()
            }
        }
    }

    override fun loadCenters() {
        GlobalScope.launch(Dispatchers.Main) {
            PrefHelper.favDepartment?.let { department ->
                try {
                    view.setLoading(true)
                    DataManager.getCenters(department.departmentCode).let {
                        val list = mutableListOf<DisplayItem>()
                        list.add(DisplayItem.LastUpdated(it.lastUpdated))
                        if (it.availableCenters.isNotEmpty()) {
                            list.add(DisplayItem.AvailableCenterHeader(it.availableCenters.size, it.availableCenters.sumBy { it.appointmentCount }))
                            list.addAll(it.availableCenters.onEach { it.available = true })
                        }
                        if (it.unavailableCenters.isNotEmpty()) {
                            if (it.availableCenters.isEmpty()) {
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

    override fun onDepartmentSelected(department: Department) {
        if (department.departmentCode != PrefHelper.favDepartment?.departmentCode) {
            PrefHelper.favDepartment = department
            view.showCenters(emptyList())
        }
        loadCenters()
    }

    override fun getSavedDepartment(): Department? {
        return PrefHelper.favDepartment
    }

    override fun onCenterClicked(center: DisplayItem.Center) {
        view.openLink(center.url)
    }

    override fun onSearchUpdated(search: String) {
        GlobalScope.launch(Dispatchers.Main) {
            val list = mutableListOf<Department>()
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
        }
    }

}