package com.cvtracker.vmd.custom

import androidx.core.view.doOnPreDraw
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator
import java.lang.ref.WeakReference
import kotlin.math.min

/*
* Copy / Paste of regular mediator which fixes the issue (with smoothScroll to false):
* - ViewPager scroll to a position, animation not finished
* - OnTabSelected call -> setCurrentPosition on ViewPager with smoothScroll to false
* - Animation broken
* Now OnPageSelected callback :
* - tabSelectedListener removed
* - selectTab
* - tabSelectedListener added
*/
class CustomTabMediator(
    private val tabLayout: TabLayout,
    private val viewPager: ViewPager2,
    private val autoRefresh: Boolean,
    private val tabConfigurationStrategy: TabLayoutMediator.TabConfigurationStrategy
) {
    private var adapter: RecyclerView.Adapter<*>? = null

    /**
     * Returns whether the [TabLayout] and the [ViewPager2] are linked together.
     */
    var isAttached = false
        private set
    private var onPageChangeCallback: TabLayoutOnPageChangeCallback? = null
    private var onTabSelectedListener: OnTabSelectedListener? = null
    private var pagerAdapterObserver: AdapterDataObserver? = null

    /**
     * A callback interface that must be implemented to set the text and styling of newly created
     * tabs.
     */
    interface TabConfigurationStrategy {
        /**
         * Called to configure the tab for the page at the specified position. Typically calls [ ][TabLayout.Tab.setText], but any form of styling can be applied.
         *
         * @param tab The Tab which should be configured to represent the title of the item at the given
         * position in the data set.
         * @param position The position of the item within the adapter's data set.
         */
        fun onConfigureTab(tab: TabLayout.Tab, position: Int)
    }

    /**
     * Link the TabLayout and the ViewPager2 together. Must be called after ViewPager2 has an adapter
     * set. To be called on a new instance of TabLayoutMediator or if the ViewPager2's adapter
     * changes.
     *
     * @throws IllegalStateException If the mediator is already attached, or the ViewPager2 has no
     * adapter.
     */
    fun attach() {
        check(!isAttached) { "TabLayoutMediator is already attached" }
        adapter = viewPager.adapter
        checkNotNull(adapter) { "TabLayoutMediator attached before ViewPager2 has an " + "adapter" }
        isAttached = true

        // Now we'll add a tab selected listener to set ViewPager's current item
        onTabSelectedListener = ViewPagerOnTabSelectedListener(viewPager)
        // Add our custom OnPageChangeCallback to the ViewPager
        onPageChangeCallback = TabLayoutOnPageChangeCallback(
            tabLayout,
            onTabSelectedListener!!
        )
        viewPager.registerOnPageChangeCallback(onPageChangeCallback!!)

        tabLayout.addOnTabSelectedListener(onTabSelectedListener)

        // Now we'll populate ourselves from the pager adapter, adding an observer if
        // autoRefresh is enabled
        if (autoRefresh) {
            // Register our observer on the new adapter
            pagerAdapterObserver = PagerAdapterObserver()
            adapter!!.registerAdapterDataObserver(pagerAdapterObserver!!)
        }
        populateTabsFromPagerAdapter()

        // Now update the scroll position to match the ViewPager's current item
        tabLayout.doOnPreDraw { tabLayout.setScrollPosition(viewPager.currentItem, 0f, true) }
    }

    /**
     * Unlink the TabLayout and the ViewPager. To be called on a stale TabLayoutMediator if a new one
     * is instantiated, to prevent holding on to a view that should be garbage collected. Also to be
     * called before [.attach] when a ViewPager2's adapter is changed.
     */
    fun detach() {
        if (autoRefresh && adapter != null) {
            adapter!!.unregisterAdapterDataObserver(pagerAdapterObserver!!)
            pagerAdapterObserver = null
        }
        tabLayout.removeOnTabSelectedListener(onTabSelectedListener)
        viewPager.unregisterOnPageChangeCallback(onPageChangeCallback!!)
        onTabSelectedListener = null
        onPageChangeCallback = null
        adapter = null
        isAttached = false
    }

    fun populateTabsFromPagerAdapter() {
        tabLayout.removeAllTabs()
        if (adapter != null) {
            val adapterCount = adapter!!.itemCount
            for (i in 0 until adapterCount) {
                val tab = tabLayout.newTab()
                tabConfigurationStrategy.onConfigureTab(tab, i)
                tabLayout.addTab(tab, false)
            }
            // Make sure we reflect the currently set ViewPager item
            if (adapterCount > 0) {
                val lastItem = tabLayout.tabCount - 1
                val currItem = min(viewPager.currentItem, lastItem)
                if (currItem != tabLayout.selectedTabPosition) {
                    tabLayout.selectTab(tabLayout.getTabAt(currItem))
                }
            }
        }
    }

    /**
     * A [ViewPager2.OnPageChangeCallback] class which contains the necessary calls back to the
     * provided [TabLayout] so that the tab position is kept in sync.
     *
     *
     * This class stores the provided TabLayout weakly, meaning that you can use [ ][ViewPager2.registerOnPageChangeCallback] without removing the
     * callback and not cause a leak.
     */
    private class TabLayoutOnPageChangeCallback(
        tabLayout: TabLayout,
        val onTabSelectedListener: OnTabSelectedListener
    ) :OnPageChangeCallback() {
        private val tabLayoutRef: WeakReference<TabLayout> = WeakReference(tabLayout)
        private var previousScrollState = 0
        private var scrollState = 0
        override fun onPageScrollStateChanged(state: Int) {
            previousScrollState = scrollState
            scrollState = state
        }

        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
            val tabLayout = tabLayoutRef.get()
            if (tabLayout != null) {
                // Only update the text selection if we're not settling, or we are settling after
                // being dragged
                val updateText =
                    scrollState != ViewPager2.SCROLL_STATE_SETTLING || previousScrollState == ViewPager2.SCROLL_STATE_DRAGGING
                // Update the indicator if we're not settling after being idle. This is caused
                // from a setCurrentItem() call and will be handled by an animation from
                // onPageSelected() instead.
                val updateIndicator =
                    !(scrollState == ViewPager2.SCROLL_STATE_SETTLING && previousScrollState == ViewPager2.SCROLL_STATE_IDLE)
                if(updateIndicator) {
                    tabLayout.setScrollPosition(position, positionOffset, updateText, updateIndicator)
                }
            }
        }

        override fun onPageSelected(position: Int) {
            val tabLayout = tabLayoutRef.get()
            if (tabLayout != null && tabLayout.selectedTabPosition != position && position < tabLayout.tabCount) {
                // Select the tab, only updating the indicator if we're not being dragged/settled
                // (since onPageScrolled will handle that).
                val updateIndicator = (scrollState == ViewPager2.SCROLL_STATE_IDLE
                        || (scrollState == ViewPager2.SCROLL_STATE_SETTLING
                        && previousScrollState == ViewPager2.SCROLL_STATE_IDLE))
                tabLayout.removeOnTabSelectedListener(onTabSelectedListener)
                tabLayout.selectTab(tabLayout.getTabAt(position), updateIndicator)
                tabLayout.addOnTabSelectedListener(onTabSelectedListener)
            }
        }

        fun reset() {
            scrollState = ViewPager2.SCROLL_STATE_IDLE
            previousScrollState = scrollState
        }

        init {
            reset()
        }
    }

    /**
     * A [TabLayout.OnTabSelectedListener] class which contains the necessary calls back to the
     * provided [ViewPager2] so that the tab position is kept in sync.
     */
    private class ViewPagerOnTabSelectedListener(
        private val viewPager: ViewPager2
    ) :
        OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab) {
            viewPager.setCurrentItem(tab.position, true)
        }

        override fun onTabUnselected(tab: TabLayout.Tab) {
            // No-op
        }

        override fun onTabReselected(tab: TabLayout.Tab) {
            // No-op
        }
    }

    private inner class PagerAdapterObserver : AdapterDataObserver() {
        override fun onChanged() {
            populateTabsFromPagerAdapter()
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
            populateTabsFromPagerAdapter()
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            populateTabsFromPagerAdapter()
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            populateTabsFromPagerAdapter()
        }

        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
            populateTabsFromPagerAdapter()
        }
    }
}