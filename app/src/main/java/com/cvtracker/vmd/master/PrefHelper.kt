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
    private const val PREF_TAG_TYPE = "PREF_TAG_TYPE"
    @Deprecated("Not used")
    private const val PREF_PRIMARY_SORT = "PREF_PRIMARY_SORT"
    private const val PREF_FILTERS = "PREF_FILTERS"
    private const val PREF_DISCLAIMER_REPEAT = "PREF_DISCLAIMER_REPEAT"
    private const val PREF_DISCLAIMER_MESSAGE = "PREF_DISCLAIMER_MESSAGE"
    private const val PREF_NEW_SYSTEM = "PREF_NEW_SYSTEM"

    private val sharedPrefs: SharedPreferences
        get() = ViteMaDoseApp.get().getSharedPreferences(PREF_VITEMADOSE, Context.MODE_PRIVATE)

    private val gson = GsonBuilder().registerTypeAdapterFactory(ValidatorAdapterFactory()).create()

    var chronodoseOnboardingDisplayed: Boolean
        get() = sharedPrefs.getBoolean(PREF_CHRONODOSE_ONBOARDING_DISPLAYED, false)
        set(value) {
            sharedPrefs.edit().putBoolean(PREF_CHRONODOSE_ONBOARDING_DISPLAYED, value).apply()
        }

    var tagType: TagType
        get() = TagType.values()[sharedPrefs.getInt(PREF_TAG_TYPE, TagType.FIRST_SHOT.ordinal)]
        set(value) {
            sharedPrefs.edit().putInt(PREF_TAG_TYPE, value.ordinal).apply()
        }

    @Deprecated("Only SortType.ByProximity is used now. CHanging this won't have any impact")
    var primarySort: SortType
        get() = SortType.fromInt(sharedPrefs.getInt(PREF_PRIMARY_SORT, SortType.ByDate.value))
        set(value) {
            sharedPrefs.edit().putInt(PREF_PRIMARY_SORT, value.value).apply()
        }

    var filters: Set<FilterPref>
        get() = try {
            /** Try parsing with FilterPref class then **/
            val myType = object : TypeToken<Set<FilterPref>>() {}.type
            gson.fromJson(sharedPrefs.getString(PREF_FILTERS, "[]"), myType)
        } catch (e: JsonParseException) {
            emptySet()
        }
        set(value) {
            val json = try {
                gson.toJson(value)
            } catch (e: Exception) {
                null
            }
            json?.let { sharedPrefs.edit().putString(PREF_FILTERS, it).apply() }
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

    var lastDisclaimerClosedTimestamp: Long
        get() = sharedPrefs.getLong(PREF_DISCLAIMER_REPEAT, 0)
        set(value) {
            sharedPrefs.edit().putLong(PREF_DISCLAIMER_REPEAT, value).apply()
        }

    var lastDisclaimerClosedMessage: String?
        get() = sharedPrefs.getString(PREF_DISCLAIMER_MESSAGE, null)
        set(value) {
            sharedPrefs.edit().putString(PREF_DISCLAIMER_MESSAGE, value).apply()
        }

    var isNewSystem: Boolean
        get() = sharedPrefs.getBoolean(PREF_NEW_SYSTEM, true)
        set(value) {
            sharedPrefs.edit().putBoolean(PREF_NEW_SYSTEM, value).apply()
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
