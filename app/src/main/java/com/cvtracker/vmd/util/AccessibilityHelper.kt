package com.cvtracker.vmd.util

import android.content.Context
import android.view.accessibility.AccessibilityManager

fun Context.isTalkbackEnabled(): Boolean {
    val am = this.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    return am.isTouchExplorationEnabled
}