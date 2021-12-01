package com.cvtracker.vmd.data

import android.location.Location
import android.telephony.PhoneNumberUtils
import android.text.format.DateFormat
import com.google.gson.annotations.SerializedName
import java.util.*

sealed class DisplayItem {

    class LastUpdated(val date: Date, var disclaimer: Disclaimer? = null) : DisplayItem()

    data class Center(
        @SerializedName("departement")
        val department: String,
        @SerializedName("nom")
        val name: String,
        @SerializedName("url")
        val url: String,
        @SerializedName("plateforme")
        val platform: String?,
        @SerializedName("metadata")
        val metadata: Metadata?,
        @SerializedName("location")
        val location: LocationCenter?,
        @SerializedName("prochain_rdv")
        val nextSlot: Date?,
        @SerializedName("appointment_count")
        var appointmentCount: Int,
        @SerializedName("appointment_by_phone_only")
        val appointmentByPhoneOnly: Boolean,
        @SerializedName("type")
        val type: String?,
        @SerializedName("internal_id")
        val id: String?,
        @SerializedName("vaccine_type")
        val vaccineType: List<String>?,
        @SerializedName("appointment_schedules")
        val schedules: List<Schedule>?
    ) : DisplayItem() {

        var distance: Float? = null

        var bookmark: Bookmark = Bookmark.NONE

        val isChronodose: Boolean
            get() = false

        val chronodoseCount: Int
            get() = (schedules?.find { it.name == "chronodose" }?.total ?: 0)

        val available: Boolean
            get() = isValidAppointmentByPhoneOnly || appointmentCount > 0

        val isValidAppointmentByPhoneOnly: Boolean
            get() = appointmentByPhoneOnly && !metadata?.phoneNumber.isNullOrBlank()

        val platformEnum: Plateform?
            get() = platform?.let { Plateform.fromId(it) }

        val typeLabel: String?
            get() = when (type) {
                "vaccination-center" -> "Centre de vaccination"
                "drugstore" -> "Pharmacie"
                "general-practitioner" -> "Médecin généraliste"
                else -> null
            }

        val formattedNextSlot: String?
            get() = try {
                DateFormat.format("EEEE d MMM à k'h'mm", nextSlot).toString().capitalize(Locale.FRANCE)
            } catch (e: Exception) {
                ""
            }

        val formattedAddress: String?
            get() = metadata?.address?.replace(", ", "\n")?.trim()

        val formattedDistance: String
            get() {
                val distanceString = distance.toString()
                return when{
                    distance == null -> ""
                    distance?.let { it > 10f } == true-> " · ${distanceString.substring(0, distanceString.lastIndexOf("."))}\u00A0km"
                    distance?.let { it > 0f } == true -> " · $distanceString\u00A0km"
                    else -> ""
                }
            }

        val hasMoreInfoToShow: Boolean
            get() = metadata?.businessHours?.description != null ||
                    metadata?.phoneNumber != null ||
                    typeLabel != null

        fun calculateDistance(city: SearchEntry.City){
            distance = if (location?.latitude != null && location.longitude != null && city.latitude != null && city.longitude != null) {
                val result = FloatArray(2)
                Location.distanceBetween(city.latitude!!, city.longitude!!, location.latitude, location.longitude, result)
                /** result is in meters, convert it to x.x kms **/
                (result[0] / 100).toLong().toFloat() / 10f
            } else {
                null
            }
        }

        class Schedule(
            @SerializedName("name")
            val name: String,
            @SerializedName("total")
            val total: Int,
        )

        class LocationCenter(
            @SerializedName("latitude")
            val latitude: Double?,
            @SerializedName("longitude")
            val longitude: Double?
        )

        class Metadata(
            @SerializedName("address")
            val address: String?,
            @SerializedName("business_hours")
            val businessHours: BusinessHours?,
            @SerializedName("phone_number")
            val phoneNumber: String?
        ) {

            val phoneFormatted: String?
                get() {
                    return phoneNumber?.let {
                        PhoneNumberUtils.formatNumber(it, Locale.getDefault().country)
                            ?.replace("+33 ", "0") ?: phoneNumber
                    }
                }

            class BusinessHours(
                @SerializedName("lundi")
                val lundi: String?,
                @SerializedName("mardi")
                val mardi: String?,
                @SerializedName("mercredi")
                val mercredi: String?,
                @SerializedName("jeudi")
                val jeudi: String?,
                @SerializedName("vendredi")
                val vendredi: String?,
                @SerializedName("samedi")
                val samedi: String?,
                @SerializedName("dimanche")
                val dimanche: String?,
            ) {

                val description: String?
                    get() {
                        return ((lundi?.takeIf { it.isNotBlank() }?.let { "Lundi : $it\n" } ?: "") +
                                (mardi?.takeIf { it.isNotBlank() }?.let { "Mardi : $it\n" } ?: "") +
                                (mercredi?.takeIf { it.isNotBlank() }?.let { "Mercredi : $it\n" } ?: "") +
                                (jeudi?.takeIf { it.isNotBlank() }?.let { "Jeudi : $it\n" } ?: "") +
                                (vendredi?.takeIf { it.isNotBlank() }?.let { "Vendredi : $it\n" } ?: "") +
                                (samedi?.takeIf { it.isNotBlank() }?.let { "Samedi : $it\n" } ?: "") +
                                (dimanche?.takeIf { it.isNotBlank() }?.let { "Dimanche : $it" } ?: ""))
                            .removeSuffix("\n")
                            .takeIf { it.isNotBlank() }
                    }
            }
        }
    }

    class AvailableCenterHeader(
        val slotsCount: Int,
        val placesCount: Int
    ) : DisplayItem()

}