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
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.doOnPreDraw
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.viewpager2.widget.ViewPager2
import com.cvtracker.vmd.R
import com.cvtracker.vmd.about.AboutActivity
import com.cvtracker.vmd.base.AbstractCenterActivity
import com.cvtracker.vmd.bookmark.BookmarkActivity
import com.cvtracker.vmd.custom.CustomTabMediator
import com.cvtracker.vmd.custom.FiltersDialogView
import com.cvtracker.vmd.custom.view_holder.LastUpdatedViewHolder
import com.cvtracker.vmd.data.DisplayItem
import com.cvtracker.vmd.data.SearchEntry
import com.cvtracker.vmd.data.TabHeaderItem
import com.cvtracker.vmd.extensions.*
import com.cvtracker.vmd.master.FilterType
import com.cvtracker.vmd.master.TagType
import com.cvtracker.vmd.util.VMDAppUpdate
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.empty_state.*
import kotlinx.android.synthetic.main.empty_state.view.*


class MainActivity : AbstractCenterActivity<MainContract.Presenter>(), MainContract.View, LastUpdatedViewHolder.Listener {

    companion object {
        const val REQUEST_CODE_BOOKMARKS = 121
        const val REQUEST_CODE_ABOUT = 123
    }

    override val presenter: MainContract.Presenter = MainPresenter(this)

    override var lastUpdatedListener: LastUpdatedViewHolder.Listener? = this

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

    private var tabMediator: CustomTabMediator? = null

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

        refreshLayout.setOnRefreshListener {
            presenter.loadCenters()
        }

        bookmarkIconView.setOnClickListener {
            showBookmarks()
        }
        aboutIconView.setOnClickListener {
            startActivityForResult(Intent(this, AboutActivity::class.java), REQUEST_CODE_ABOUT)
        }

        sortSwitchView.onTagTypeChangedListener = { filter ->
            presenter.onTagTypeChanged(filter)
        }
        filterView.setOnClickListener {
            showFiltersDialog(presenter.getFilters().toMutableList())
        }

        centersPagerView.offscreenPageLimit = 2
        centersPagerView.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
                /** selector was focused and a touch is detected outside, reset the selector **/
                refreshLayout.isEnabled = (state == ViewPager2.SCROLL_STATE_IDLE)
            }
        })

        presenter.loadInitialState()
        presenter.loadCenters()

        appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            /** Manage colors when switching between collapsed and expanded state **/
            val progress = (-verticalOffset / headerLayout.measuredHeight.toFloat()) * 1.25f
            headerLayout.alpha = 1 - progress
            mainContent.translationY = (verticalOffset / headerLayout.measuredHeight.toFloat())* (sortSwitchView.measuredHeight + dpToPx(4f))
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

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if(selectedDepartment.isFocused && ev != null && !selectedDepartment.isViewInBounds(ev.x.toInt(), ev.y.toInt())){
            resetSelectorState()
        }
        return super.dispatchTouchEvent(ev)
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
        }
    }

    override fun displaySelectorDropdown() {
        arrayOf(
            emptyStateSelectedDepartment,
            selectedDepartment
        ).firstOrNull { it?.isAttachedToWindow == true }?.let {
            it.showDropDown()
        }
    }

    override fun removeEmptyStateIfNeeded() {
        bookmarkIconView.show()
        mainContent.show()
        /** Set the mainContent height based to the available height
         * This is useful as we setup a translationY on mainContent while appBar collapses **/
        val params = (mainContent.layoutParams as CoordinatorLayout.LayoutParams)
        if(params.height == CoordinatorLayout.LayoutParams.MATCH_PARENT) {
            container.doOnPreDraw {
                if (container.measuredHeight > 0) {
                    params.height = container.measuredHeight + dpToPx(4f)
                }
            }
        }
        emptyStateContainer?.parent?.let { (it as ViewGroup).removeView(emptyStateContainer) }
    }

    override fun showEmptyState() {
        bookmarkIconView.hide()
        mainContent.hide()
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
        if ((requestCode == REQUEST_CODE_BOOKMARKS || requestCode == REQUEST_CODE_ABOUT) && resultCode == RESULT_OK) {
            presenter.loadCenters()
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

    private fun switchFilter(filterId: String, excludedFilterId: String) {
        val filters = presenter.getFilters()
        filters.onEach {
            val filter = it.filters.find { it.id == filterId } ?: return@onEach
            filter.enabled = filter.enabled.not()
            Snackbar.make(container, "Filtre \"${filter.displayTitle}\" ${if(filter.enabled) "activé" else "désactivé"}", Snackbar.LENGTH_SHORT).show()

            /** We want to be sure we disable $excludedFilterId when we enable $filterId **/
            if(filter.enabled) {
                val filterToDisable = it.filters.find { it.id == excludedFilterId } ?: return@onEach
                filterToDisable.enabled = false
            }
        }
        presenter.updateFilters(filters)
    }

    override fun showTabs(listTabHeader: List<TabHeaderItem>?) {
        if (listTabHeader != null) {
            tabLayout.isVisible = true
            tabMediator?.detach()
            tabMediator = CustomTabMediator(tabLayout, centersPagerView, true) { tab, position ->
                tab.text = listTabHeader[position].header
                (tab.view.getChildAt(1) as? TextView)?.setTextColor(if (listTabHeader[position].count > 0) {
                    ContextCompat.getColorStateList(this, R.color.selector_tab_view)
                }else {
                    ColorStateList.valueOf(color(R.color.grey_10))
                })
            }
            tabMediator?.attach()
        } else {
            tabLayout.isVisible = false
        }
    }

    override fun showCenters(list: List<List<DisplayItem>>, tagType: TagType?) {
        super.showCenters(list, tagType)
        tabLayout.isInvisible = list.isEmpty()
    }

    override fun onRemoveDisclaimerClick() {
        presenter.removeDisclaimer()
    }
}