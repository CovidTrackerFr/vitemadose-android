package com.cvtracker.vmd.custom

import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cvtracker.vmd.custom.view_holder.CenterViewHolder
import com.cvtracker.vmd.custom.view_holder.LastUpdatedViewHolder
import com.cvtracker.vmd.custom.view_holder.StatisticsHeaderViewHolder
import com.cvtracker.vmd.data.Bookmark
import com.cvtracker.vmd.data.DisplayItem
import com.cvtracker.vmd.master.PrefHelper

class CenterAdapter(
        private val context: Context,
        private val items: List<DisplayItem>,
        private val centerListener: CenterViewHolder.Listener? = null,
        private val lastUpdatedListener: LastUpdatedViewHolder.Listener? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var expandedPosition = -1

    companion object {
        private const val TYPE_CENTER = 0
        private const val TYPE_AVAILABLE_HEADER = 1
        private const val TYPE_LAST_UPDATED = 2
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is DisplayItem.Center -> TYPE_CENTER
            is DisplayItem.AvailableCenterHeader -> TYPE_AVAILABLE_HEADER
            is DisplayItem.LastUpdated -> TYPE_LAST_UPDATED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_CENTER -> CenterViewHolder(context, parent, this, centerListener)
            TYPE_AVAILABLE_HEADER -> StatisticsHeaderViewHolder(context, parent, this)
            TYPE_LAST_UPDATED -> LastUpdatedViewHolder(context, parent, this, lastUpdatedListener)
            else -> throw IllegalArgumentException("Type not supported")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is CenterViewHolder -> holder.bind(items[position] as DisplayItem.Center, position)
            is StatisticsHeaderViewHolder -> holder.bind(items[position] as DisplayItem.AvailableCenterHeader, position)
            is LastUpdatedViewHolder -> holder.bind(items[position] as DisplayItem.LastUpdated, position)
        }
    }

    override fun getItemCount() = items.size

    fun refreshBookmarkState() {
        val centersBookmark = PrefHelper.centersBookmark
        items.filterIsInstance<DisplayItem.Center>().onEach { center ->
            center.bookmark = centersBookmark
                    .firstOrNull { center.id == it.centerId }?.bookmark
                    ?: Bookmark.NONE
        }
        notifyDataSetChanged()
    }

}