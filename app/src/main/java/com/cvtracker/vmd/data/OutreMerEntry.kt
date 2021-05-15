package com.cvtracker.vmd.data

enum class OutreMerEntry(val code: String, val postalCode: String, val latitude: Double, val longitude: Double) {
    SAINT_BARTHELEMY("97701", "97133", 17.9034,-62.8314),
    SAINT_MARTIN("97801", "97150", 18.0409,-63.0785),
    SAINT_PIERRE("97502", "97500", 46.7667, -56.1833),
    MIQUELON_LANGLADE("97501", "97500", 47.0975, -56.3814);

    val departmentCode = "om"

    companion object {
        fun fromCode(code: String): OutreMerEntry? {
            return values().firstOrNull { it.code == code }
        }
    }
}