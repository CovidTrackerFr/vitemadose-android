package com.covidtracker.vitemadose.home

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.covidtracker.vitemadose.R
import com.covidtracker.vitemadose.data.Department
import com.covidtracker.vitemadose.data.DisplayItem
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), MainContract.View {

    private val presenter: MainContract.Presenter = MainPresenter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        presenter.loadDepartments()
        presenter.loadCenters()
    }

    override fun showCenters(list: List<DisplayItem>) {
        centersRecyclerView.layoutManager = LinearLayoutManager(this)
        centersRecyclerView.adapter = CenterAdapter(this, list) { center, index ->
            presenter.onCenterClicked(center)
        }
    }

    override fun setupSelector(items: List<Department>, indexSelected: Int) {
        val array = items.map { "${it.codeDepartement} - ${it.nomDepartement}" }.toTypedArray()
        departmentSelector.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(R.string.choose_department_title)
                .setItems(array) { dialogInterface, index ->
                    presenter.onDepartmentSelected(items[index])
                    displaySelectedDepartment(items[index])
                    dialogInterface.dismiss()
                }.create().show()
        }
        displaySelectedDepartment(items.getOrNull(indexSelected))
    }

    private fun displaySelectedDepartment(department: Department?) {
        selectedDepartment.text = if (department != null) {
            "${department.codeDepartement} - ${department.nomDepartement}"
        } else {
            getString(R.string.choose_department_title)
        }
    }

    override fun openLink(url: String) {
        try {
            startActivity(Intent().apply {
                action = Intent.ACTION_VIEW
                data = Uri.parse(url)
            })
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }

    override fun showCentersError() {
        Snackbar.make(container, getString(R.string.centers_error), Snackbar.LENGTH_SHORT).show()
    }
}