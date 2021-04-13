package com.cvtracker.vmd.data

import com.google.gson.annotations.SerializedName
import java.util.*

class CenterResponse(
    @SerializedName("version")
    val version: String,
    @SerializedName("last_updated")
    val lastUpdated: Date,
    @SerializedName("centres_disponibles")
    val availableCenters: List<DisplayItem.Center>,
    @SerializedName("centres_indisponibles")
    val unavailableCenters: List<DisplayItem.Center>,
)