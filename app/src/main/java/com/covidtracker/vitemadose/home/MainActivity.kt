package com.covidtracker.vitemadose.home

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.covidtracker.vitemadose.R
import com.covidtracker.vitemadose.data.Department
import com.covidtracker.vitemadose.data.DisplayItem
import com.covidtracker.vitemadose.extensions.color
import com.covidtracker.vitemadose.extensions.dpToPx
import com.covidtracker.vitemadose.extensions.hide
import com.covidtracker.vitemadose.extensions.show
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import java.net.URLEncoder


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

        appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            val progress = -verticalOffset / appBarLayout.measuredHeight.toFloat()
            val width = (resources.dpToPx(40f) * progress).toInt()
            selectorCollapsedIconView.apply {
                /** Constraint the width of the icon **/
                if (width == 0) hide() else show()
                (layoutParams as ConstraintLayout.LayoutParams).matchConstraintMaxWidth = width
                alpha = progress
                requestLayout()
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
                ValueAnimator.ofObject(
                    ArgbEvaluator(),
                    color(R.color.grey_2),
                    color(R.color.corail)
                ).apply {
                    setCurrentFraction(progress)
                    backgroundSelectorView.setBackgroundColor(animatedValue as Int)
                }
            }
        })
    }

    override fun showCenters(list: List<DisplayItem>) {
        centersRecyclerView.layoutManager = LinearLayoutManager(this)
        centersRecyclerView.adapter = CenterAdapter(
            context = this,
            items = list,
            onClicked = { presenter.onCenterClicked(it) },
            onAddressClicked = { startMapsActivity(it) },
            onPhoneClicked = { startPhoneActivity(it) }
        )
    }

    private fun startPhoneActivity(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
        }
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, R.string.no_app_activity_found, Toast.LENGTH_SHORT).show()
        }
    }

    private fun startMapsActivity(address: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("geo:0,0?q=${URLEncoder.encode(address, "utf-8")}")
        }
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, R.string.no_app_activity_found, Toast.LENGTH_SHORT).show()
        }
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