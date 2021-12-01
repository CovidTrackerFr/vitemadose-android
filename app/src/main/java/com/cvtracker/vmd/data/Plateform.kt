package com.cvtracker.vmd.data

import androidx.annotation.DrawableRes
import com.cvtracker.vmd.R

enum class Plateform(val id: String, val label: String, @DrawableRes val logo: Int) {
    DOCTOLIB("Doctolib", "Doctolib.fr", R.drawable.logo_doctolib),
    KELDOC("Keldoc", "Keldoc.com", R.drawable.logo_keldoc),
    MAIIA("Maiia", "Maiia.com", R.drawable.logo_maiia),
    ORDOCLIC("Ordoclic", "Ordoclic.fr", R.drawable.logo_ordoclic),
    MAPHARMA("Mapharma", "mapharma.net", R.drawable.logo_mapharma),
    AVECMONDOC("AvecMonDoc", "avecmondoc.com", R.drawable.logo_avecmondoc),
    MESOIGNER("mesoigner", "mesoigner.fr", R.drawable.logo_mesoigner),
    VALWIN("Valwin", "valwin.fr", R.drawable.logo_valwin),
    BIMEDOC("Bimedoc", "bimedoc.fr", R.drawable.logo_bimedoc);

    companion object {
        fun fromId(id: String): Plateform? = values().firstOrNull { it.id == id }
    }
}