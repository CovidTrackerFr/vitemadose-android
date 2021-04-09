package com.covidtracker.vitemadose.home

import android.content.Context
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.covidtracker.vitemadose.R
import com.covidtracker.vitemadose.data.DisplayItem
import com.covidtracker.vitemadose.extensions.hide
import com.covidtracker.vitemadose.extensions.show
import kotlinx.android.synthetic.main.item_available_center_header.view.*
import kotlinx.android.synthetic.main.item_center.view.*
import java.text.SimpleDateFormat
import java.util.*

class CenterAdapter(
    private val context: Context,
    private val items: List<DisplayItem>,
    private val onClicked: (DisplayItem.Center) -> Unit,
    private val onAddressClicked: (String) -> Unit,
    private val onPhoneClicked: (String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TYPE_CENTER = 0
        const val TYPE_CENTER_UNAVAILABLE = 1
        const val TYPE_AVAILABLE_HEADER = 3
        const val TYPE_UNAVAILABLE_HEADER = 4
    }

    private val dateParser: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.FRANCE)

    open inner class AvailableCenterViewHolder(
        context: Context,
        parent: ViewGroup,
    ) : RecyclerView.ViewHolder(
        LayoutInflater.from(context).inflate(R.layout.item_center, parent, false)
    ) {
        fun bind(center: DisplayItem.Center, position: Int) {
            with(itemView) {
                centerNameView.text = center.displayName
                if (center.available && center.url.isNotBlank()) {
                    dateView.text = try {
                        /** I have not found the exact parser for all date format returned by the API
                         * Then I take only the string until minutes. This is sub-optimal */
                        DateFormat.format(
                            "EEEE dd MMMM à kk'h'mm",
                            dateParser.parse(center.nextSlot.substring(0, 16))
                        ).toString().capitalize(Locale.FRANCE)
                    } catch (e: Exception) {
                        ""
                    }
                } else {
                    dateView.text = context.getString(R.string.check_center)
                }

                center.metadata?.address?.let { address ->
                    centerNameView.setOnClickListener { onAddressClicked(address) }
                } ?: run {
                    centerNameView.setOnClickListener(null)
                }

                center.metadata?.phoneNumber?.let { phoneNumber ->
                    phoneView.setOnClickListener { onPhoneClicked(phoneNumber) }
                    phoneView.show()
                    phoneView.text = phoneNumber
                    iconPhoneView.show()
                } ?: run {
                    phoneView.hide()
                    iconPhoneView.hide()
                }

                center.platformEnum?.let { partner ->
                    partnerView?.text = String.format(
                        context.getString(R.string.partner_placeholder),
                        partner.label
                    )
                    partnerView.isVisible = true
                    partnerImageView.setImageResource(partner.logo)
                    partnerImageView.isVisible = true
                } ?: run {
                    partnerView.isVisible = false
                    partnerImageView.isVisible = true
                }

                bookButton.setOnClickListener { onClicked.invoke(center) }
            }
        }
    }

    inner class UnavailableCenterViewHolder(
        context: Context,
        parent: ViewGroup
    ) : RecyclerView.ViewHolder(
        LayoutInflater.from(context).inflate(R.layout.item_center_unavailable, parent, false)
    ) {
        fun bind(center: DisplayItem.Center) {
            with(itemView) {
                centerNameView.text = center.displayName
                dateView.text = context.getString(R.string.no_slots_available)
                bookButton.setOnClickListener { onClicked.invoke(center) }

                center.metadata?.address?.let { address ->
                    centerNameView.setOnClickListener { onAddressClicked(address) }
                } ?: run {
                    centerNameView.setOnClickListener(null)
                }
            }
        }
    }

    inner class UnavailableCenterHeaderViewHolder(context: Context, parent: ViewGroup) :
        RecyclerView.ViewHolder(
            LayoutInflater.from(context)
                .inflate(R.layout.item_unavailable_center_header, parent, false)
        ) {
        fun bind(header: DisplayItem.UnavailableCenterHeader) {
            with(itemView) {
                sectionLibelleView.setText(header.titleRes)
            }
        }
    }

    inner class AvailableCenterHeaderViewHolder(context: Context, parent: ViewGroup) :
        RecyclerView.ViewHolder(
            LayoutInflater.from(context)
                .inflate(R.layout.item_available_center_header, parent, false)
        ) {
        fun bind(header: DisplayItem.AvailableCenterHeader) {
            itemView.sectionLibelleView.text =
                String.format(
                    context.resources.getQuantityString(
                        R.plurals.disponibilities,
                        header.count, header.count
                    )
                )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_CENTER -> AvailableCenterViewHolder(context, parent)
            TYPE_CENTER_UNAVAILABLE -> UnavailableCenterViewHolder(context, parent)
            TYPE_UNAVAILABLE_HEADER -> UnavailableCenterHeaderViewHolder(context, parent)
            TYPE_AVAILABLE_HEADER -> AvailableCenterHeaderViewHolder(context, parent)
            else -> throw IllegalArgumentException("Type not supported")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (val result = items[position]) {
            is DisplayItem.Center -> {
                if (result.available) {
                    TYPE_CENTER
                } else {
                    TYPE_CENTER_UNAVAILABLE
                }
            }
            is DisplayItem.UnavailableCenterHeader -> TYPE_UNAVAILABLE_HEADER
            is DisplayItem.AvailableCenterHeader -> TYPE_AVAILABLE_HEADER
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is AvailableCenterViewHolder -> {
                holder.bind(items[position] as DisplayItem.Center, position)
            }
            is UnavailableCenterViewHolder -> {
                holder.bind(items[position] as DisplayItem.Center)
            }
            is UnavailableCenterHeaderViewHolder -> {
                holder.bind(items[position] as DisplayItem.UnavailableCenterHeader)
            }
            is AvailableCenterHeaderViewHolder -> {
                holder.bind(items[position] as DisplayItem.AvailableCenterHeader)
            }
        }
    }

    override fun getItemCount() = items.size

}