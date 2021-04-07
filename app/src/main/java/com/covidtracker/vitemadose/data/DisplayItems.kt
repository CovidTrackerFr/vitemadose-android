package com.covidtracker.vitemadose.data

import com.google.gson.annotations.SerializedName

sealed class DisplayItem {
    class Center(
        @SerializedName("departement")
        val departement: String,
        @SerializedName("nom")
        val name: String,
        @SerializedName("url")
        val url: String,
        @SerializedName("plateforme")
        val platform: String,
        @SerializedName("prochain_rdv")
        val nextSlot: String,
        var available: Boolean = false
    ) : DisplayItem()

    object UnavailableCenterHeader : DisplayItem()

    class AvailableCenterHeader(val count: Int) : DisplayItem()

}