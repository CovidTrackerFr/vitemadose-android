package com.cvtracker.vmd.home

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.cvtracker.vmd.R
import com.cvtracker.vmd.about.AboutActivity
import com.cvtracker.vmd.base.AbstractCenterActivity
import com.cvtracker.vmd.bookmark.BookmarkActivity
import com.cvtracker.vmd.custom.CenterAdapter
import com.cvtracker.vmd.custom.FiltersDialogView
import com.cvtracker.vmd.data.DisplayItem
import com.cvtracker.vmd.data.SearchEntry
import com.cvtracker.vmd.extensions.*
import com.cvtracker.vmd.master.FilterType
import com.cvtracker.vmd.master.SortType
import com.cvtracker.vmd.onboarding.ChronodoseOnboardingActivity
import com.cvtracker.vmd.util.VMDAppUpdate
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_bookmark.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.appBarLayout
import kotlinx.android.synthetic.main.activity_main.centersRecyclerView
import kotlinx.android.synthetic.main.activity_main.container
import kotlinx.android.synthetic.main.activity_main.refreshLayout
import kotlinx.android.synthetic.main.empty_state.*
import kotlinx.android.synthetic.main.empty_state.view.*


class MainActivity : AbstractCenterActivity<MainContract.Presenter>(), MainContract.View {

    companion object {
        const val REQUEST_CODE_BOOKMARKS = 121
    }

    override val presenter: MainContract.Presenter = MainPresenter(this)

    override val onChronodoseFilterClick: (() -> Unit) = {
        switchFilter(FilterType.FILTER_CHRONODOSE_ID)
    }

    override val onSlotsFilterClick: (() -> Unit) = {
        switchFilter(FilterType.FILTER_AVAILABLE_ID)
    }

    override val onRemoveDisclaimerClick: (() -> Unit) = {
        presenter.removeDisclaimer()
    }

    private val appUpdateChecker: VMDAppUpdate by lazy { VMDAppUpdate(this, container) }

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

