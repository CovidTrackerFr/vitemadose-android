package com.cvtracker.vmd.data

import com.cvtracker.vmd.master.AnalyticsHelper
import com.google.gson.annotations.SerializedName

sealed class SearchEntry {

    val entryCode: String
        get() = when (this) {
            is Department -> this.departmentCode
            is City -> this.postalCode
        }

    val entryName: String
        get() = when (this) {
            is Department -> this.departmentName
            is City -> this.name
        }

    val entryDepartmentCode: String
        get() = when (this) {
            is Department -> this.departmentCode
            is City -> when {
                postalCode.startsWith("202") -> "2A"
                postalCode.startsWith("20") -> "2B"
                else -> postalCode.substring(0, 2)
            }
        }

    val defaultFilterType : AnalyticsHelper.FilterType
        get() = when(this){
            is Department -> AnalyticsHelper.FilterType.ByDate
            is City -> AnalyticsHelper.FilterType.ByProximity
        }

    override fun toString(): String {
        return "$entryCode - $entryName"
    }

    class Department(
        @SerializedName("code_departement")
        val departmentCode: String,
        @SerializedName("nom_departement")
        val departmentName: String,
        @SerializedName("code_region")
        val regionCode: Int,
        @SerializedName("nom_region")
        val regionName: String
    ) : SearchEntry()

    class City(
        @SerializedName("code")
        val code: String,
        @SerializedName("nom")
        val name: String,
        @SerializedName("centre")
        val center: Center,
        @SerializedName("codesPostaux")
        val postalCodeList: List<String>
    ) : SearchEntry() {

        /** In case the user has searched 75017, we want to display 75017 to him **/
        var searchedPostalCode: String? = null

        val postalCode: String
            get() = searchedPostalCode ?: postalCodeList.first()

        class Center(
            @SerializedName("coordinates")
            val coordinates: List<Double>,
        )

        val latitude: Double
            get() = center.coordinates[1]

        val longitude: Double
            get() = center.coordinates[0]
    }
}