package com.example.myapitest.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Item(
    @SerializedName("id")
    val id: String?,
    
    @SerializedName("imageUrl")
    val imageUrl: String?,
    
    @SerializedName("year")
    val year: String?,
    
    @SerializedName("name")
    val name: String?,
    
    @SerializedName("licence")
    val licence: String?,
    
    @SerializedName("place")
    val place: ItemLocation?,

    // Suporte para o retorno aninhado do GET por ID
    @SerializedName("value")
    val value: Item? = null
) : Serializable

data class ItemLocation(
    @SerializedName("lat")
    val lat: Double,
    @SerializedName("long")
    val long: Double
) : Serializable
