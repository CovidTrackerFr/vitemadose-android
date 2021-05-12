package com.cvtracker.vmd.data

import android.content.Context
import com.cvtracker.vmd.R
import com.cvtracker.vmd.extensions.colorAttr

enum class DisclaimerSeverity {
    INFO,
    WARNING,
    ERROR;

    fun textColor(context: Context) = when(this){
        INFO -> context.colorAttr(R.attr.disclaimerInfoTextColor)
        WARNING -> context.colorAttr(R.attr.disclaimerWarningTextColor)
        ERROR -> context.colorAttr(R.attr.disclaimerErrorTextColor)
    }

    fun backgroundColor(context: Context) = when(this){
        INFO -> context.colorAttr(R.attr.disclaimerInfoBackgroundColor)
        WARNING -> context.colorAttr(R.attr.disclaimerWarningBackgroundColor)
        ERROR -> context.colorAttr(R.attr.disclaimerErrorBackgroundColor)
    }
}