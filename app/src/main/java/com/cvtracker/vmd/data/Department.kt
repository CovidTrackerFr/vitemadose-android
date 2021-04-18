package com.cvtracker.vmd.data

import com.google.gson.annotations.SerializedName

class Department(
    @SerializedName("code_departement")
    val departmentCode: String,
    @SerializedName("nom_departement")
    val departmentName: String,
    @SerializedName("code_region")
    val regionCode: Int,
    @SerializedName("nom_region")
    val regionName: String
){
    override fun toString(): String {
        return "$departmentCode - $departmentName"
    }
}