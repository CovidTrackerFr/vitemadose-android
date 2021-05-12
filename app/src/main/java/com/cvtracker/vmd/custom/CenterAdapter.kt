package com.cvtracker.vmd.custom

import android.content.Context
import android.content.res.ColorStateList
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cvtracker.vmd.R
import com.cvtracker.vmd.data.Bookmark
import com.cvtracker.vmd.data.DisplayItem
import com.cvtracker.vmd.data.ItemStat
import com.cvtracker.vmd.extensions.color
import com.cvtracker.vmd.extensions.colorAttr
import com.cvtracker.vmd.extensions.hide
import com.cvtracker.vmd.extensions.show
import com.cvtracker.vmd.master.PrefHelper
import kotlinx.android.synthetic.main.item_available_center_header.view.*
import kotlinx.android.synthetic.main.item_center.view.*
import kotlinx.android.synthetic.main.item_last_updated.view.*

class CenterAdapter(
    private val context: Context,
    private val items: List<DisplayItem>,
    private val onClicked: (DisplayItem.Center) -> Unit,
    private val onBookmarkClicked: (DisplayItem.Center, Int) -> Unit,
    private val onAddressClicked: (String) -> Unit,
    private val onPhoneClicked: (String) -> Unit,
    private val onChronodoseFilterClick: (() -> Unit)? = null,
    private val onSlotsFilterClick: (() -> Unit)? = null,
    private val onRemoveDisclaimerClick: (() -> Unit)? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mExpandedPosition = -1

    companion object {
        const val TYPE_CENTER = 0
        const val TYPE_CENTER_UNAVAILABLE = 1
        const val TYPE_AVAILABLE_HEADER = 3
        const val TYPE_LAST_UPDATED = 4
    }

    open inner class CenterViewHolder(
        context: Context,
        parent: ViewGroup,
    ) : RecyclerView.ViewHolder(
        LayoutInflater.from(context).inflate(R.layout.item_center, parent, false)
    ) {
        fun bind(center: DisplayItem.Center, position: Int) {
            with(itemView) {
                centerNameView.text = center.name

                dateView.text = when {
                    center.available && center.url.isNotBlank() && center.nextSlot != null -> center.formattedNextSlot
                    center.available && center.isValidAppointmentByPhoneOnly -> context.getString(R.string.appointment_by_phone_only)
                    else -> context.getString(R.string.no_slots_available)
                } + center.formattedDistance

                center.metadata?.address?.let { address ->
                    centerAddressView.text = center.formattedAddress
                    centerAddressView.show()
                    centerAddressView.setOnClickListener { onAddressClicked(address) }
                } ?: run {
                    centerAddressView.hide()
                    centerAddressView.setOnClickListener(null)
                }

                center.vaccineType?.let { vaccine ->
                    centerVaccineView.text = vaccine.joinToString(separator = " | ")
                    iconVaccineView.show()
                    centerVaccineView.show()
                } ?: run {
                    iconVaccineView.hide()
                    centerVaccineView.hide()
                }

                center.platformEnum?.let { partner ->
                    partnerImageView.setImageResource(partner.logo)
                    bottomSeparatorView.show()
                    partnerImageView.show()
                } ?: run {
                    bottomSeparatorView.hide()
                    partnerImageView.hide()
                }

                setupExpandedState(this, center, position)

                bookButton.setOnClickListener { onClicked.invoke(center) }
                bookButton.backgroundTintList = ColorStateList.valueOf(if(center.isChronodose){
                    colorAttr(R.attr.colorPrimary)
                }else{
                    color(R.color.danube)
                })

                checkButton.setOnClickListener { onClicked.invoke(center) }
                callButton.setOnClickListener { center.metadata?.phoneFormatted?.let { onPhoneClicked.invoke(it) } }
                bookmarkView.setOnClickListener { onBookmarkClicked.invoke(center, position) }

                val slotsToShow = if(center.isChronodose) center.chronodoseCount else center.appointmentCount
                appointmentsCountView.text =
                    String.format(
                        context.resources.getQuantityString(
                            R.plurals.shot_disponibilities,
                            slotsToShow, slotsToShow
                        )
                    )

                bookmarkView.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0,
                    when (center.bookmark) {
                        Bookmark.NOTIFICATION_CHRONODOSE -> R.drawable.ic_lightning_charge_fill_24dp
                        Bookmark.NOTIFICATION -> R.drawable.ic_notifications_24dp
                        Bookmark.FAVORITE -> R.drawable.ic_bookmark_24dp
                        else -> R.drawable.ic_bookmark_border_24_dp
                    }, 0)
                bookmarkView.setText(
                    when{
                        center.available -> R.string.empty_string
                        center.bookmark == Bookmark.NOTIFICATION_CHRONODOSE -> R.string.notifications_chronodose_activated
                        center.bookmark == Bookmark.NOTIFICATION -> R.string.notifications_activated
                        else -> R.string.activate_notifs
                    }
                )

                if (center.available && center.isValidAppointmentByPhoneOnly) {
                    cardView.setCardBackgroundColor(colorAttr(R.attr.backgroundCardColor))
                    centreAvailableSpecificViews.hide()
                    callButton.text = context.getString(R.string.call_center, center.metadata?.phoneFormatted)
                    callButton.show()
                    checkButton.hide()
                    bookmarkView.hide()
                } else if (center.available) {
                    cardView.setCardBackgroundColor(colorAttr(R.attr.backgroundCardColor))
                    centreAvailableSpecificViews.show()
                    callButton.hide()
                    checkButton.hide()
                    bookmarkView.show()
                } else {
                    cardView.setCardBackgroundColor(colorAttr(R.attr.backgroundCardColorSecondary))
                    centreAvailableSpecificViews.hide()
                    checkButton.show()
                    callButton.hide()
                    bookmarkView.show()
                }

                if (center.isChronodose) {
                    chronodoseView.show()
                } else {
                    chronodoseView.hide()
                }
            }
        }
    }

    private fun setupExpandedState(itemView: View, center: DisplayItem.Center, position: Int) {
        with(itemView) {
            if (mExpandedPosition == position) {
                moreView.rotation = 180f

                center.typeLabel?.let { type ->
                    centerTypeView.text = type
                    centerTypeView.show()
                    iconTypeView.show()
                } ?: run {
                    centerTypeView.hide()
                    iconTypeView.hide()
                }

                center.metadata?.phoneFormatted?.let { phoneNumber ->
                    phoneView.setOnClickListener { onPhoneClicked(phoneNumber) }
                    phoneView.show()
                    phoneView.text = phoneNumber
                    iconPhoneView.show()
                } ?: run {
                    phoneView.hide()
                    iconPhoneView.hide()
                }

                center.metadata?.businessHours?.description?.let { hours ->
                    businessHoursView.show()
                    businessHoursView.text = hours
                    iconBusinessHoursView.show()
                } ?: run {
                    businessHoursView.hide()
                    iconBusinessHoursView.hide()
                }
            } else {
                moreView.rotation = 0f
                centerTypeView.hide()
                iconTypeView.hide()
                businessHoursView.hide()
                iconBusinessHoursView.hide()
                phoneView.hide()
                iconPhoneView.hide()
            }

            if (center.hasMoreInfoToShow) {
                moreView.show()
            } else {
                moreView.hide()
            }

            moreView.setOnClickListener {
                mExpandedPosition = if (mExpandedPosition == position) {
                    -1
                } else {
                    val oldPosition = mExpandedPosition
                    if (oldPosition >= 0) {
                        notifyItemChanged(oldPosition)
                    }
                    position
                }
                notifyItemChanged(position)
            }
        }
    }

    inner class LastUpdatedViewHolder(context: Context, parent: ViewGroup) :
        RecyclerView.ViewHolder(
            LayoutInflater.from(context)
                .inflate(R.layout.item_last_updated, parent, false)
        ) {
        fun bind(item: DisplayItem.LastUpdated, position: Int) {
            with(itemView) {
                lastUpdated.text = context.getString(
                    R.string.last_updated,
                    DateUtils.getRelativeTimeSpanString(
                        item.date.time,
                        System.currentTimeMillis(),
                        0L
                    )
                )
                item.disclaimer?.let { disclaimer ->
                    disclaimerMessageView.text = disclaimer.message
                    disclaimerMessageView.setTextColor(disclaimer.severity.textColor(context))
                    disclaimerCardView.setCardBackgroundColor(disclaimer.severity.backgroundColor(context))
                    removeDisclaimerView.imageTintList = ColorStateList.valueOf(disclaimer.severity.textColor(context))
                    removeDisclaimerView.setOnClickListener {
                        item.disclaimer = null
                        notifyItemChanged(position)
                        onRemoveDisclaimerClick?.invoke()
                    }
                    disclaimerCardView.show()
                } ?: apply{
                    disclaimerCardView.hide()
                }
            }
        }
    }

    inner class AvailableCenterHeaderViewHolder(context: Context, parent: ViewGroup) :
        RecyclerView.ViewHolder(
            LayoutInflater.from(context)
                .inflate(R.layout.item_available_center_header, parent, false)
        ) {
        fun bind(header: DisplayItem.AvailableCenterHeader, position: Int) {
            itemView.firstStatView.apply {
                isSelected = header.isSlotFilterSelected
                bind(
                    ItemStat(
                        icon = R.drawable.ic_appointement,
                        plurals = R.plurals.slot_disponibilities,
                        countString = header.slotsCount.toString(),
                        count = header.slotsCount,
                        color = color(R.color.danube)
                    )
                )
                setOnClickListener {
                    header.isSlotFilterSelected = !header.isSlotFilterSelected
                    header.isChronodoseFilterSelected = false
                    notifyItemChanged(position)
                    onSlotsFilterClick?.invoke()
                }
            }
            itemView.secondStatView.apply {
                isSelected = header.isChronodoseFilterSelected
                bind(
                    ItemStat(
                        icon = R.drawable.ic_eclair,
                        plurals = R.plurals.chronodose_disponibilities,
                        countString = header.chronodoseCount.toString(),
                        count = header.chronodoseCount,
                        color = colorAttr(R.attr.colorPrimary)
                    )
                )
                setOnClickListener {
                    header.isChronodoseFilterSelected = !header.isChronodoseFilterSelected
                    header.isSlotFilterSelected = false
                    notifyItemChanged(position)
                    onChronodoseFilterClick?.invoke()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_CENTER -> CenterViewHolder(context, parent)
            TYPE_AVAILABLE_HEADER -> AvailableCenterHeaderViewHolder(context, parent)
            TYPE_LAST_UPDATED -> LastUpdatedViewHolder(context, parent)
            else -> throw IllegalArgumentException("Type not supported")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is DisplayItem.Center -> TYPE_CENTER
            is DisplayItem.AvailableCenterHeader -> TYPE_AVAILABLE_HEADER
            is DisplayItem.LastUpdated -> TYPE_LAST_UPDATED
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is CenterViewHolder -> {
                holder.bind(items[position] as DisplayItem.Center, position)
            }
            is AvailableCenterHeaderViewHolder -> {
                holder.bind(items[position] as DisplayItem.AvailableCenterHeader, position)
            }
            is LastUpdatedViewHolder -> {
                holder.bind(items[position] as DisplayItem.LastUpdated, position)
            }
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