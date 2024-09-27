package at.posselt.pfrpg2e.data.regions

enum class WeatherEffect {
    NONE,
    SNOW,
    RAIN,
    SUNNY,
    LEAVES,
    RAIN_STORM,
    FOG,
    BLIZZARD;
}

enum class Season {
    SPRING,
    SUMMER,
    FALL,
    WINTER;
}

enum class Month {
    JANUARY,
    FEBRUARY,
    MARCH,
    APRIL,
    MAY,
    JUNE,
    JULY,
    AUGUST,
    SEPTEMBER,
    OCTOBER,
    NOVEMBER,
    DECEMBER;
}

data class Climate(
    val coldDc: Int? = null,
    val precipitationDc: Int? = null,
    val month: Month,
    val season: Season,
    val weatherEventDc: Int? = 18
)

enum class WeatherType {
    COLD,
    SNOWY,
    RAINY,
    SUNNY,
}

fun getMonth(index: Int): Month =
    Month.entries[index]

fun findWeatherType(isCold: Boolean, hasPrecipitation: Boolean) =
    if (isCold && hasPrecipitation) {
        WeatherType.SNOWY
    } else if (isCold) {
        WeatherType.COLD
    } else if (hasPrecipitation) {
        WeatherType.RAINY
    } else {
        WeatherType.SUNNY
    }
