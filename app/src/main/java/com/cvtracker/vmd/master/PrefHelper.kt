package com.cvtracker.vmd.master

import android.content.Context
import android.content.SharedPreferences
import com.cvtracker.vmd.data.SearchEntry
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import timber.log.Timber

object PrefHelper {

    private const val PREF_VITEMADOSE = "PREF_VITEMADOSE"

    private const val PREF_SEARCH_ENTRY = "PREF_SEARCH_ENTRY"

    private val sharedPrefs: SharedPreferences
        get() = ViteMaDoseApp.get().getSharedPreferences(PREF_VITEMADOSE, Context.MODE_PRIVATE)

    var favEntry: SearchEntry?
        get(){
            /** I have struggled finding a way to parse efficiently the sealed class SearchEntru **/
            return try {
                /** Try parsing with Departement class first **/
                val typeDepartment = object : TypeToken<SearchEntry.Department>() {}.type
                Gson().fromJson(sharedPrefs.getString(PREF_SEARCH_ENTRY, null), typeDepartment)
            }catch (e: Exception){
                return try {
                    /** Try parsing with City class then **/
                    val typeCity = object : TypeToken<SearchEntry.City>() {}.type
                    Gson().fromJson(sharedPrefs.getString(PREF_SEARCH_ENTRY, null), typeCity)
                }catch (e: Exception){
                    Timber.e(e)
                    null
                }
            }
        }
        set(value) {
            val json = try {
                Gson().toJson(value)
            }catch (e: Exception){
                null
            }
            json?.let { sharedPrefs.edit().putString(PREF_SEARCH_ENTRY, it).apply() }
        }
}