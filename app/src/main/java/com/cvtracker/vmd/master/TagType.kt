package com.cvtracker.vmd.master

import androidx.annotation.StringRes
import com.cvtracker.vmd.R

enum class TagType(val key: String, @StringRes val titleRes: Int) {
    FIRST_SHOT( "first_or_second_dose", R.string.tag_type_first_shot),
    THIRD_SHOT("third_dose", R.string.tag_type_third_shot),
    KID_SHOT( "kids_first_dose", R.string.tag_type_kid),;
}