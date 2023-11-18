package com.example.homework3

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    @SerializedName("location") val location: Location,
    @SerializedName("current") val current: Current
)

data class Location(
    @SerializedName("name") val name: String
)

data class Current(
    @SerializedName("temp_c") val temperatureCelsius: Double,
    @SerializedName("condition") val condition: Condition
)

data class Condition(
    @SerializedName("text") val description: String,
    @SerializedName("icon") val icon: String
)