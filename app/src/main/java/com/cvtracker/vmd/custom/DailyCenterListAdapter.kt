package com.cvtracker.vmd.custom

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.cvtracker.vmd.R
import com.cvtracker.vmd.custom.view_holder.CenterViewHolder
import com.cvtracker.vmd.custom.view_holder.LastUpdatedViewHolder
import com.cvtracker.vmd.data.DisplayItem

class DailyCenterListAdapter(
    private val context: Context,
    private var items: List<List<DisplayItem>>,
    private val centerListener: CenterViewHolder.Listener? = null,
    private val lastUpdatedListener: LastUpdatedViewHolder.Listener? = null
) : RecyclerView.Adapter<DailyCenterListAdapter.DailyCenterListViewHolder>() {

    inner class DailyCenterListViewHolder(baseView: View): RecyclerView.ViewHolder(baseView) {
        val centersRecyclerView: RecyclerView = baseView.findViewById(R.id.centersRecyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyCenterListViewHolder {
        return DailyCenterListViewHolder(LayoutInflater.from(context).inflate(R.layout.item_list_center, parent, false)).apply {
            centersRecyclerView.layoutManager = StaggeredGridLayoutManager(context.resources.getInteger(R.integer.column), StaggeredGridLayoutManager.VERTICAL)
        }
    }

    override fun onBindViewHolder(holder: DailyCenterListViewHolder, position: Int) {
        holder.centersRecyclerView.tag = "list-$position"
        holder.centersRecyclerView.adapter = CenterAdapter(
            context = context,
            items = items[position],
            centerListener = centerListener,
            lastUpdatedListener = lastUpdatedListener
        )
    }

    override fun getItemCount() = items.size

    fun updateList(items: List<List<DisplayItem>>){
        this@DailyCenterListAdapter.items = items
        notifyDataSetChanged()

    }
}