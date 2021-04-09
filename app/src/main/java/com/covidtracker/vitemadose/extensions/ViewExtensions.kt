package com.covidtracker.vitemadose.extensions

import android.app.Activity
import android.content.res.Configuration
import android.content.res.Resources
import android.util.TypedValue
import android.view.View
import androidx.annotation.AttrRes
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat

fun View.dpToPx(valueInDp: Float): Int {
    return (valueInDp * context.resources.displayMetrics.density).toInt()
}

fun View.colorAttr(@AttrRes resId: Int): Int {
    val typedValue = TypedValue()
    context.theme.resolveAttribute(resId, typedValue, true)
    return typedValue.data
}

fun Activity.colorAttr(@AttrRes resId: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(resId, typedValue, true)
    return typedValue.data
}

fun View.color(@ColorRes resId: Int) = ContextCompat.getColor(context, resId)

fun Activity.color(@ColorRes resId: Int) = ContextCompat.getColor(this, resId)

fun Activity.isDarkTheme(): Boolean {
    return resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
}

fun View.show(){
    visibility = View.VISIBLE
}

fun View.hide(){
    visibility = View.GONE
}

fun View.mask(){
    visibility = View.INVISIBLE
}

fun Resources.dpToPx(dp: Float): Int {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, displayMetrics).toInt()
}