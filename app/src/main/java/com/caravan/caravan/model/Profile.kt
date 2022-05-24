package com.caravan.caravan.model

import java.io.Serializable

data class Profile(
    val id: Long,
    val name: String,
    val surname: String,
    val phoneNumber: String,
    val email: String? = null,
    val role: String = "TOURIST", // GUIDE
    val status: String = "ACTIVE", // BLOCK
    val profilePhoto: String? = null,
    val gender: String, // MALE, FEMALE
    val birthDate: String? = null,
    val createdDate: String,
    val updatedDate: String? = null,
    val appLanguage: String, // en, ru, uz
    val devices: ArrayList<Device>,
    val trips: ArrayList<Trip>? = null,
    val guides: ArrayList<GuideProfile>? = null
) : Serializable