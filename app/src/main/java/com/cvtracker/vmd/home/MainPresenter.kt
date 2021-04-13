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

    override fun loadDepartments() {
        if (PrefHelper.favDepartmentCode == null) {
            view.showEmptyState()
        }

        GlobalScope.launch(Dispatchers.Main) {
            val items = mutableListOf<Department>()
            try {
                items.addAll(DataManager.getDepartments().apply {
                    PrefHelper.cacheDepartmentList = this
                })
            } catch (e: Exception) {
                Timber.e(e)
                PrefHelper.cacheDepartmentList?.let { items.addAll(it) }
            } finally {
                view.setupSelector(
                    items,
                    items.indexOfFirst { PrefHelper.favDepartmentCode == it.departmentCode })
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