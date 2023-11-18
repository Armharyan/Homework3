package com.example.homework3

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class WeatherViewModel : ViewModel() {

    private val _weatherData = MutableLiveData<List<City>>()
    val weatherData: LiveData<List<City>> get() = _weatherData

    fun fetchWeatherData(weatherApiService: WeatherApiService, cities: List<City>) {
        viewModelScope.launch {
            val updatedCities = mutableListOf<City>()

            cities.forEach { city ->
                try {
                    val response = weatherApiService.getCurrentWeather(
                        apiKey = "6339a15949794fddb2d201051232411",
                        cityName = city.name
                    ).execute()

                    if (response.isSuccessful) {
                        val weatherResponse = response.body()
                        city.weather = weatherResponse
                        updatedCities.add(city)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            _weatherData.postValue(updatedCities)
        }
    }
}