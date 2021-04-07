package com.covidtracker.vitemadose.home

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
                    DataManager.getCenters(department).let {
                        val list = mutableListOf<DisplayItem>()
                        if (it.availableCenters.isNotEmpty()) {
                            list.add(DisplayItem.AvailableCenterHeader(it.availableCenters.size))
                            list.addAll(it.availableCenters.onEach { it.available = true })
                        }
                        if (it.unavailableCenters.isNotEmpty()) {
                            list.add(DisplayItem.UnavailableCenterHeader)
                            list.addAll(it.unavailableCenters.onEach { it.available = false })
                        }
                        view.showCenters(list)
                    }
                }catch (e: Exception){
                    Timber.e(e)
                    view.showCentersError()
                }
            }
        }
    }

    override fun loadDepartments() {
        GlobalScope.launch(Dispatchers.Main) {
            try{
                val items = DataManager.getDepartments()
                view.setupSelector(
                    items,
                    items.indexOfFirst { PrefHelper.favDepartmentCode == it.codeDepartement })
            }catch (e: Exception){
                Timber.e(e)
                /** Do we want to display an error if we have departments cache ? **/
            }
        }
    }

    override fun onDepartmentSelected(department: Department) {
        PrefHelper.favDepartmentCode = department.codeDepartement
        loadCenters()
    }

    override fun onCenterClicked(center: DisplayItem.Center) {
        view.openLink(center.url)
    }

}