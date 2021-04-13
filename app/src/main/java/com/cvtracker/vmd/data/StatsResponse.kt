package com.cvtracker.vmd.data

import androidx.annotation.DrawableRes
import androidx.annotation.PluralsRes
import com.cvtracker.vmd.R
import com.google.gson.annotations.SerializedName

class StatsResponse(
    @SerializedName("tout_departement")
    val allDepartments: AllDepartments
) {

    class AllDepartments(
        @SerializedName("disponibles")
        val availableCount: Int,
        @SerializedName("total")
        val totalCount: Int,
        @SerializedName("creneaux")
        val slotCount: Int
    )
}

class DisplayStat(
    val centersCount: ItemStat,
    val availableCentersCount: ItemStat,
    val slotsCount: ItemStat,
    val fillRatio: ItemStat
) {
    companion object {
        fun from(statsResponse: StatsResponse): DisplayStat {
            return DisplayStat(
                centersCount = ItemStat(
                    icon = R.drawable.ic_search,
                    plurals = R.plurals.centers,
                    countString = statsResponse.allDepartments.totalCount.toString(),
                    count = statsResponse.allDepartments.totalCount
                ),
                availableCentersCount = ItemStat(
                    icon = R.drawable.ic_check,
                    plurals = R.plurals.center_disponibilities,
                    countString = statsResponse.allDepartments.availableCount.toString(),
                    count = statsResponse.allDepartments.availableCount
                ),
                slotsCount = ItemStat(
                    icon = R.drawable.ic_appointement,
                    plurals = R.plurals.slot_disponibilities,
                    countString = statsResponse.allDepartments.slotCount.toString(),
                    count = statsResponse.allDepartments.slotCount
                ),
                fillRatio = ItemStat(
                    icon = R.drawable.ic_percentage,
                    plurals = R.plurals.center_ratio_disponibilities,
                    countString = (statsResponse.allDepartments.availableCount * 100 / statsResponse.allDepartments.totalCount).toString() + "%",
                    count = (statsResponse.allDepartments.availableCount * 100 / statsResponse.allDepartments.totalCount)
                )
            )
        }
    }
}

class ItemStat(
    @PluralsRes val plurals: Int,
    @DrawableRes val icon: Int,
    val countString: String,
    val count: Int
)