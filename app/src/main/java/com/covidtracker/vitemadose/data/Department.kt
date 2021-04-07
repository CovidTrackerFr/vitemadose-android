package com.covidtracker.vitemadose.data

import com.google.gson.annotations.SerializedName

class Department(
    @SerializedName("code_departement")
    val codeDepartement: String,
    @SerializedName("nom_departement")
    val nomDepartement: String,
    @SerializedName("code_region")
    val codeRegion: Int,
    @SerializedName("nom_region")
    val nomRegion: String
)