package com.covidtracker.vitemadose.data

import androidx.annotation.StringRes
import com.google.gson.annotations.SerializedName
import java.util.*

sealed class DisplayItem {

    class LastUpdated(val date: Date): DisplayItem()

    class Center(
        @SerializedName("departement")
        val department: String,
        @SerializedName("nom")
        val name: String,
        @SerializedName("url")
        val url: String,
        @SerializedName("plateforme")
        val platform: String,
        @SerializedName("metadata")
        val metadata: Metadata?,
        @SerializedName("prochain_rdv")
        val nextSlot: String,
        @SerializedName("appointment_count")
        val appointmentCount: Int,
        var available: Boolean = false
    ) : DisplayItem() {

        val platformEnum: Plateform?
            get() = Plateform.fromId(platform)

        val displayName: String
            get() {
                val additionalInfo = metadata?.address?.replace(", ","\n")?.trim()
                return name + (if (additionalInfo != null) "\n" + additionalInfo else "")
            }

        class Metadata(
            @SerializedName("address")
            val address: String?,
            @SerializedName("business_hours")
            val businessHours: BusinessHours?,
            @SerializedName("phone_number")
            val phoneNumber: String?
        ) {

            val hasMoreInfoToShow: Boolean
                get() = businessHours?.description != null || phoneNumber != null

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

    class AvailableCenterHeader(val count: Int) : DisplayItem()

}