package com.simformsolutions.myspotify.data.model.local


data class LibraryDisplayData(
    val index: Int,
    val isFiltered: Boolean,
    val type: LibraryItemType,
    val data: List<LibraryDisplay>
)
data class LibraryDisplay(
    val isFiltered: Boolean,
    val name: String?,
    val ownerDisplayName: String?,
    val image: String?,
    val type: LibraryItemType,
    val id: String?,
    var description: String = ""
)

enum class LibraryItemType(val value: Int) {
    PLAYLIST(0),
    ALBUM(1),
    ARTISTS(2),
    PODCAST(3)
}