data class WeatherResponse(
    val main: Main,
    val wind: Wind,
    val weather: List<Weather>
)

data class Main(
    val temp: Double,
    val humidity: Int,
    val feels_like: Double
)

data class Wind(
    val speed: Double
)

data class Weather(
    val icon: String
)
