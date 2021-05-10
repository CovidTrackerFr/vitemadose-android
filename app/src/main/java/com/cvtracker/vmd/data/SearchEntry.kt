package com.cvtracker.vmd.data

import com.cvtracker.vmd.master.SortType
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
            is City -> this.department?.departmentCode ?: ""
        }

    val defaultSortType : SortType
        get() = when(this){
            is Department -> SortType.ByDate
            is City -> SortType.ByDate
        }

    override fun toString(): String {
        return "$entryCode - $entryName"
    }

    class Department(
        @SerializedName("code")
        val departmentCode: String,
        @SerializedName("nom")
        val departmentName: String
    ) : SearchEntry()

    class City(
        @SerializedName("code")
        val code: String,
        @SerializedName("nom")
        val name: String,
        @SerializedName("centre")
        val center: Center,
        @SerializedName("codesPostaux")
        val postalCodeList: List<String>,
        @SerializedName("departement")
        val department: Department?,
    ) : SearchEntry() {

        /** In case the user has searched 75017, we want to display 75017 to him **/
        var searchedPostalCode: String? = null

        val postalCode: String
            get() = searchedPostalCode ?: postalCodeList.firstOrNull() ?: ""

        class Center(
            @SerializedName("coordinates")
            val coordinates: List<Double>,
        )

        val latitude: Double
            get() = center.coordinates[1]

        val longitude: Double
            get() = center.coordinates[0]

        val isValid: Boolean
            get() = department != null
    }
}