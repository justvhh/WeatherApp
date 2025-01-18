package com.example.weatherapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.api.GetAPI
import com.example.weatherapp.database.WeatherDataResponse
import kotlinx.coroutines.launch
import retrofit2.Response

class WeatherViewModel(private val response: WeatherRepository) : ViewModel() {
    private val _weatherData = MutableLiveData<WeatherDataResponse>()
    val weatherData: LiveData<WeatherDataResponse> = _weatherData

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    fun getWeatherData(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = response.getCurrentWeather(latitude, longitude)
                if (response.isSuccessful) {
                    _weatherData.value = response.body()
                } else {
                    _error.value = response.errorBody()?.string()
                }
            }catch (e: Exception) {
                _error.value = e.message
            }finally {
                _isLoading.value = false
            }

        }
    }
}

class WeatherRepository(private val api: GetAPI) {
    suspend fun getCurrentWeather(latitude: Double, longitude: Double): Response<WeatherDataResponse> {
        return api.getWeatherData(latitude, longitude)
    }
}