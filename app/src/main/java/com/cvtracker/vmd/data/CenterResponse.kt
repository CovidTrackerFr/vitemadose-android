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

    lateinit var dailyAvailability: DailyCenterResponse

    fun aggregate(response: CenterResponse) {
        availableCenters.addAll(response.availableCenters)
        unavailableCenters.addAll(response.unavailableCenters)
    }

    /**
     * We apply the date and tag filters based on the dailyAvailability data we have
     */
    fun applyDailyFilters(dateKey: String, tagKey: String): CenterResponse {
        val globalList = mutableListOf<DisplayItem.Center>().apply {
            addAll(availableCenters.map { it.copy() })
            addAll(unavailableCenters.map { it.copy() })
        }

        /** Populate availableCenters **/
        val availableFilteredCenters = mutableListOf<DisplayItem.Center>()
        dailyAvailability.dailySlots
            .find { it.date == dateKey }?.centers
            ?.filter { it.tagSlots.find { it.tag == tagKey }?.slotsCount ?: 0 > 0 }
            ?.mapNotNull { centerData ->
                globalList.find { it.id == centerData.centerId }?.apply {
                    appointmentCount = centerData.tagSlots.find { it.tag == tagKey }?.slotsCount ?: 0
                }
            }
            ?.let{ availableFilteredCenters.addAll(it) }

        /** Populate unavailableCenters **/
        val availableCentersIds = availableFilteredCenters.map { it.id }
        val unavailableFilteredCenters = mutableListOf<DisplayItem.Center>()
        unavailableFilteredCenters.addAll(globalList.filterNot{ availableCentersIds.contains(it.id) }.onEach {
            it.appointmentCount = 0
        })
        return CenterResponse(
            version = version,
            lastUpdated = lastUpdated,
            availableCenters = availableFilteredCenters.map { it.copy() }.toMutableList(),
            unavailableCenters = unavailableFilteredCenters.map { it.copy() }.toMutableList())
    }
}