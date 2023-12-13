package com.example.simpleweather

import WeatherData
import WeatherManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.simpleweather.ui.theme.SimpleWeatherTheme

import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.draw.clip

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.ColorUtils

import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SimpleWeatherTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WeatherApp()
                }
            }
        }
    }
}

@Composable
fun WeatherApp() {
    var weatherData by remember { mutableStateOf<WeatherData?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var cityInput by remember { mutableStateOf("") }

    DisposableEffect(Unit) {
        if (cityInput.isNotEmpty()) {
            isLoading = true
            WeatherManager().getWeatherData(cityInput) { data ->
                weatherData = data
                isLoading = false
            }
        }
        onDispose { }
    }

    val backgroundColor = weatherData?.let { getBackgroundColor(it.weatherIcon) } ?: Color.White

    val inputFieldColor = if (isDarkColor(MaterialTheme.colorScheme.background)) Color.White else Color.Black

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            OutlinedTextField(
                value = cityInput,
                onValueChange = { cityInput = it },
                label = { Text("Enter city", color = inputFieldColor) },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier
                    .weight(1f)
            )

            IconButton(
                onClick = {
                    if (cityInput.isNotEmpty()) {
                        isLoading = true
                        WeatherManager().getWeatherData(cityInput) { data ->
                            weatherData = data
                            isLoading = false
                            if (data.city == "") {
                                error = "Unknown city"
                            }
                        }
                    }
                }
            ) {
                Icon(imageVector = Icons.Default.Search, contentDescription = null)
            }
        }

        if (isLoading && cityInput.isNotEmpty() && weatherData?.city != "") {
            CircularProgressIndicator(modifier = Modifier.align(CenterHorizontally))
        } else if (weatherData != null && weatherData?.city != "") {
            WeatherInfoCard(weatherData!!)
        }else if(error != null){
            Text(error.toString(), color = Color.Red)
        }

    }
}


@Composable
fun WeatherInfoCard(weatherData: WeatherData) {
    val backgroundColor = getBackgroundColor(weatherData.weatherIcon)
    val isDarkBackground = isDarkColor(backgroundColor)
    val textColor = if (isDarkColor(backgroundColor)) Color.White else Color.Black


    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {

        Column(
            modifier = Modifier
                .padding(16.dp)

        ){
            Text(
                text = "City: ${weatherData.city}",
                style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = textColor)
            )

            val iconResourceId = getWeatherIconResourceId(weatherData.weatherIcon)
            Image(
                painter = painterResource(id = iconResourceId),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .padding(30.dp)
            )

            val temperatureCelsius = weatherData.temperature - 273.15
            Text(
                text = "Temperature: ${temperatureCelsius.roundToInt()}°C",
                style = TextStyle(fontSize = 18.sp, color = textColor)
            )

            val feelsLikeCelsius = weatherData.feelsLike - 273.15
            Text(
                text = "Feels like: ${feelsLikeCelsius.roundToInt()}°C",
                style = TextStyle(fontSize = 18.sp, color = textColor),
                modifier = Modifier.padding(top = 8.dp)
            )

            Text(
                text = "Humidity: ${weatherData.humidity}%",
                style = TextStyle(fontSize = 18.sp, color = textColor),
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = "Wind Speed: ${weatherData.windSpeed} m/s",
                style = TextStyle(fontSize = 18.sp, color = textColor),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}


@Composable
private fun isDarkColor(color: Color): Boolean {
    val luminance = ColorUtils.calculateLuminance(color.toArgb())
    return luminance < 0.5
}


@Composable
private fun getBackgroundColor(weatherIcon: String): Color {
    return when (weatherIcon) {
        "01d", "02d" -> Color(0xFF87CEEB) // день
        "01n", "02n", "03n", "04n", "09n", "10n", "11n", "13n","50n" -> Color(0xFF000080) // ночь
        "03d", "04d",  -> Color(0xFF778899) // день обалчно
        "09d", "10d", "11d", "13d",  "50d" -> Color(0xFF4682B4) // день дождь
        else -> Color(0xFFFFFFFF) // по умолчанию
    }
}


@Composable
private fun getWeatherIconResourceId(iconCode: String): Int {
    return when (iconCode) {
        "01d" -> R.drawable.ic_clear_day
        "01n" -> R.drawable.ic_clear_night
        "02n", "03n", "04n" -> R.drawable.ic_night_clouds
        "02d" -> R.drawable.ic_few_clouds
        "03d" -> R.drawable.ic_scattered_clouds
        "04d" -> R.drawable.ic_broken_clouds
        "09d", "09n" -> R.drawable.ic_shower_rain
        "10d", "10n" -> R.drawable.ic_rain
        "11d", "11n" -> R.drawable.ic_thunderstorm
        "13d", "13n" -> R.drawable.ic_snow
        "50d", "50n" -> R.drawable.ic_mist
        else -> R.drawable.ic_unknown
    }
}
