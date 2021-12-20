package com.cvtracker.vmd.custom.view_holder

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.view.View
import android.view.ViewGroup
import com.cvtracker.vmd.R
import com.cvtracker.vmd.custom.CenterAdapter
import com.cvtracker.vmd.data.Bookmark
import com.cvtracker.vmd.data.DisplayItem
import com.cvtracker.vmd.extensions.*
import com.cvtracker.vmd.master.PrefHelper
import com.cvtracker.vmd.util.isTalkbackEnabled
import kotlinx.android.synthetic.main.item_center.view.*

class CenterViewHolder(
    context: Context,
    parent: ViewGroup,
    adapter: CenterAdapter,
    private val listener: Listener?
) : AbstractViewHolder<DisplayItem.Center>(context, parent, adapter, R.layout.item_center) {

    interface Listener {
        fun onClicked (center: DisplayItem.Center)
        fun onBookmarkClicked (adapter: CenterAdapter, center: DisplayItem.Center, position: Int)
        fun onAddressClicked (address: String)
        fun onPhoneClicked (phoneNumber: String)
    }

    override fun bind(center: DisplayItem.Center, position: Int) {
        with(itemView) {
            centerNameView.text = center.name

            @SuppressLint("SetTextI18n")
            dateView.text = when {
                center.available && center.url.isNotBlank() && center.nextSlot != null -> if (PrefHelper.isNewSystem) {
                    context.resources.getQuantityString(R.plurals.shot_disponibilities_found, center.appointmentCount, center.appointmentCount)
                } else {
                    center.formattedNextSlot
                }
                center.available && center.isValidAppointmentByPhoneOnly -> context.getString(R.string.appointment_by_phone_only)
                else -> context.getString(R.string.no_slots_available)
            } + center.formattedDistance

            center.metadata?.address?.let { address ->
                centerAddressView.text = center.formattedAddress
                centerAddressView.show()
                centerAddressView.setOnClickListener { listener?.onAddressClicked(address) }
            } ?: run {
                centerAddressView.hide()
                centerAddressView.setOnClickListener(null)
            }

            center.vaccineType?.takeIf { it.isNotEmpty() }?.let { vaccine ->
                centerVaccineView.text = vaccine.joinToString(separator = " | ")
                iconVaccineView.show()
                centerVaccineView.show()
            } ?: run {
                iconVaccineView.hide()
                centerVaccineView.hide()
            }

            center.platformEnum?.let { partner ->
                partnerImageView.setImageResource(partner.logo)
                partnerImageView.contentDescription = partner.label
                bottomSeparatorView.show()
                partnerImageView.show()
            } ?: run {
                bottomSeparatorView.hide()
                partnerImageView.hide()
            }

            setupExpandedState(this, center, position)

            bookButton.setOnClickListener { listener?.onClicked(center) }
            bookButton.backgroundTintList = ColorStateList.valueOf(if (center.isChronodose) {
                colorAttr(R.attr.colorPrimary)
            } else {
                color(R.color.danube)
            })

            checkButton.setOnClickListener { listener?.onClicked(center) }
            callButton.setOnClickListener { center.metadata?.phoneFormatted?.let { listener?.onPhoneClicked(it) } }
            bookmarkView.setOnClickListener { listener?.onBookmarkClicked(adapter, center, position) }

            val slotsToShow = if (center.isChronodose) center.chronodoseCount else center.appointmentCount
            appointmentsCountView.text = if(slotsToShow > 0) {
                String.format(
                    context.resources.getQuantityString(
                        R.plurals.shot_disponibilities,
                        slotsToShow, slotsToShow
                    )
                )
            } else ""

            bookmarkView.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0,
                when (center.bookmark) {
                    Bookmark.NOTIFICATION_CHRONODOSE -> R.drawable.ic_lightning_charge_fill_24dp
                    Bookmark.NOTIFICATION -> R.drawable.ic_notifications_24dp
                    Bookmark.FAVORITE -> R.drawable.ic_bookmark_24dp
                    else -> R.drawable.ic_bookmark_border_24_dp
                }, 0)
            bookmarkView.setText(
                when {
                    center.available -> R.string.empty_string
                    center.bookmark == Bookmark.NOTIFICATION_CHRONODOSE -> R.string.notifications_chronodose_activated
                    center.bookmark == Bookmark.NOTIFICATION -> R.string.notifications_activated
                    else -> R.string.activate_notifs
                }
            )

            if (center.available && center.isValidAppointmentByPhoneOnly) {
                cardView.setCardBackgroundColor(colorAttr(R.attr.backgroundCardColor))
                bookButton.hide()
                appointmentsCountView.mask()
                callButton.text = context.getString(R.string.call_center, center.metadata?.phoneFormatted)
                callButton.show()
                checkSpecificViews.hide()
                bookmarkView.hide()
                bottomSeparatorView.hide()
            } else if (center.available) {
                cardView.setCardBackgroundColor(colorAttr(R.attr.backgroundCardColor))
                bookButton.show()
                appointmentsCountView.show()
                callButton.hide()
                checkSpecificViews.hide()
                bookmarkView.show()
            } else {
                cardView.setCardBackgroundColor(colorAttr(R.attr.backgroundCardColorSecondary))
                bookButton.hide()
                appointmentsCountView.mask()
                checkSpecificViews.show()
                callButton.hide()
                bookmarkView.show()
                bottomSeparatorView.hide()
            }

            if (center.isChronodose) {
                chronodoseView.show()
            } else {
                chronodoseView.hide()
            }
        }
    }

    private fun setupExpandedState(itemView: View, center: DisplayItem.Center, position: Int) {
        with(itemView) {
            updateAppointmentCardUI(position, center)

            moreView.setOnClickListener {
                adapter.expandedPosition = if (adapter.expandedPosition == position) {
                    -1
                } else {
                    position
                }

                if(context.isTalkbackEnabled()){
                    updateAppointmentCardUI(position, center)
                } else {
                    adapter.notifyItemChanged(position)
                }
            }
        }
    }

    private fun View.updateAppointmentCardUI(
        position: Int,
        center: DisplayItem.Center
    ) {
        if (adapter.expandedPosition == position) {
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
                phoneView.setOnClickListener { listener?.onPhoneClicked(phoneNumber) }
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
    }
}