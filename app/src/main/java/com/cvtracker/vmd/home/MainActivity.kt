package com.cvtracker.vmd.home

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cvtracker.vmd.R
import com.cvtracker.vmd.about.AboutActivity
import com.cvtracker.vmd.data.DisplayItem
import com.cvtracker.vmd.data.SearchEntry
import com.cvtracker.vmd.extensions.*
import com.cvtracker.vmd.master.AnalyticsHelper
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.empty_state.*
import kotlinx.android.synthetic.main.empty_state.view.*
import java.net.URLEncoder

class MainActivity : AppCompatActivity(), MainContract.View {

    private val presenter: MainContract.Presenter = MainPresenter(this)

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

        override fun afterTextChanged(s: Editable?) {
            presenter.onSearchUpdated(s?.toString()?.trim().orEmpty())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.setBackgroundDrawable(ColorDrawable(colorAttr(R.attr.backgroundColor)))

        refreshLayout.setProgressViewOffset(false, resources.dpToPx(10f), resources.dpToPx(60f))
        refreshLayout.setOnRefreshListener {
            presenter.loadCenters()
        }

        aboutIconView.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }

        filterSwitchView.onFilterChangedListener = { filter ->
            presenter.onFilterChanged(filter)
        }

        selectedDepartment.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                selectedDepartment.gravity = Gravity.START
                /** Clear text when autocompletetextview become focused **/
                selectedDepartment.setText("", false)
            } else {
                selectedDepartment.gravity = Gravity.CENTER
            }
        }

        centersRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING && selectedDepartment.isFocused) {
                    /** selector was focused and a scroll is detected, reset the selector **/
                    resetSelectorState()
                }
            }
        })

        presenter.loadInitialState()
        presenter.loadCenters()

        appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            /** Manage colors when switching between collapsed and expanded state **/
            val progress = (-verticalOffset / headerLayout.measuredHeight.toFloat()) * 1.25f
            headerLayout.alpha = 1 - progress
            filterSwitchView.alpha = 1 - progress
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                loadColor(colorAttr(R.attr.iconTintColor), color(R.color.white), progress) {
                    aboutIconView.imageTintList = ColorStateList.valueOf(it)
                }
                loadColor(
                    colorAttr(R.attr.backgroundColor),
                    colorAttr(R.attr.colorPrimary),
                    progress
                ) {
                    backgroundSelectorView.setBackgroundColor(it)
                    appBarLayout.setBackgroundColor(it)
                }
                if (isDarkTheme()) {
                    loadColor(
                        colorAttr(android.R.attr.textColorPrimary),
                        color(R.color.mine_shaft),
                        progress
                    ) {
                        selectedDepartment.setTextColor(it)
                    }
                    loadColor(
                        colorAttr(R.attr.backgroundCardColor),
                        color(R.color.grey_5),
                        progress
                    ) {
                        departmentSelector.setCardBackgroundColor(it)
                    }
                }
            }
        })
    }

    private fun resetSelectorState() {
        selectedDepartment.clearFocus()
        selectedDepartment.hideKeyboard()
        displaySelectedSearchEntry(presenter.getSavedSearchEntry())
    }

    override fun showCenters(list: List<DisplayItem>, filter: AnalyticsHelper.FilterType?) {
        appBarLayout.setExpanded(true, true)
        centersRecyclerView.layoutManager = LinearLayoutManager(this)
        centersRecyclerView.adapter = CenterAdapter(
            context = this,
            items = list,
            onClicked = { presenter.onCenterClicked(it) },
            onAddressClicked = { startMapsActivity(it) },
            onPhoneClicked = { startPhoneActivity(it) }
        )
        /** set up filter state **/
        if (filter != null) {
            centersRecyclerView.topPadding = resources.dpToPx(50f)
            filterSwitchView.show()
            filterSwitchView.updateSelectedFilter(filter)
        } else {
            centersRecyclerView.topPadding = resources.dpToPx(12f)
            filterSwitchView.hide()
        }

        emptyStateContainer?.parent?.let { (it as ViewGroup).removeView(emptyStateContainer) }
    }

    private fun startPhoneActivity(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
        }
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Snackbar.make(
                container,
                getString(R.string.no_app_activity_found),
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    private fun startMapsActivity(address: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("geo:0,0?q=${URLEncoder.encode(address, "utf-8")}")
        }
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Snackbar.make(
                container,
                getString(R.string.no_app_activity_found),
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    override fun setLoading(loading: Boolean) {
        refreshLayout.isRefreshing = loading
    }

    override fun setupSelector(items: List<SearchEntry>) {
        arrayOf(
            emptyStateSelectedDepartment,
            selectedDepartment
        ).firstOrNull { it?.isAttachedToWindow == true }?.let {
            it.setAdapter(
                ArrayAdapter(
                    this,
                    R.layout.drop_down_resource,
                    items.toTypedArray()
                )
            )
            it.setOnItemClickListener { parent, view, position, id ->
                container.requestFocus()
                presenter.onSearchEntrySelected(parent.getItemAtPosition(position) as SearchEntry)
                resetSelectorState()
            }
            it.showDropDown()
        }
    }

    override fun showEmptyState() {
        stubEmptyState.setOnInflateListener { stub, inflated ->
            SpannableString(inflated.emptyStateBaselineTextView.text).apply {
                setSpan(
                    ForegroundColorSpan(colorAttr(R.attr.colorPrimary)),
                    27,
                    37,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                setSpan(
                    ForegroundColorSpan(color(R.color.danube)),
                    41,
                    51,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                inflated.emptyStateBaselineTextView.setText(this, TextView.BufferType.SPANNABLE)
            }
        }
        stubEmptyState.inflate()
    }

    override fun displaySelectedSearchEntry(entry: SearchEntry?) {
        arrayOf(selectedDepartment, emptyStateSelectedDepartment).filterNotNull().forEach {
            it.removeTextChangedListener(textWatcher)
            it.setText(entry?.toString() ?: "", false)
            it.addTextChangedListener(textWatcher)
        }
    }

    override fun openLink(url: String) {
        launchWebUrl(url)
    }

    override fun showCentersError() {
        Snackbar.make(container, getString(R.string.centers_error), Snackbar.LENGTH_SHORT).show()
    }

    override fun showSearchError() {
        Snackbar.make(container, getString(R.string.search_error), Snackbar.LENGTH_SHORT).show()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    fun loadColor(
        colorStart: Int,
        colorEnd: Int,
        progress: Float,
        onColorLoaded: (Int) -> Unit
    ) {
        ValueAnimator.ofObject(ArgbEvaluator(), colorStart, colorEnd).apply {
            setCurrentFraction(progress)
            onColorLoaded.invoke(animatedValue as Int)
        }
    }
}