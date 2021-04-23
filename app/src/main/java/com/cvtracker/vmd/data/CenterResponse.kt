package com.cvtracker.vmd.data

import com.google.gson.annotations.SerializedName
import java.util.*

class CenterResponse(
    @SerializedName("version")
    val version: String,
    @SerializedName("last_updated")
    val lastUpdated: Date,
    @SerializedName("centres_disponibles")
    val availableCenters: MutableList<DisplayItem.Center>,
    @SerializedName("centres_indisponibles")
    val unavailableCenters: MutableList<DisplayItem.Center>,
) {
    fun aggregate(response: CenterResponse) {
        availableCenters.addAll(response.availableCenters)
        unavailableCenters.addAll(response.unavailableCenters)
    }
}