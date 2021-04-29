package com.cvtracker.vmd.master

import android.content.Context
import android.content.SharedPreferences
import com.cvtracker.vmd.custom.ValidatorAdapterFactory
import com.cvtracker.vmd.data.DisplayItem
import com.cvtracker.vmd.data.SearchEntry
import com.google.gson.GsonBuilder
import com.google.gson.JsonParseException
import timber.log.Timber

object PrefHelper {

    private const val PREF_VITEMADOSE = "PREF_VITEMADOSE"

    private const val PREF_SEARCH_ENTRY = "PREF_SEARCH_ENTRY"
    private const val PREF_SUBSCRIBED_CENTERS = "PREF_SUBSCRIBED_CENTERS"

    private val sharedPrefs: SharedPreferences
        get() = ViteMaDoseApp.get().getSharedPreferences(PREF_VITEMADOSE, Context.MODE_PRIVATE)

    private val gson = GsonBuilder().registerTypeAdapterFactory(ValidatorAdapterFactory()).create()

    var favEntry: SearchEntry?
        get(){
            /** I have struggled finding a way to parse efficiently the sealed class SearchEntry **/
            return try {
                /** Try parsing with Department class first **/
                gson.fromJson(sharedPrefs.getString(PREF_SEARCH_ENTRY, null), SearchEntry.Department::class.java)
            }catch (e: JsonParseException){
                Timber.e(e)
                return try {
                    /** Try parsing with City class then **/
                    gson.fromJson(sharedPrefs.getString(PREF_SEARCH_ENTRY, null), SearchEntry.City::class.java)
                }catch (e: JsonParseException){
                    null
                }
            }
        }
        set(value) {
            val json = try {
                gson.toJson(value)
            }catch (e: Exception){
                null
            }
            json?.let { sharedPrefs.edit().putString(PREF_SEARCH_ENTRY, it).apply() }
        }

    var subscribedCenters: Set<String>
        get() = sharedPrefs.getStringSet(PREF_SUBSCRIBED_CENTERS, emptySet()) ?: emptySet()
        set(value) {
            sharedPrefs.edit().putStringSet(PREF_SUBSCRIBED_CENTERS, value).apply()
        }

    fun addSubscribedCenter(center: DisplayItem.Center) {
        center.id?.let {
            val newSubscribedCenters = subscribedCenters.toMutableSet()
            newSubscribedCenters.add(it)
            subscribedCenters = newSubscribedCenters
        }
    }

    fun removeSubscribedCenter(center: DisplayItem.Center) {
        center.id?.let {
            val newSubscribedCenters = subscribedCenters.toMutableSet()
            newSubscribedCenters.remove(it)
            subscribedCenters = newSubscribedCenters
        }
    }
}