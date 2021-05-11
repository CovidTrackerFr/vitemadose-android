package com.cvtracker.vmd.master

import android.content.Context
import android.content.SharedPreferences
import com.cvtracker.vmd.custom.ValidatorAdapterFactory
import com.cvtracker.vmd.data.Bookmark
import com.cvtracker.vmd.data.CenterBookmark
import com.cvtracker.vmd.data.DisplayItem
import com.cvtracker.vmd.data.SearchEntry
import com.google.gson.GsonBuilder
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken
import timber.log.Timber

object PrefHelper {

    private const val PREF_VITEMADOSE = "PREF_VITEMADOSE"

    private const val PREF_CHRONODOSE_ONBOARDING_DISPLAYED = "PREF_CHRONODOSE_ONBOARDING_DISPLAYED"
    private const val PREF_SEARCH_ENTRY = "PREF_SEARCH_ENTRY"
    private const val PREF_CENTERS_BOOKMARK = "PREF_CENTERS_BOOKMARK"

    private val sharedPrefs: SharedPreferences
        get() = ViteMaDoseApp.get().getSharedPreferences(PREF_VITEMADOSE, Context.MODE_PRIVATE)

    private val gson = GsonBuilder().registerTypeAdapterFactory(ValidatorAdapterFactory()).create()

    var chronodoseOnboardingDisplayed: Boolean
        get() = sharedPrefs.getBoolean(PREF_CHRONODOSE_ONBOARDING_DISPLAYED, false)
        set(value) {
            sharedPrefs.edit().putBoolean(PREF_CHRONODOSE_ONBOARDING_DISPLAYED, value).apply()
        }

    var favEntry: SearchEntry?
        get() {
            /** I have struggled finding a way to parse efficiently the sealed class SearchEntry **/
            return try {
                /** Try parsing with Department class first **/
                gson.fromJson(
                    sharedPrefs.getString(PREF_SEARCH_ENTRY, null),
                    SearchEntry.Department::class.java
                )
            } catch (e: JsonParseException) {
                Timber.e(e)
                return try {
                    /** Try parsing with City class then **/
                    gson.fromJson(
                        sharedPrefs.getString(PREF_SEARCH_ENTRY, null),
                        SearchEntry.City::class.java
                    )
                } catch (e: JsonParseException) {
                    null
                }
            }
        }
        set(value) {
            val json = try {
                gson.toJson(value)
            } catch (e: Exception) {
                null
            }
            json?.let { sharedPrefs.edit().putString(PREF_SEARCH_ENTRY, it).apply() }
        }


    var centersBookmark: Set<CenterBookmark>
        get() = try {
            /** Try parsing with CenterBookmark class then **/
            val myType = object : TypeToken<Set<CenterBookmark>>() {}.type
            gson.fromJson(sharedPrefs.getString(PREF_CENTERS_BOOKMARK, "[]"), myType)
        } catch (e: JsonParseException) {
            emptySet()
        }
        set(value) {
            val json = try {
                gson.toJson(value)
            } catch (e: Exception) {
                null
            }
            json?.let { sharedPrefs.edit().putString(PREF_CENTERS_BOOKMARK, it).apply() }
        }

    fun updateBookmark(center: DisplayItem.Center) {
        updateBookmark(center.id, center.department, center.bookmark)
    }

    fun updateBookmark(centerId: String?, department: String, bookmark: Bookmark) {
        centerId?.let { id ->
            val update = centersBookmark.toMutableSet()

            // remove center
            update.removeAll { it.centerId == id }

            // add bookmark if necessary
            if (bookmark != Bookmark.NONE) {
                update.add(CenterBookmark(id, department, bookmark))
            }

            // save
            centersBookmark = update
        }
    }
}