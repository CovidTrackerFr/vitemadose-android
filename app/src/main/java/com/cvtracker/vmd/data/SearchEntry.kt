package com.cvtracker.vmd.data

import com.google.gson.annotations.SerializedName

sealed class SearchEntry {

    val entryCode: String
        get() = when (this) {
            is Department -> this.departmentCode
            is City -> this.code
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
                code.startsWith("202") -> "2A"
                code.startsWith("20") -> "2B"
                else -> code.substring(0, 2)
            }
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
    ) : SearchEntry() {
        override fun toString(): String {
            return "$departmentCode - $departmentName"
        }
    }

    class City(
        @SerializedName("code")
        val code: String,
        @SerializedName("nom")
        val name: String,
        @SerializedName("centre")
        val center: Center
    ) : SearchEntry() {

        class Center(
            @SerializedName("coordinates")
            val coordinates: List<Double>,
        )

        val latitude: Double
            get() = center.coordinates[0]

        val longitude: Double
            get() = center.coordinates[1]

        override fun toString(): String {
            return "$code - $name"
        }
    }
}