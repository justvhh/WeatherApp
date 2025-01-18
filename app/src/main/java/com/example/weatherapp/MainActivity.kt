package com.example.weatherapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.BuildCompat
import com.example.weatherapp.api.GetAPI
import com.example.weatherapp.database.WeatherDataResponse
import com.example.weatherapp.databinding.ActivityMainBinding
import com.example.weatherapp.viewmodel.CityRepository
import com.example.weatherapp.viewmodel.CityViewModel
import com.example.weatherapp.viewmodel.WeatherRepository
import com.example.weatherapp.viewmodel.WeatherViewModel
import com.google.android.gms.location.*
import com.squareup.picasso.BuildConfig
import com.squareup.picasso.Picasso
import java.util.*
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var currentLocation: String
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        checkLocationPermission()
        onAddCityButtonClicked()
        checkInputComplete()
        setupTouchListener()
    }


    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                100
            )
        }
        else{
            updateBackground()
            loadWeatherData()
            refreshWeatherData()
        }
    }

    private fun isGpsEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 100) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                updateBackground()
                loadWeatherData()
                refreshWeatherData()
            } else {
                Toast.makeText(this, "Permission denied. Cannot access location.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateBackground() {
        val currentTime = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val textColor = if (currentTime in 6..17) R.color.black else R.color.white
        val backgroundResource = if (currentTime in 6..17) R.drawable.day else R.drawable.night

        setTextColorForViews(textColor)

        binding.root.setBackgroundResource(backgroundResource)
    }

    private fun setTextColorForViews(colorResId: Int) {
        val color = ContextCompat.getColor(this, colorResId)
        binding.cityName.setTextColor(color)
        binding.currentTemp.setTextColor(color)
        binding.minTemp.setTextColor(color)
        binding.maxTemp.setTextColor(color)
        binding.humidity.setTextColor(color)
        binding.wind.setTextColor(color)
        binding.weatherDescription.setTextColor(color)
        binding.cityInput.setTextColor(color)
    }

    private fun getSavedWeatherData(): WeatherDataResponse? {
        val sharedPref = getSharedPreferences("weatherData", Context.MODE_PRIVATE)
        val weatherData = sharedPref.getString("weatherData", null)
        return if (weatherData != null) {
            GetAPI.weatherAdapter.fromJson(weatherData)
        } else {
            null
        }
    }

    private fun loadWeatherData() {
        val weatherData = getSavedWeatherData()
        if (weatherData != null) {
            updateWeatherUI(weatherData)
        } else {
            if (isGpsEnabled()){
                currentWeatherData()
            }else{
                Toast.makeText(this, "GPS is required for checking weather", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun refreshWeatherData() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            currentWeatherData()
            binding.wind.visibility = View.VISIBLE
            binding.humidity.visibility = View.VISIBLE
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun currentWeatherData() {
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setMinUpdateIntervalMillis(5000)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                val location = locationResult.lastLocation
                if (location != null) {
                    val lat = location.latitude
                    val lon = location.longitude
                    val geocoder = Geocoder(this@MainActivity, Locale.getDefault())
                    try {
                        val addresses = geocoder.getFromLocation(lat, lon, 1)
                        if (!addresses.isNullOrEmpty()) {
                            val cityName = addresses[0].adminArea
                            currentLocation = cityName
                        } else {
                            Toast.makeText(this@MainActivity, "Error connection", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    getWeatherData(lat, lon)
                }
            }
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    private fun getWeatherData(latitude: Double, longitude: Double) {
        val weatherRepository = WeatherRepository(GetAPI.getAPI)
        val weatherViewModel = WeatherViewModel(weatherRepository)
        weatherViewModel.getWeatherData(latitude, longitude)
        weatherViewModel.weatherData.observe(this) { weatherData ->
            weatherData.cityName = currentLocation
            updateWeatherUI(weatherData)
        }
    }

    private fun updateWeatherUI(weatherData: WeatherDataResponse) {
        binding.cityName.text = weatherData.cityName
        binding.currentTemp.text = "${weatherData.weatherDetail.temp.roundToInt()}°C"
        binding.minTemp.text = "Max temp: ${weatherData.weatherDetail.tempMax.roundToInt()}°C"
        binding.maxTemp.text = "Min temp: ${weatherData.weatherDetail.tempMin.roundToInt()}°C"
        binding.humidity.text = "Humidity: ${weatherData.weatherDetail.humidity}%"
        binding.wind.text = "Wind: ${weatherData.windDetail.speed}m/s"
        if (weatherData.weather.firstOrNull()?.icon != null) {
            val icon = weatherData.weather.firstOrNull()?.icon
            val iconUrl = "https://openweathermap.org/img/w/$icon.png"
            Picasso.get()
                .load(iconUrl)
                .resize(300, 300)
                .into(binding.weatherIcon)
            binding.weatherDescription.text = weatherData.weather.first().description.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.getDefault()
                ) else it.toString()
            }
        }
        saveWeatherData(weatherData)
    }

    private fun saveWeatherData(weatherData: WeatherDataResponse) {
        val sharedPref = getSharedPreferences("weatherData", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("weatherData", GetAPI.weatherAdapter.toJson(weatherData))
            apply()
        }
    }

    private fun onAddCityButtonClicked() {
        binding.addButton.setOnClickListener {
            binding.addButton.visibility = View.GONE
            binding.cityInput.visibility = View.VISIBLE
            binding.cityInput.requestFocus()
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.cityInput, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun checkInputComplete() {
        binding.cityInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                getCityData(binding.cityInput.text.toString())
                binding.cityInput.clearFocus()
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.cityInput.windowToken, 0)
                return@setOnEditorActionListener true
            }
            false
        }
        binding.cityInput.setOnFocusChangeListener { _, focus ->
            if (!focus) {
                binding.addButton.visibility = View.VISIBLE
                binding.cityInput.visibility = View.GONE
                binding.cityInput.text.clear()
            }
        }
    }

    private fun getCityData(cityName: String) {
        val cityRepository = CityRepository(GetAPI.getAPI)
        val cityViewModel = CityViewModel(cityRepository)
        cityViewModel.getCityData(cityName)
        cityViewModel.cityData.observe(this) { cityData ->
            cityData.firstOrNull()?.let {
                currentLocation = it.cityName
                getWeatherData(it.latitude, it.longitude)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupTouchListener() {
        binding.main.setOnTouchListener { view, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                binding.cityInput.clearFocus()
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
                view.performClick()
            }
            false
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        if (::currentLocation.isInitialized){
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
}