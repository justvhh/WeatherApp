package com.example.weatherapp.database

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class WeatherDataResponse(
    @Json(name = "weather") val weather: List<Weather>,
    @Json(name = "main") val weatherDetail: Main,
    @Json(name = "name") var cityName: String,
    @Json(name = "wind") val windDetail: Wind
)

@JsonClass(generateAdapter = true)
data class Main (
    val temp: Double,
    @Json(name = "temp_min") val tempMin: Double,
    @Json(name = "temp_max") val tempMax: Double,
    val humidity: Int
)
@JsonClass(generateAdapter = true)
data class Weather(
    val description: String,
    val icon: String
)
@JsonClass(generateAdapter = true)
data class Wind(
    val speed: Double
)

@JsonClass(generateAdapter = true)
data class CityDataResponse(
    @Json(name = "name") val cityName: String,
    @Json(name = "lat") val latitude: Double,
    @Json(name = "lon") val longitude: Double
)