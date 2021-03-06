package com.cvtracker.vmd.data

import com.google.gson.annotations.SerializedName
import java.util.*


data class ContributorResponse(
    @SerializedName("contributors")
    val contributors: List<Contributor>
)

data class Contributor(
    @SerializedName("nom")
    val name: String?,
    @SerializedName("pseudo")
    val pseudo: String?,
    @SerializedName("photo")
    val avatarUrl: String?,
    @SerializedName("teams")
    val teams: List<String>,
    @SerializedName("links")
    val links: List<Link>
) {

    val displayName: String
        get() = (name ?: pseudo ?: "Unknown").split(" ").joinToString(" ") { it.capitalize(Locale.ROOT) }

    data class Link(
        @SerializedName("site")
        val site: String?,
        @SerializedName("url")
        val url: String?
    )
}