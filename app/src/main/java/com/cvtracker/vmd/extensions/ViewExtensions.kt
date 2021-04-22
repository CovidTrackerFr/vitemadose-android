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
        }.build().intent
        customTabsIntent.data = Uri.parse(url)
        startActivityExcludingOwnApp(this, customTabsIntent)
    } catch (e: ActivityNotFoundException) {
        try {
            startActivityExcludingOwnApp(this, Intent(Intent.ACTION_VIEW).setData(Uri.parse(url)))
        } catch (e1: ActivityNotFoundException) {
            Toast.makeText(this, R.string.no_app_activity_found, Toast.LENGTH_SHORT).show()
        }
    }
}

/**
 * Attempts to start an activity to handle the given intent, excluding activities of this app.
 *
 *  * If the user has set a default activity (which does not belong in this app's package), it is opened without prompt.
 *  * Otherwise, an intent chooser is displayed that excludes activities of this app's package.
 *
 *
 * @param context context
 * @param intent intent to open
 */
fun startActivityExcludingOwnApp(context: Context, intent: Intent) {
    val possibleIntents: MutableList<Intent> = ArrayList()
    val possiblePackageNames: MutableSet<String> = HashSet()
    for (resolveInfo in context.packageManager.queryIntentActivities(intent, 0)) {
        val packageName = resolveInfo.activityInfo.packageName
        if (packageName != context.packageName) {
            val possibleIntent = Intent(intent)
            possibleIntent.setPackage(resolveInfo.activityInfo.packageName)
            possiblePackageNames.add(resolveInfo.activityInfo.packageName)
            possibleIntents.add(possibleIntent)
        }
    }
    val defaultResolveInfo = context.packageManager.resolveActivity(intent, 0)
    if (defaultResolveInfo == null || possiblePackageNames.isEmpty()) {
        throw ActivityNotFoundException()
    }

    // If there is a default app to handle the intent (which is not this app), use it.
    if (possiblePackageNames.contains(defaultResolveInfo.activityInfo.packageName)) {
        context.startActivity(intent)
    } else { // Otherwise, let the user choose.
        val intentChooser = Intent.createChooser(possibleIntents.removeAt(0), context.getString(R.string.open_with))
        intentChooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, possibleIntents.toTypedArray())
        context.startActivity(intentChooser)
    }
}