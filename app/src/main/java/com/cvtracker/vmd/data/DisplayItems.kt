package com.cvtracker.vmd.data

import android.location.Location
import android.telephony.PhoneNumberUtils
import androidx.annotation.StringRes
import com.google.gson.annotations.SerializedName
import java.util.*


sealed class DisplayItem {

    class LastUpdated(val date: Date) : DisplayItem()

    class Center(
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
        val appointmentCount: Int,
        @SerializedName("type")
        val type: String?,
        @SerializedName("vaccine_type")
        val vaccineType: List<String>?,
        var available: Boolean = false,
        var distance: Float = -1f
    ) : DisplayItem() {

        val platformEnum: Plateform?
            get() = platform?.let { Plateform.fromId(it) }

        val typeLabel: String?
            get() = when (type) {
                "vaccination-center" -> "Centre de vaccination"
                "drugstore" -> "Pharmacie"
                "general-practitioner" -> "Médecin généraliste"
                else -> null
            }

        val formattedAddress: String?
            get() = metadata?.address?.replace(", ","\n")?.trim()

        val formattedDistance: String
            get() {
                val distanceString = distance.toString()
                return when{
                    distance > 10f -> " · ${distanceString.substring(0, distanceString.lastIndexOf("."))} km"
                    distance > 0f -> " · $distanceString km"
                    else -> ""
                }
            }

        val hasMoreInfoToShow: Boolean
            get() = metadata?.businessHours?.description != null ||
                    metadata?.phoneNumber != null ||
                    typeLabel != null

        fun calculateDistance(city: SearchEntry.City){
            distance = if(location?.latitude != null && location.longitude != null){
                val result = FloatArray(2)
                Location.distanceBetween(city.latitude, city.longitude, location.latitude, location.longitude, result)
                /** result is in meters, convert it to x.x kms **/
                (result[0]/100).toLong().toFloat()/10f
            }else{
                -1f
            }
        }

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
                            .replace("+33 ", "0")
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

    class UnavailableCenterHeader(@StringRes val titleRes: Int) : DisplayItem()

    class AvailableCenterHeader(val placesCount: Int, val slotsCount: Int) : DisplayItem()

}