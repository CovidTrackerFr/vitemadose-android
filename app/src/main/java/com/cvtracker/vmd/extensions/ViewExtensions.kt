package com.cvtracker.vmd.extensions

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.net.Uri
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.annotation.ColorRes
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import com.cvtracker.vmd.R

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

fun View.show() {
    visibility = View.VISIBLE
}

fun View.hide() {
    visibility = View.GONE
}

fun View.mask() {
    visibility = View.INVISIBLE
}

fun Resources.dpToPx(dp: Float): Int {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, displayMetrics).toInt()
}

@Suppress("DEPRECATION")
fun Activity.launchWebUrl(url: String) {
    try {
        val customTabsIntent = CustomTabsIntent.Builder().apply {
            setToolbarColor(colorAttr(R.attr.colorPrimary))
        }.build()
        customTabsIntent.launchUrl(this, Uri.parse(url))
    }catch (e: ActivityNotFoundException){
        try{
            startActivity(Intent(Intent.ACTION_VIEW).setData(Uri.parse(url)))
        }catch (e1: ActivityNotFoundException){
            Toast.makeText(this, R.string.no_app_activity_found, Toast.LENGTH_SHORT).show()
        }
    }
}

fun View.hideKeyboard() {
    (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(windowToken, 0)
}