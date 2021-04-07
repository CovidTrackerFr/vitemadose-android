package com.covidtracker.vitemadose.data

import com.google.gson.annotations.SerializedName

class CenterResponse(
    @SerializedName("last_updated")
    val codeDepartement: String,
    @SerializedName("centres_disponibles")
    val availableCenters: List<DisplayItem.Center>,
    @SerializedName("centres_indisponibles")
    val unavailableCenters: List<DisplayItem.Center>,
)