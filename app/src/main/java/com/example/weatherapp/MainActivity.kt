package com.example.weatherapp

import android.os.Bundle
import android.util.Log
import android.widget.SearchView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.weatherapp.databinding.ActivityMainBinding
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// cc8cb96d120861ae17bc9654db6b0fd9 API
class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy{
        ActivityMainBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
//        fetchWeatherData("Faisalabad")
        searchCity()
        binding.day.text = dayName(System.currentTimeMillis())
        binding.date.text = date()
        binding.cityName.text = "Search a City"
    }

    private fun searchCity() {
        val searchView = binding.searchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    fetchWeatherData(query)
                }
                searchView.clearFocus()
                return true
            }

        })
    }

    private fun changeImagesWeather(condition: String) {
        when {
            condition.contains("Clear", ignoreCase = true) || condition.contains("Sunny", ignoreCase = true) -> {
                binding.backgroundImage.setImageResource(R.drawable.sunny_background)
                binding.lottieAnimationView.setAnimation(R.raw.sun)
            }
            condition.contains("Clouds", ignoreCase = true) || condition.contains("Overcast" , ignoreCase = true) || condition.contains("Haze", ignoreCase = true) || condition.contains("Fog", ignoreCase = true) || condition.contains("Smoke", ignoreCase = true) || condition.contains("Mist", ignoreCase = true)-> {
                binding.backgroundImage.setImageResource(R.drawable.colud_background)
                binding.lottieAnimationView.setAnimation(R.raw.cloud)
            }
            condition.contains("Rain", ignoreCase = true) || condition.contains("Drizzle", ignoreCase = true) || condition.contains("Shower", ignoreCase = true) -> {
                binding.backgroundImage.setImageResource(R.drawable.rain_background)
                binding.lottieAnimationView.setAnimation(R.raw.rain)
            }
            condition.contains("Snow", ignoreCase = true) || condition.contains("Blizzard", ignoreCase = true) -> {
                binding.backgroundImage.setImageResource(R.drawable.snow_background)
                binding.lottieAnimationView.setAnimation(R.raw.snow)
            }
            else -> {
                binding.backgroundImage.setImageResource(R.drawable.sunny_background)
                binding.lottieAnimationView.setAnimation(R.raw.sun)
            }
        }
        binding.lottieAnimationView.playAnimation()
    }

    private fun fetchWeatherData(cityName: String?) {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .build().create(ApiInterface::class.java)
        val response = retrofit.getWeatherData(cityName,"cc8cb96d120861ae17bc9654db6b0fd9", "metric")
        response.enqueue(object : Callback<WeatherApp> {
            override fun onResponse(
                call: Call<WeatherApp?>,
                response: Response<WeatherApp?>
            ) {
                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null){
                    val temperature = responseBody.main?.temp.toString()
                    val humidity = responseBody.main?.humidity
                    val windSpeed = responseBody.wind?.speed
                    val sunRise = responseBody.sys?.sunrise?.toLong()
                    val sunSet = responseBody.sys?.sunset?.toLong()
                    val seaLevel = responseBody.main?.pressure
                    val condition = responseBody.weather.firstOrNull()?.main?: "unknown"
                    val maxTemp = responseBody.main?.temp_max
                    val minTemp = responseBody.main?.temp_min

                    binding.temp.text = "$temperature °C"
                    binding.weather.text = condition
                    binding.maxTemp.text = "Max: $maxTemp °C"
                    binding.minTemp.text = "Min: $minTemp °C"
                    binding.humidity.text = "$humidity %"
                    binding.windSpeed.text = "$windSpeed m/s"
                    binding.sunRise.text = "${time(sunRise)}"
                    binding.sunSet.text = "${time(sunSet)}"
                    binding.sea.text = "$seaLevel hPa"
                    binding.condition.text = condition
                    binding.day.text = dayName(System.currentTimeMillis())
                    binding.date.text = date()
                    binding.cityName.text = "$cityName"

                    changeImagesWeather(condition)

                }
            }

            override fun onFailure(
                call: Call<WeatherApp?>,
                t: Throwable
            ) {
                Log.e("TAG", "API Call Failed: ${t.message}")            }
        })
    }
    private fun date(): String {
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        return sdf.format(Date())
    }
    private fun time(timestamp: Long?): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp?.times(1000) ?: 0L))
    }
    fun dayName(timestamp: Long): String {
        val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
        return sdf.format(Date())
    }
}