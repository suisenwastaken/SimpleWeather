// WeatherApiService.kt
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {
    @GET("data/2.5/weather")
    fun getWeather(
        @Query("q") city: String,
        @Query("appid") apiKey: String
    ): Call<WeatherResponse>
}
private const val API_KEY = "dded376c57129dbc181a6ca874fcb133"

class WeatherManager {


    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.openweathermap.org/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val weatherApi = retrofit.create(WeatherApi::class.java)

    fun getWeatherData(city: String, callback: (WeatherData) -> Unit) {
        val weatherCall = weatherApi.getWeather(city, API_KEY)

        weatherCall.enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (response.isSuccessful) {
                    val weatherResponse = response.body()
                    weatherResponse?.let {
                        val weatherData = WeatherData(
                            city = city,
                            temperature = it.main.temp,
                            humidity = it.main.humidity,
                            windSpeed = it.wind.speed,
                            feelsLike = it.main.feels_like,
                            weatherIcon = it.weather.firstOrNull()?.icon ?: ""
                        )
                        callback(weatherData)
                    }
                } else {
                    val weatherData = WeatherData(
                        city = "",
                        temperature = 0.0,
                        humidity = 0,
                        windSpeed = 0.0,
                        feelsLike = 0.0,
                        weatherIcon = ""
                    )
                    callback(weatherData)
                    println("Error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                println("Network error: ${t.message}")
            }
        })
    }

}
