package com.covidtracker.vitemadose.master

import android.content.Context
import android.content.SharedPreferences

object PrefHelper {

    private const val PREF_VITEMADOSE = "PREF_VITEMADOSE"

    private const val PREF_DEPARTMENT_CODE = "PREF_DEPARTMENT_CODE"

    private val sharedPrefs: SharedPreferences
        get() = ViteMaDoseApp.get().getSharedPreferences(PREF_VITEMADOSE, Context.MODE_PRIVATE)

    var favDepartmentCode: String?
        get() = sharedPrefs.getString(PREF_DEPARTMENT_CODE, null)
        set(value) = sharedPrefs.edit().putString(PREF_DEPARTMENT_CODE, value).apply()
}