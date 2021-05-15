package com.cvtracker.vmd.custom.view_holder

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.cvtracker.vmd.custom.CenterAdapter

abstract class AbstractViewHolder<T>(
        context: Context,
        parent: ViewGroup,
        val adapter: CenterAdapter,
        @LayoutRes layout: Int) :
        RecyclerView.ViewHolder(LayoutInflater.from(context).inflate(layout, parent, false)) {

    abstract fun bind(data: T, position: Int)
}