package com.cvtracker.vmd.data

import com.google.gson.annotations.SerializedName

data class Contributor(
    @SerializedName("nom")
    val name: String,
    @SerializedName("photo")
    val avatarUrl: String,
    @SerializedName("teams")
    val teams: List<String>,
    @SerializedName("links")
    val links: List<Link>
) {
    data class Link(
        @SerializedName("site")
        val site: String,
        @SerializedName("url")
        val url: String
    )
}