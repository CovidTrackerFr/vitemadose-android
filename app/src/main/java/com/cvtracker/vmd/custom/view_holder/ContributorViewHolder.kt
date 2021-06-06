package com.cvtracker.vmd.custom.view_holder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.cvtracker.vmd.R
import com.cvtracker.vmd.custom.ContributorAdapter
import com.cvtracker.vmd.data.Contributor
import kotlinx.android.synthetic.main.item_contributor.view.*

class ContributorViewHolder(
    itemView: View,
    private val onClick: (item: Contributor) -> Unit
) : RecyclerView.ViewHolder(itemView) {
    fun bind(contributor: Contributor) {
        itemView.setOnClickListener { onClick(contributor) }
        with(contributor) {
            Glide.with(itemView)
                .load(avatarUrl)
                .placeholder(R.drawable.ic_splash_logo)
                .apply(RequestOptions.circleCropTransform())
                .into(itemView.contributor_avatar)
            itemView.contributor_name.text = displayName
            itemView.contributor_teams.text = contributor.teams.joinToString(",")
        }
    }
}