package com.cvtracker.vmd.master

import android.content.Context
import android.content.SharedPreferences
import com.cvtracker.vmd.data.Department
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object PrefHelper {

    private const val PREF_VITEMADOSE = "PREF_VITEMADOSE"

    private const val PREF_DEPARTMENT = "PREF_DEPARTMENT"
    private const val PREF_CACHE_DEPARTMENT_LIST = "PREF_CACHE_DEPARTMENT_LIST"

    private val sharedPrefs: SharedPreferences
        get() = ViteMaDoseApp.get().getSharedPreferences(PREF_VITEMADOSE, Context.MODE_PRIVATE)

    var favDepartment: Department?
        get(){
            val myType = object : TypeToken<Department>() {}.type
            return try {
                Gson().fromJson(sharedPrefs.getString(PREF_DEPARTMENT, null), myType)
            }catch (e: Exception){
                null
            }
        }
        set(value) {
            val json = try {
                Gson().toJson(value)
            }catch (e: Exception){
                null
            }
            json?.let { sharedPrefs.edit().putString(PREF_DEPARTMENT, it).apply() }
        }

    var cacheDepartmentList: List<Department>?
        get(){
            val myType = object : TypeToken<List<Department>>() {}.type
            return try {
                Gson().fromJson(sharedPrefs.getString(PREF_CACHE_DEPARTMENT_LIST, null), myType)
            }catch (e: Exception){
                null
            }
        }
        set(value) {
            val json = try {
                Gson().toJson(value)
            }catch (e: Exception){
                null
            }
            json?.let { sharedPrefs.edit().putString(PREF_CACHE_DEPARTMENT_LIST, it).apply() }
        }
}