package com.cvtracker.vmd.master

import android.content.Context
import android.content.SharedPreferences
import com.cvtracker.vmd.custom.ValidatorAdapterFactory
import com.cvtracker.vmd.data.SearchEntry
import com.google.gson.GsonBuilder
import com.google.gson.JsonParseException

object PrefHelper {

    private const val PREF_VITEMADOSE = "PREF_VITEMADOSE"

    private const val PREF_SEARCH_ENTRY = "PREF_SEARCH_ENTRY"

    private val sharedPrefs: SharedPreferences
        get() = ViteMaDoseApp.get().getSharedPreferences(PREF_VITEMADOSE, Context.MODE_PRIVATE)

    private val gson = GsonBuilder().registerTypeAdapterFactory(ValidatorAdapterFactory()).create()

    var favEntry: SearchEntry?
        get(){
            /** I have struggled finding a way to parse efficiently the sealed class SearchEntru **/
            return try {
                /** Try parsing with Department class first **/
                gson.fromJson(sharedPrefs.getString(PREF_SEARCH_ENTRY, null), SearchEntry.Department::class.java)
            }catch (e: JsonParseException){
                e.printStackTrace()
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
}