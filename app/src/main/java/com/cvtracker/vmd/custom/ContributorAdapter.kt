package com.cvtracker.vmd.custom

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cvtracker.vmd.R
import com.cvtracker.vmd.custom.view_holder.ContributorViewHolder
import com.cvtracker.vmd.data.Contributor

class ContributorAdapter(
    private val data: List<Contributor>,
    private val onClicked: (item: Contributor) -> Unit
) : RecyclerView.Adapter<ContributorViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContributorViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_contributor, parent, false)
        return ContributorViewHolder(view, onClicked)
    }

    override fun onBindViewHolder(holder: ContributorViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size
}