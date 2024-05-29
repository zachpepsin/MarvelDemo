package com.example.marveldemo.data

import androidx.annotation.StringRes
import com.example.marveldemo.BuildConfig
import com.example.marveldemo.R
import com.example.marveldemo.data.database.ComicsResponse
import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.security.MessageDigest

interface ComicsService {
    /**
     * Retrieves the JSON array of comic objects from the backend service and convert it to a
     * [ComicsResponse]
     *
     * @return a [ComicsResponse] which nested within contains a
     * list of [com.example.marveldemo.data.database.Comic]s
     */
    @GET("comics")
    suspend fun getComics(
        @Query("ts") timestamp: Long = System.currentTimeMillis(),
        @Query("apikey") publicKey: String = BuildConfig.PUBLIC_KEY,
        @Query("hash") hash: String = generateHash(timestamp),
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("orderBy") orderBy: String,
        @Query("titleStartsWith") titleStartsWith: String?
    ): ComicsResponse

    /**
     * Retrieves the JSON array of comic objects from the backend service and convert it to a
     * [ComicsResponse]
     *
     * @return a [ComicsResponse] which nested within contains a
     * list of [com.example.marveldemo.data.database.Comic]s that will have
     * either 0 or 1 items
     */
    @GET("comics/{id}")
    suspend fun getComic(
        @Path("id") id: Int,
        @Query("ts") timestamp: Long = System.currentTimeMillis(),
        @Query("apikey") publicKey: String = BuildConfig.PUBLIC_KEY,
        @Query("hash") hash: String = generateHash(timestamp),
    ): ComicsResponse

    enum class ComicSortOrder {
        @SerializedName("focDate")
        FOC_DATE_ASC,

        @SerializedName("-focDate")
        FOC_DATE_DESC,

        @SerializedName("onsaleDate")
        ON_SALE_DATE_ASC,

        @SerializedName("-onsaleDate")
        ON_SALE_DATE_DESC,

        @SerializedName("title")
        TITLE_ASC,

        @SerializedName("-title")
        TITLE_DESC,

        @SerializedName("issueNumber")
        ISSUE_NUMBER_ASC,

        @SerializedName("-issueNumber")
        ISSUE_NUMBER_DESC,

        @SerializedName("modified")
        MODIFIED_ASC,

        @SerializedName("-modified")
        MODIFIED_DESC;

        override fun toString(): String {
            return javaClass
                .getField(name)
                .getAnnotation(SerializedName::class.java)!!
                .value
        }

        @StringRes
        fun getDescriptionRes(): Int =
            when (this) {
                FOC_DATE_ASC -> R.string.sort_foc_date_asc
                FOC_DATE_DESC -> R.string.sort_foc_date_desc
                ON_SALE_DATE_ASC -> R.string.sort_on_sale_date_asc
                ON_SALE_DATE_DESC -> R.string.sort_on_sale_date_desc
                TITLE_ASC -> R.string.sort_title_asc
                TITLE_DESC -> R.string.sort_title_desc
                ISSUE_NUMBER_ASC -> R.string.sort_issue_number_asc
                ISSUE_NUMBER_DESC -> R.string.sort_issue_number_desc
                MODIFIED_ASC -> R.string.sort_modified_asc
                MODIFIED_DESC -> R.string.sort_modified_desc
            }
    }

    companion object {
        @OptIn(ExperimentalStdlibApi::class)
        private fun generateHash(timestamp: Long): String =
            MessageDigest.getInstance("MD5")
                .digest(("${timestamp}${BuildConfig.PRIVATE_KEY}${BuildConfig.PUBLIC_KEY}").toByteArray())
                .toHexString()
    }
}