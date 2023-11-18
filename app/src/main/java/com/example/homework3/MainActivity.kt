package com.example.homework3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


data class City(
    val name: String,
    val description: String,
    val imageRes: Int,
    var weather: WeatherResponse? = null
)

@Composable
fun WelcomeScreen(
    navController: NavHostController,
    viewModel: WeatherViewModel,
) {

    Surface(
        modifier = androidx.compose.ui.Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ){
        Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally )
        {
        Text(text = "Welcome to the App!", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { navController.navigate("second_screen") }) {
                Text(text = "Go to Second Screen")
            }
       }
    }
}

@Composable
fun SecondScreen(
    cities: List<City>,
    navController: NavHostController,
    weatherApiService: WeatherApiService,
    weatherViewModel: WeatherViewModel = viewModel(),
    locationPermissionHandler: LocationPermissionHandler
) {
    val weatherData by weatherViewModel.weatherData.observeAsState(initial = emptyList())
    val locationPermissionResult by locationPermissionHandler.permissionResult.collectAsState(initial = true)

    LaunchedEffect(locationPermissionResult) {
        if (locationPermissionResult) {
            weatherViewModel.fetchWeatherData(weatherApiService, cities)
        }
    }
    LazyColumn {
        items(cities) { city ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(text = city.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = city.description)
                Spacer(modifier = Modifier.height(8.dp))
                Image(
                    painter = painterResource(id = city.imageRes),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(shape = RoundedCornerShape(4.dp)),
                    contentScale = ContentScale.Crop
                )
                city.weather?.let { weatherResponse ->
                    Text(
                        text = "${weatherResponse.current.temperatureCelsius}°C",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        item {
            Button(onClick = { navController.popBackStack() }) {
                Text(text = "Go Back to Welcome Screen")
            }
        }
    }
}

class MainActivity : ComponentActivity() {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.weatherapi.com/v1/") // Adjust the base URL based on API documentation
        .addConverterFactory(GsonConverterFactory.create())
        .client(OkHttpClient.Builder().addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }).build())
        .build()

    private val weatherApiService = retrofit.create(WeatherApiService::class.java)
    private val locationPermissionHandler: LocationPermissionHandler by viewModels()
    private val weatherViewModel: WeatherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationPermissionHandler.checkLocationPermission(this@MainActivity)
        setContent {

            val navController = rememberNavController()
            val locationPermissionHandler: LocationPermissionHandler by viewModels()

            LaunchedEffect(locationPermissionHandler.permissionResult) {
                locationPermissionHandler.checkLocationPermission(this@MainActivity)
            }
            NavHost(navController, startDestination = "welcome_screen") {
                composable("welcome_screen") {
                    WelcomeScreen(navController, viewModel())
                }
                composable("second_screen") {
                    val cities = listOf(
                        City("Yerevan", "Capital of Armenia", R.drawable.yerevan),
                        City("Washington", "Capital of the United States", R.drawable.washington),
                        City("Madrid", "Capital of Spain", R.drawable.madrid)
                        // Add more cities
                    )
                    // Fetch weather information for each city
                    cities.forEach { city ->
                        // Use CoroutineScope for asynchronous operations
                        lifecycleScope.launch {
                            try {
                                val response = weatherApiService.getCurrentWeather(
                                    apiKey = "6339a15949794fddb2d201051232411",
                                    cityName = city.name
                                ).execute()

                                if (response.isSuccessful) {
                                    val weatherResponse = response.body()
                                    city.weather = weatherResponse
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                    SecondScreen(cities, navController, weatherApiService, weatherViewModel = weatherViewModel, locationPermissionHandler )
                }
            }
        }
    }
}
