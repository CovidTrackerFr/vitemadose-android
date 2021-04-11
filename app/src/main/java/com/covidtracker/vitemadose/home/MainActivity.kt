package com.covidtracker.vitemadose.home

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.covidtracker.vitemadose.R
import com.covidtracker.vitemadose.about.AboutActivity
import com.covidtracker.vitemadose.data.Department
import com.covidtracker.vitemadose.data.DisplayItem
import com.covidtracker.vitemadose.extensions.color
import com.covidtracker.vitemadose.extensions.hide
import com.covidtracker.vitemadose.extensions.launchWebUrl
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.empty_state.*
import kotlinx.android.synthetic.main.empty_state.view.*
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

        aboutIconView.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }

        appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            val progress = (-verticalOffset / headerLayout.measuredHeight.toFloat()) * 1.5f
            headerLayout.alpha = 1 - progress
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
                ValueAnimator.ofObject(
                    ArgbEvaluator(),
                    color(R.color.mine_shaft),
                    color(R.color.white)
                ).apply {
                    setCurrentFraction(progress)
                    aboutIconView.imageTintList = ColorStateList.valueOf(animatedValue as Int)
                }
                ValueAnimator.ofObject(
                    ArgbEvaluator(),
                    color(R.color.grey_2),
                    color(R.color.corail)
                ).apply {
                    setCurrentFraction(progress)
                    backgroundSelectorView.setBackgroundColor(animatedValue as Int)
                    appBarLayout.setBackgroundColor(animatedValue as Int)
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

        emptyStateContainer?.hide()
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
        arrayOf(emptyStateDepartmentSelector, departmentSelector).filterNotNull()
            .forEach { selector ->
                selector.setOnClickListener {
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
    }

    override fun showEmptyState() {
        stubEmptyState.setOnInflateListener { stub, inflated ->
            SpannableString(inflated.emptyStateBaselineTextView.text).apply {
                setSpan(
                    ForegroundColorSpan(color(R.color.corail)),
                    27,
                    37,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                setSpan(
                    ForegroundColorSpan(color(R.color.blue_main)),
                    41,
                    51,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                inflated.emptyStateBaselineTextView.setText(this, TextView.BufferType.SPANNABLE)
            }
        }
        stubEmptyState.inflate()
    }

    private fun displaySelectedDepartment(department: Department?) {
        arrayOf(emptyStateSelectedDepartment, selectedDepartment).filterNotNull().forEach {
            it.text = if (department != null) {
                "${department.departmentCode} - ${department.departmentName}"
            } else {
                getString(R.string.choose_department_title)
            }
        }
    }

    override fun openLink(url: String) {
        launchWebUrl(url)
    }

    override fun showCentersError() {
        Snackbar.make(container, getString(R.string.centers_error), Snackbar.LENGTH_SHORT).show()
    }
}