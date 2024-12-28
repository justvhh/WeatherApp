package com.example.weatherapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.api.GetAPI
import com.example.weatherapp.database.CityDataResponse
import kotlinx.coroutines.launch
import retrofit2.Response


class CityViewModel(private val response: CityRepository): ViewModel() {
    private val _cityData = MutableLiveData<List<CityDataResponse>>()
    val cityData: LiveData<List<CityDataResponse>> = _cityData

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    fun getCityData(cityName: String) {
        viewModelScope.launch {
            try {
                val response = response.getCityData(cityName)
                if (response.isSuccessful) {
                    _cityData.value = response.body()
                } else {
                    _error.value = response.errorBody()?.string()
                }
            }catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}

class CityRepository(private val api: GetAPI) {
    suspend fun getCityData(cityName: String): Response<List<CityDataResponse>> {
        return api.getCityData(cityName)
    }
}