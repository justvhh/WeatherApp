package com.example.weatherapp.api

import com.example.weatherapp.database.CityDataResponse
import com.example.weatherapp.database.WeatherDataResponse
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.example.weatherapp.BuildConfig
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface GetAPI {
    @GET("data/2.5/weather")
    suspend fun getWeatherData(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String = BuildConfig.API_KEY,
        @Query("units") dataType: String = "metric"
    ): Response<WeatherDataResponse>
    @GET("geo/1.0/direct")
     suspend fun getCityData(
        @Query("q") cityName: String,
        @Query("appid") apiKey: String = BuildConfig.API_KEY
    ): Response<List<CityDataResponse>>
    companion object{
        private const val URL = "https://api.openweathermap.org/"

        private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

        private val retrofit = Retrofit.Builder()
            .baseUrl(URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        val getAPI: GetAPI by lazy {
            retrofit.create(GetAPI::class.java)
        }

        val weatherAdapter: JsonAdapter<WeatherDataResponse> by lazy {
            moshi.adapter(WeatherDataResponse::class.java)
        }
    }
}