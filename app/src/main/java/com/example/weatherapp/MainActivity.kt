package com.example.weatherapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.weatherapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val weatherRepository = WeatherRepository(GetAPI.getAPI)
    private val weatherViewModel = WeatherViewModel(weatherRepository)

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        weatherViewModel.weatherData.observe(this, Observer { dataResponse ->
//            // Update UI with dataResponse
//        })
    }

//    private fun getCityData(cityName: String) {
//        val cityRepository = CityRepository(GetAPI.getAPI)
//        val cityViewModel = CityViewModel(cityRepository)
//        cityViewModel.getCityData(cityName)
//        cityViewModel.cityData.observe(this, Observer { dataResponse ->
//            // Update UI with dataResponse
//        })
//    }
}