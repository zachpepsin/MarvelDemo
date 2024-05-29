package com.example.marveldemo.data.database

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ComicsResponse(
    @Json(name = "data") val comicDataContainer: ComicDataContainer
)

@JsonClass(generateAdapter = true)
data class ComicDataContainer(
    @Json(name = "offset") val offset: Int?,
    @Json(name = "total") val total: Int?,
    @Json(name = "count") val count: Int?,
    @Json(name = "results") val results: List<Comic>
)

@Entity(tableName = "comics")
@JsonClass(generateAdapter = true)
data class Comic(
    @Json(name = "id") val id: Int,

    @PrimaryKey(autoGenerate = true)
    val  incrementedId: Int = 0, // Use this to sort the data, since comic id is not a sort option

    @Json(name = "title") val title: String?,

    @Embedded(prefix = "comic_image_")
    @Json(name = "thumbnail") val thumbnail: ComicImage?,

    @Json(name = "description") val description: String?,

    @Json(name = "textObjects")
    val textObjects: List<TextObject>?,

    @Embedded(prefix = "comic_creator_list_")
    @Json(name = "creators") val creatorList: CreatorList?,

    @Embedded(prefix = "comic_character_list_")
    @Json(name = "characters") val characterList: CharacterList?,


) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Comic

        if (id != other.id) return false
        if (incrementedId != other.incrementedId) return false
        if (title != other.title) return false
        if (thumbnail != other.thumbnail) return false
        if (description != other.description) return false
        if (textObjects != other.textObjects) return false
        if (creatorList != other.creatorList) return false
        if (characterList != other.characterList) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + incrementedId
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + (thumbnail?.hashCode() ?: 0)
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + (textObjects?.hashCode() ?: 0)
        result = 31 * result + (creatorList?.hashCode() ?: 0)
        result = 31 * result + (characterList?.hashCode() ?: 0)
        return result
    }
}

@JsonClass(generateAdapter = true)
data class ComicImage(
    @Json(name = "path") val path: String?,
    @Json(name = "extension") val extension: String?
)

@JsonClass(generateAdapter = true)
data class TextObject(
    @Json(name = "type") val type: String?,
    @Json(name = "language") val language: String?,
    @Json(name = "text") val text: String?,
)

@JsonClass(generateAdapter = true)
data class CreatorList(
    @Json(name = "items") val items: List<CreatorSummary>?,
)

@JsonClass(generateAdapter = true)
data class CreatorSummary(
    @Json(name = "name") val name: String?,
    @Json(name = "role") val role: String?
)

@JsonClass(generateAdapter = true)
data class CharacterList(
    @Json(name = "items") val items: List<CharacterSummary>?,
)

@JsonClass(generateAdapter = true)
data class CharacterSummary(
    @Json(name = "name") val name: String?,
    @Json(name = "role") val role: String?
)
