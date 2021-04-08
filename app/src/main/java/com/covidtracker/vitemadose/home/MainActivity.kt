package com.covidtracker.vitemadose.home

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.covidtracker.vitemadose.R
import com.covidtracker.vitemadose.data.Department
import com.covidtracker.vitemadose.data.DisplayItem
import com.covidtracker.vitemadose.extensions.color
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import java.net.URLEncoder
import java.util.*

class MainActivity : AppCompatActivity(), MainContract.View {

    private val presenter: MainContract.Presenter = MainPresenter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.setBackgroundDrawable(ColorDrawable(color(R.color.grey_2)))

        presenter.loadDepartments()
        presenter.loadCenters()

        refreshLayout.setOnRefreshListener {
            presenter.loadCenters()
        }
    }

    override fun showCenters(list: List<DisplayItem>, lastUpdatedDate: Date?) {
        lastUpdatedDate?.let { date ->
            lastUpdated.visibility = View.VISIBLE
            lastUpdated.text = getString(
                R.string.last_updated, DateFormat.format(
                    "EEEE dd MMMM à kk'h'mm",
                    date
                ).toString().capitalize(Locale.FRANCE)
            )
        } ?: run {
            lastUpdated.visibility = View.GONE
        }

        centersRecyclerView.layoutManager = LinearLayoutManager(this)
        centersRecyclerView.adapter = CenterAdapter(
            context = this,
            items = list,
            onClicked = { center ->
                presenter.onCenterClicked(center)
            },
            onInfoClicked = { center ->
                showInfoDialogForCenter(center)
            })
    }

    private fun showInfoDialogForCenter(center: DisplayItem.Center) {
        val builder = MaterialAlertDialogBuilder(this).apply {
            setTitle(center.name)
            setNeutralButton(R.string.close) { dialog, _ ->
                dialog.dismiss()
            }
            val description = StringBuilder()
            center.metadata?.address?.let {
                setNegativeButton(R.string.maps) { dialog, _ ->
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("geo:0,0?q=${URLEncoder.encode(it, "utf-8")}")
                    }
                    startActivity(intent)
                    dialog.dismiss()
                }
                description.append(it.replace(", ", "\n"))
            }
            center.metadata?.businessHours?.description?.let {
                if(description.isNotBlank()){
                    description.append("\n\n")
                }
                description.append(it)
            }
            center.metadata?.phoneNumber?.let {
                setPositiveButton(R.string.call) { dialog, _ ->
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:$it")
                    }
                    startActivity(intent)
                    dialog.dismiss()
                }
                if(description.isNotBlank()){
                    description.append("\n\n")
                }
                description.append("Téléphone : $it")
            }
            setMessage(description.toString())
        }
        builder.create().show()
    }

    override fun setLoading(loading: Boolean) {
        refreshLayout.isRefreshing = loading
    }

    override fun setupSelector(items: List<Department>, indexSelected: Int) {
        val array = items.map { "${it.departmentCode} - ${it.departmentName}" }.toTypedArray()
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
            "${department.departmentCode} - ${department.departmentName}"
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