        override fun afterTextChanged(s: Editable?) {
            (presenter as MainPresenter).onSearchUpdated(s?.toString()?.trim().orEmpty())
        }
    }

    private val onEditorActionListener = TextView.OnEditorActionListener { v, actionId, event ->
        var handled = false
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            (v as AutoCompleteTextView).showDropDown()
            v.hideKeyboard()
            handled = true
        }
        handled
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.setBackgroundDrawable(ColorDrawable(colorAttr(R.attr.backgroundColor)))

        if(savedInstanceState == null){
            intent.dataString?.let { presenter.handleDeepLink(it) }
        }

        appUpdateChecker.checkUpdates()
        initSelectors()

        refreshLayout.setProgressViewOffset(false, resources.dpToPx(10f), resources.dpToPx(60f))
        refreshLayout.setOnRefreshListener {
            presenter.loadCenters()
        }

        bookmarkIconView.setOnClickListener {
            showBookmarks()
        }
        aboutIconView.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }

        sortSwitchView.onSortChangedListener = { filter ->
            presenter.onSortChanged(filter)
        }
        filterView.setOnClickListener {
            showFiltersDialog(presenter.getFilters().toMutableList())
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

        centersRecyclerView.topPadding = resources.dpToPx(60f)
        presenter.loadInitialState()
        presenter.loadCenters()

        appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            /** Manage colors when switching between collapsed and expanded state **/
            val progress = (-verticalOffset / headerLayout.measuredHeight.toFloat()) * 1.25f
            headerLayout.alpha = 1 - progress
            sortSwitchView.alpha = 1 - progress
            filterView.alpha = 1 - progress
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                loadColor(colorAttr(R.attr.iconTintColor), color(R.color.white), progress) {
                    bookmarkIconView.imageTintList = ColorStateList.valueOf(it)
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

    private fun initSelectors() {
        selectedDepartment.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                selectedDepartment.gravity = Gravity.START
                /** Clear text when autocompletetextview become focused **/
                selectedDepartment.setText("", false)
            } else {
                selectedDepartment.gravity = Gravity.CENTER
            }
        }
        selectedDepartment.setOnEditorActionListener(onEditorActionListener)
    }

    private fun resetSelectorState() {
        selectedDepartment.clearFocus()
        selectedDepartment.hideKeyboard()
        displaySelectedSearchEntry(presenter.getSavedSearchEntry())
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

    override fun removeEmptyStateIfNeeded() {
        bookmarkIconView.show()
        emptyStateContainer?.parent?.let { (it as ViewGroup).removeView(emptyStateContainer) }
        presenter.displayChronodoseOnboardingIfNeeded()
    }

    override fun showEmptyState() {
        bookmarkIconView.hide()
        stubEmptyState.setOnInflateListener { stub, inflated ->
            emptyStateSelectedDepartment.setOnEditorActionListener(onEditorActionListener)
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

    override fun showChronodoseOnboarding() {
        startActivity(Intent(this, ChronodoseOnboardingActivity::class.java))
    }

    override fun displaySelectedSearchEntry(entry: SearchEntry?) {
        arrayOf(selectedDepartment, emptyStateSelectedDepartment).filterNotNull().forEach {
            it.removeTextChangedListener(textWatcher)
            it.setText(entry?.toString() ?: "", false)
            it.addTextChangedListener(textWatcher)
        }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_BOOKMARKS && resultCode == RESULT_OK) {
            (centersRecyclerView.adapter as? CenterAdapter)?.refreshBookmarkState()
        }
    }

    override fun showBookmarks(department: String?, centerId: String?) {
        startActivityForResult(
            Intent(this, BookmarkActivity::class.java).apply {
                putExtra(BookmarkActivity.EXTRA_DEPARTMENT, department)
                putExtra(BookmarkActivity.EXTRA_CENTER_ID, centerId)
            }, REQUEST_CODE_BOOKMARKS
        )
    }

    override fun onNewIntent(newIntent: Intent?) {
        super.onNewIntent(newIntent)
        intent = newIntent
        intent.dataString?.let { presenter.handleDeepLink(it) }
    }

    override fun showFiltersDialog(filterSections: MutableList<FilterType.FilterSection>) {
        val filtersDialogView = FiltersDialogView(this)
        filtersDialogView.populateFilters(filterSections)
        AlertDialog.Builder(this)
            .setTitle(R.string.filters_title)
            .setView(filtersDialogView)
            .setPositiveButton(R.string.apply){ _,_ ->
                presenter.updateFilters(filtersDialogView.newFilters)
            }
            .setNegativeButton(R.string.cancel){ _,_ -> }
            .show()
    }

    override fun updateFilterState(defaultFilters: Boolean) {
        filterView.isSelected = defaultFilters.not()
    }

    private fun switchFilter(filterId: String) {
        val filters = presenter.getFilters()
        filters.onEach {
            val filter = it.filters.find { it.id == filterId } ?: return@onEach
            filter.enabled = filter.enabled.not()
            Snackbar.make(container, "Filtre \"${filter.displayTitle}\" ${if(filter.enabled) "activé" else "désactivé"}", Snackbar.LENGTH_SHORT).show()
        }
        presenter.updateFilters(filters)
    }

    override fun showCenters(list: List<DisplayItem>, sortType: SortType?) {
        super.showCenters(list, sortType)
        showPlaceholderEmptyList(list.lastOrNull() is DisplayItem.AvailableCenterHeader)
    }

    private fun showPlaceholderEmptyList(show: Boolean){
        if(show) {
            noCentersView.show()
            if (filterView.isSelected) {
                resetFiltersView.show()
                resetFiltersView.setOnClickListener {
                    presenter.resetFilters()
                }
            } else {
                resetFiltersView.hide()
            }
        }else{
            noCentersView.hide()
            resetFiltersView.hide()
        }
    }
}