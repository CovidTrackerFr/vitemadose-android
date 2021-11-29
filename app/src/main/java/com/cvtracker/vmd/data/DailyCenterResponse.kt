package com.cvtracker.vmd.data

import com.google.gson.annotations.SerializedName

class DailyCenterResponse(
    @SerializedName("creneaux_quotidiens")
    val dailySlots: MutableList<DailySlots>,
){
    fun aggregate(response: DailyCenterResponse) {
        /** Find, merge, remove from parameter response data **/
        dailySlots.forEach { itemDate ->
            val itemSubDate = response.dailySlots.find { it.date == itemDate.date }
            if(itemSubDate != null){
                /** We have found a date item, merge it **/
                itemDate.total = itemDate.total + itemSubDate.total
                itemDate.centers.addAll(itemSubDate.centers)
                response.dailySlots.remove(itemSubDate)
            }
        }
        /** Remaining data is added to the list **/
        dailySlots.addAll(response.dailySlots)
    }
}

class DailySlots(
    @SerializedName("date")
    val date: String,
    @SerializedName("total")
    var total: Int,
    @SerializedName("creneaux_par_lieu")
    val centers: MutableList<CenterSlots>)

class CenterSlots(
    @SerializedName("lieu")
    val centerId: String,
    @SerializedName("creneaux_par_tag")
    val tagSlots: List<TagSlots>)

class TagSlots(
    @SerializedName("tag")
    val tag: String,
    @SerializedName("creneaux")
    val slotsCount: Int)
