package com.cvtracker.vmd.data

import androidx.annotation.DrawableRes
import com.cvtracker.vmd.R

enum class Plateform(val id: String, val label: String, @DrawableRes val logo: Int) {
    DOCTOLIB("Doctolib", "Doctolib.fr", R.drawable.logo_doctolib),
    KELDOC("Keldoc", "Keldoc.com", R.drawable.logo_keldoc),
    MAIIA("Maiia", "Maiia.com", R.drawable.logo_maiia),
    ORDOCLIC("Ordoclic", "Ordoclic.fr", R.drawable.logo_ordoclic),
    PANDALAB("Pandalab", "Pandalab.fr", R.drawable.logo_pandalab),
    MAPHARMA("Mapharma", "mapharma.net", R.drawable.logo_mapharma),
    AVECMONDOC("AvecMonDoc", "avecmondoc.com", R.drawable.logo_avecmondoc),
    CLIKODOC("Clikodoc", "clikodoc.com", R.drawable.logo_clikodoc),
    MESOIGNER("mesoigner", "mesoigner.fr", R.drawable.logo_mesoigner);

    companion object {
        fun fromId(id: String): Plateform? = values().firstOrNull { it.id == id }
    }
}